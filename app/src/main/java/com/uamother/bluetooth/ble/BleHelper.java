package com.uamother.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.uamother.bluetooth.utils.LogUtils;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * 蓝牙辅助类,只做和蓝牙相关的基本操作
 */
public class BleHelper extends BluetoothGattCallback implements LeScanCallback, BleDataTransfer {

    private final static String TAG = "BluetoothHelper";

    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;

    private volatile BleStatus status = BleStatus.NOSCANNING;

    private Activity activity;

    private final Map<String, BluetoothDevice> scanCache;
    private BluetoothDevice targetDevice;

    public BleHelper(Activity activity) {
        this.activity = activity;
        scanCache = new Hashtable<String, BluetoothDevice>();
    }

    public void initBle() {
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        this.adapter = bluetoothManager.getAdapter();
    }

    //蓝牙状态枚举类
    public enum BleStatus {
        CLOSED("closed"),//蓝牙关闭状态
        SCANNING("scanning"),//蓝牙正在扫描
        NOSCANNING("noscanning"),//蓝牙处于打开状态，并且已停止扫描
        PREPARED("prepared"),//扫描到设备，并准备连接蓝牙
        DISCONNECTED("disconnected"),//蓝牙已经和断开连接,没有扫描，这个状态在onConnectionStateChange被设置，并且会马上又调用start启动扫描
        CONNECTED("connected"),//蓝牙处于与秤连接的状态
        FORCE_DISCONNECTED("force_disconnected");//蓝牙处于被强制断开的状态，这个状态不会再启动扫描

        String name;

        BleStatus(String name) {
            this.name = name;
        }
    }

    @Override
    public synchronized void onLeScan(final BluetoothDevice device,
                                      final int rssi, final byte[] scanRecord) {
        final String deviceAddress = device.getAddress();
        LogUtils.saveBleLog("扫描到设备，蓝牙名为:" + device.getName() + " 地址为:" + deviceAddress + " 信号强度:" + rssi);
        if (this.status != BleStatus.SCANNING) {
            LogUtils.saveBleLog("当前状态并非扫描状态，丢弃这个包 " + deviceAddress);
            adapter.stopLeScan(BleHelper.this);
            return;
        }
        synchronized (scanCache) {

/*        StringBuilder sb = new StringBuilder();
        for (byte b : scanRecord) {
            sb.append(String.format("%02X ", b));
        }
        LogUtils.log("hdr", "扫描数据是:", sb.toString());*/

            scanCache.put(deviceAddress, device);

            String deviceName = device.getName();

            Intent intent = new Intent(BleConst.ACTION_BLE_SCAN);
            intent.putExtra(BleConst.KEY_DEVICE_NAME, deviceName);
            intent.putExtra(BleConst.KEY_MAC, deviceAddress);
            intent.putExtra(BleConst.KEY_SCAN_RECORD, scanRecord);
            this.sendBroadcast(intent);
        }
    }

    public void resume() {
        if (this.status == BleStatus.FORCE_DISCONNECTED) {
            this.status = BleStatus.DISCONNECTED;
        }
    }


    /**
     * 蓝牙初始化，并开始扫描
     */
    public synchronized void start() {
        if (adapter == null) {
//            ToastMaker.showOnUiThread("设备无蓝牙或蓝牙版本不是4.0");
            return;
        }
        // 正常启动
        if (!adapter.isEnabled()) {
            LogUtils.saveBleLog("蓝牙不可用！");
            this.status = BleStatus.CLOSED;
            return;
        }
        showStatus();
        if (this.status == BleStatus.CONNECTED) {
            this.disconnect(true);
        } else if (status == BleStatus.SCANNING) {
            LogUtils.saveBleLog("在start中，检测到正在扫描");
        } else if (status != BleStatus.FORCE_DISCONNECTED) {
            synchronized (scanCache) {
                scanCache.clear();
            }
            status = BleStatus.SCANNING;
            this.gatt = null;
        }
    }

    public void stopScan() {
        LogUtils.saveBleLog("调用了stopScan，当前状态为:" + status.name);
        if (status == BleStatus.SCANNING) {
            status = BleStatus.NOSCANNING;
            LogUtils.saveBleLog("蓝牙停止扫描 stopScan");
        }
    }

    public void clearScanCache() {
        scanCache.clear();
    }

    public void restart() {
        LogUtils.saveBleLog("在restart中");
        showStatus();
        if (status == BleStatus.DISCONNECTED) {
            start();
        }
    }

