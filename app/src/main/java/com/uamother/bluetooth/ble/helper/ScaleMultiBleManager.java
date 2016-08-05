package com.uamother.bluetooth.ble.helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;
import com.uamother.bluetooth.ble.BleConst;

import java.util.UUID;

/**
 * Created by hdr on 16/3/17.
 */
public class ScaleMultiBleManager extends MultiBleManager<ScaleMultiBleManager.ScaleBleCallback> {
    BluetoothGattCharacteristic ReadBgc, WriteBgc;
    public ScaleMultiBleManager(Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return bleManagerGattCallback;
    }

    public interface ScaleBleCallback extends BleManagerCallbacks {
        void onReceiveData(UUID uuid, byte[] value);

        int getProtocolType();

        String getCurrentAddress();

        boolean needReadScaleName();

        boolean needReadInternalModel();

    }

    public void writeData(UUID serviceUUID, UUID characteristicUUID, byte[] value) {
        BluetoothGattCharacteristic bgc = WriteBgc;
        if (characteristicUUID.equals(WriteBgc.getUuid())) {
            bgc = WriteBgc;
        }else {
            BluetoothGatt gatt = gattMap.get(mCallbacks.getCurrentAddress());
            if (gatt != null) {
                bgc = bleManagerGattCallback.getCharacteristic(gatt, serviceUUID, characteristicUUID);
            }
        }
        if (bgc == null) {
            return;
        }
        bgc.setValue(value);
        writeCharacteristic(mCallbacks.getCurrentAddress(), bgc);
    }

    public void readData(){
        BluetoothGattCharacteristic bgc = null;

        byte[] SendDatabyte = new byte[4];

        SendDatabyte[0] = '?';
        SendDatabyte[1] = 0x02;
        SendDatabyte[2] = (byte) 0xa1;
        SendDatabyte[3] = (byte) 0xa3;

        BluetoothGatt gatt = gattMap.get(mCallbacks.getCurrentAddress());
        if (gatt != null) {
            bgc = bleManagerGattCallback.getCharacteristic(gatt, BleConst.CLIENT_CHARACTERISTIC_CONFIG, BleConst.CLIENT_CHARACTERISTIC_CONFIG);
        }

        bgc.setValue(SendDatabyte);

        Log.i("ysq读取数据：", "发送的信息是：" + SendDatabyte);

        readCharacteristic(mCallbacks.getCurrentAddress(), bgc);
    }


    final BleManagerGattCallback bleManagerGattCallback = new BleManagerGattCallback() {
        @Override
        protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
            String address = gatt.getDevice().getAddress();
            if (!address.equals(mCallbacks.getCurrentAddress())) {
                return false;
            }
            ReadBgc = getCharacteristic(gatt, BleConst.CLIENT_CHARACTERISTIC_CONFIG, BleConst.CLIENT_CHARACTERISTIC_CONFIG);
            WriteBgc = getCharacteristic(gatt, BleConst.CLIENT_CHARACTERISTIC_CONFIG, BleConst.CLIENT_CHARACTERISTIC_CONFIG);

            return ReadBgc != null && WriteBgc != null;
        }

        @Override
        protected void onDeviceConnected(String address) {
            Log.i("ysq:是不是设备链接成功", "address:" + address);
            readData();
        }

        @Override
        protected void onDeviceDisconnected(String address) {

        }

        @Override
        protected void onCharacteristicRead(String address, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mCallbacks.onReceiveData(characteristic.getUuid(), characteristic.getValue());
        }

        @Override
        protected void onCharacteristicNotified(String address, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mCallbacks.onReceiveData(characteristic.getUuid(), characteristic.getValue());
        }

        @Override
        protected void onCharacteristicIndicated(String address, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mCallbacks.onReceiveData(characteristic.getUuid(), characteristic.getValue());
        }

        @Override
        protected void onDeviceServiceDiscovered(String address) {
            if (!address.equals(mCallbacks.getCurrentAddress())) {
                return;
            }
            addRequest(address, Request.newEnableNotificationsRequest(ReadBgc));
            addRequest(address, Request.newEnableNotificationsRequest(WriteBgc));
        }
    };
}
