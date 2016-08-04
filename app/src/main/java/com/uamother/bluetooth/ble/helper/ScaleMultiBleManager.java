package com.uamother.bluetooth.ble.helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.UUID;

/**
 * Created by hdr on 16/3/17.
 */
public class ScaleMultiBleManager extends MultiBleManager<ScaleMultiBleManager.ScaleBleCallback> {
    BluetoothGattCharacteristic yolandaReadBgc, yolandaWriteBgc, yolandaBleReadBgc, yolandaBleWriteBgc, yolandaNameReadBgc, yolandaBleInfoWriterBgc, miNotifyBgc, miIndicateBgc;

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
        BluetoothGattCharacteristic bgc = null;
        if (characteristicUUID.equals(yolandaWriteBgc.getUuid())) {
            bgc = yolandaWriteBgc;
        } else if (characteristicUUID.equals(yolandaBleWriteBgc.getUuid())) {
            bgc = yolandaBleWriteBgc;
        } else if (characteristicUUID.equals(yolandaBleInfoWriterBgc.getUuid())) {
            bgc = yolandaBleInfoWriterBgc;
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

    final BleManagerGattCallback bleManagerGattCallback = new BleManagerGattCallback() {
        @Override
        protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
            String address = gatt.getDevice().getAddress();
            if (!address.equals(mCallbacks.getCurrentAddress())) {
                return false;
            }
            switch (mCallbacks.getProtocolType()) {

                case 3: {
                    //体重秤
                   // yolandaReadBgc = getCharacteristic(gatt, BleConst.UUID_IBT_SERVICES, BleConst.UUID_IBT_READ);
                    return yolandaReadBgc != null;
                }
                case -1: {
                    return false;
                }
                default: {
                    //普通智能秤

                    return yolandaReadBgc != null && yolandaWriteBgc != null;
                }

            }
        }

        @Override
        protected void onDeviceConnected(String address) {

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
            switch (mCallbacks.getProtocolType()) {
                case 6: {
                    //小米设备
                    addRequest(address, Request.newEnableNotificationsRequest(miNotifyBgc));
                    addRequest(address, Request.newEnableIndicationsRequest(miIndicateBgc));
                    break;
                }

                case 3: {
                    //体重秤
                    addRequest(address, Request.newEnableNotificationsRequest(yolandaReadBgc));
                    break;
                }
                case -1: {
                    break;
                }
                default: {
                    //普通智能秤
                    addRequest(address, Request.newEnableNotificationsRequest(yolandaReadBgc));
                    if (yolandaBleReadBgc != null) {
                        addRequest(address, Request.newEnableIndicationsRequest(yolandaBleReadBgc));
                    }
                    if (mCallbacks.needReadScaleName() && yolandaNameReadBgc != null) {
                        addRequest(address, Request.newReadRequest(yolandaNameReadBgc));
                    } else if (mCallbacks.needReadInternalModel() && yolandaBleInfoWriterBgc != null) {
                        addRequest(address, Request.newWriteRequest(yolandaBleInfoWriterBgc, new byte[]{0x42, 0x04}));
                    }

                }

            }
        }
    };
}