    /**
     * 停止扫描蓝牙，并且关闭蓝牙活动
     */
    public void stop() {
        stopScan();
        if (isConnected()) {
            LogUtils.saveBleLog("在stop强行结束连接");
            this.disconnect(true);
        } else if (status == BleStatus.PREPARED) {
            status = BleStatus.FORCE_DISCONNECTED;
            LogUtils.saveBleLog("正在准备连接，但是此时已调用 stop");
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                        int newState) {
        LogUtils.saveBleLog("蓝牙连接状态已改变 status:" + status + " newState:" + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED && this.gatt == null) {
            if (this.status != BleStatus.PREPARED) {
                LogUtils.saveBleLog("蓝牙状态不是 " + BleStatus.PREPARED + " 断开连接");
                gatt.disconnect();
                return;
            }

            this.status = BleStatus.CONNECTED;
            this.gatt = gatt;
            this.gatt.discoverServices();

            Intent intent = new Intent(BleConst.ACTION_BLE_CONNECTED);
            this.sendBroadcast(intent);

        } else if (newState != BluetoothProfile.STATE_CONNECTED) {

            LogUtils.saveBleLog("蓝牙断开连接,状态为:" + this.status.name);
            if (this.status != BleStatus.FORCE_DISCONNECTED) {
                this.status = BleStatus.DISCONNECTED;
            }
            gatt.close();
            if (this.gatt != null && this.gatt != gatt)
                this.gatt.close();

            Intent intent = new Intent(BleConst.ACTION_BLE_DISCONNECTED);
            this.sendBroadcast(intent);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {

            Intent intent = new Intent(BleConst.ACTION_BLE_DISCOVERED);
            this.sendBroadcast(intent);

        } else {
            LogUtils.saveBleLog("蓝牙discovered失败,status," + status);
            disconnect(false);
        }
    }


    /**
     * 读取到了秤的型号名称
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {

        Intent intent = new Intent(BleConst.ACTION_BLE_RECEIVE_DATA);
        intent.putExtra(BleConst.KEY_UUID, characteristic.getUuid());
        intent.putExtra(BleConst.KEY_DATA, characteristic.getValue());
        this.sendBroadcast(intent);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(BleConst.ACTION_BLE_RECEIVE_DATA);
        intent.putExtra(BleConst.KEY_UUID, characteristic.getUuid());
        intent.putExtra(BleConst.KEY_DATA, characteristic.getValue());
        this.sendBroadcast(intent);

//        final byte[] initChartType = characteristic.getValue();
//        decoder.decode(initChartType);
    }

    void sendBroadcast(Intent intent) {
        if (activity == null) {
            return;
        }
        activity.sendBroadcast(intent);
    }

    public void readData(UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattCharacteristic bgc = getBgc(serviceUUID, characteristicUUID);
        if (bgc == null) {
            return;
        }
        this.gatt.readCharacteristic(bgc);

    }

    public void setNotification(UUID serviceUUID, UUID characteristicUUID) {

        BluetoothGattCharacteristic bgc = getBgc(serviceUUID, characteristicUUID);
        if (bgc == null) {
            return;
        }
        gatt.setCharacteristicNotification(bgc, true);
        BluetoothGattDescriptor descriptor = bgc.getDescriptor(BleConst.CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    public void setIndication(UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattCharacteristic bgc = getBgc(serviceUUID, characteristicUUID);
        if (bgc == null) {
            return;
        }
        gatt.setCharacteristicNotification(bgc, true);
        BluetoothGattDescriptor descriptor = bgc.getDescriptor(BleConst.CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    BluetoothGattCharacteristic getBgc(UUID serviceUUID, UUID characteristicUuid) {
        if (gatt == null) {
            return null;
        }
        BluetoothGattService bgs = this.gatt.getService(serviceUUID);
        if (bgs == null) {
            return null;
        }
        BluetoothGattCharacteristic bgc = bgs.getCharacteristic(characteristicUuid);
        return bgc;
    }

    /**
     * 主动断开连接的方法
     */
    public void disconnect(boolean isForce) {
        if (gatt == null) {
            LogUtils.saveBleLog("disconnectAll:BluetoothGatt not initialized");
        } else {
            try {
                adapter.cancelDiscovery();
                gatt.disconnect();
            } catch (Exception e) {
                LogUtils.saveBleLog("连接蓝牙出错");
            }
        }
        this.status = isForce ? BleStatus.FORCE_DISCONNECTED
                : BleStatus.DISCONNECTED;
        Log.w(TAG, "status :" + status);
    }

    public void close() {
        if (adapter != null && adapter.isEnabled()) {
            adapter.cancelDiscovery();
            // mBluetoothAdapter.disable();
        }
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
        status = BleStatus.NOSCANNING;
        LogUtils.saveBleLog(" close 中");
    }

    @Override
    public void writeData(UUID serviceUUID, UUID characteristicUUID, byte[] data) {
        BluetoothGattCharacteristic bgc = getBgc(serviceUUID, characteristicUUID);
        if (bgc != null) {
            bgc.setValue(data);
            this.gatt.writeCharacteristic(bgc);
            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                sb.append(String.format("%02X ", b));
            }
            LogUtils.saveBleLog("发送数据:", sb.toString());

        }
    }

    public boolean isConnected() {
        return this.status == BleStatus.CONNECTED;
    }

    public void showStatus() {
        if (status != null) {
            LogUtils.saveBleLog("当前蓝牙状态为:" + this.status.name);
        }
    }

    public static boolean isEnable(Context context) {
        boolean isEnable = false;
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null
                && (mBluetoothAdapter.isEnabled() || mBluetoothAdapter
                .getState() == BluetoothAdapter.STATE_CONNECTING))
            isEnable = true;
        return isEnable;
    }

}
