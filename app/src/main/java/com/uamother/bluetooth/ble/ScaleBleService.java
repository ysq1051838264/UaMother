package com.uamother.bluetooth.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.uamother.bluetooth.ble.helper.BleProfileService;
import com.uamother.bluetooth.ble.helper.MultiBleManager;
import com.uamother.bluetooth.ble.helper.ScaleMultiBleManager;

import java.util.UUID;

/**
 * Created by hdr on 16/3/17.
 */
public class ScaleBleService extends BleProfileService implements ScaleMultiBleManager.ScaleBleCallback {

    ScaleMultiBleManager scaleBleManager;
    private String mDeviceAddress;

    @Override
    public IBinder onBind(Intent intent) {
        return new ScaleBleBinder();
    }

    @Override
    protected MultiBleManager initializeManager() {
        return scaleBleManager = new ScaleMultiBleManager(this);
    }

    public void connect(BluetoothDevice device) {
        this.mDeviceAddress = device.getAddress();
        scaleBleManager.connect(device);
    }

    public void disconnect() {
        scaleBleManager.disconnect(mDeviceAddress);
    }

    public void writeData(UUID serviceUUID, UUID characteristicUUID, byte[] value) {
        scaleBleManager.writeData(serviceUUID, characteristicUUID, value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mDeviceAddress = null;
    }

    @Override
    public void onReceiveData(UUID uuid, byte[] value) {
        Intent intent = new Intent(BleConst.ACTION_BLE_RECEIVE_DATA);

        intent.putExtra(BleConst.KEY_UUID, uuid);
        intent.putExtra(BleConst.KEY_DATA, value);
        sendBroadcast(intent);
    }

    @Override
    public void onServicesDiscovered(String address) {
        Intent intent = new Intent(BleConst.ACTION_BLE_DISCOVERED);
        intent.putExtra(BleConst.KEY_MAC, address);
        sendBroadcast(intent);
    }

    @Override
    public void onDeviceDisconnected(String address) {
        Intent intent = new Intent(BleConst.ACTION_BLE_DISCONNECTED);
        intent.putExtra(BleConst.KEY_MAC, address);
        sendBroadcast(intent);
    }

    @Override
    public void onDeviceConnected(String address) {
        Intent intent = new Intent(BleConst.ACTION_BLE_CONNECTED);
        intent.putExtra(BleConst.KEY_MAC, address);
        sendBroadcast(intent);
    }

    @Override
    public int getProtocolType() {
        return -1;
    }

    @Override
    public String getCurrentAddress() {
        return mDeviceAddress;
    }

    @Override
    public boolean needReadScaleName() {
        return false;
    }

    @Override
    public boolean needReadInternalModel() {
        return  true;
    }

    public class ScaleBleBinder extends Binder {
        public ScaleBleService getService() {
            return ScaleBleService.this;
        }
    }

}
