package com.hdr.wristband.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.hdr.blelib.BleManagerCallbacks
import com.hdr.blelib.MultiBleManager
import com.hdr.wristband.utils.StringUtils
import java.util.*

/**
 * Created by hdr on 16/7/4.
 */
class WristBleManager(context: Context) : MultiBleManager<WristBleManager.WristBleCallback>(context) {
    companion object {
        val NRF51_UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val NRF51_UUID_CHARACTERISTIC_WRITE = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        val NRF51_UUID_CHARACTERISTIC_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }

    lateinit var writeBgc: BluetoothGattCharacteristic
    lateinit var notifyBgc: BluetoothGattCharacteristic

    override fun getGattCallback(): MultiBleManager<WristBleManager.WristBleCallback>.BleManagerGattCallback {
        return bleManagerGattCallback
    }

    val bleManagerGattCallback = object : MultiBleManager<WristBleManager.WristBleCallback>.BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val address = gatt.device.address
            if (address != mCallbacks.currentAddress) {
                return false
            }
            val writeBgc = getCharacteristic(gatt, NRF51_UUID_SERVICE, NRF51_UUID_CHARACTERISTIC_WRITE)
            val notifyBgc = getCharacteristic(gatt, NRF51_UUID_SERVICE, NRF51_UUID_CHARACTERISTIC_NOTIFY)
            if (writeBgc == null || notifyBgc == null) {
                return false
            }
            this@WristBleManager.writeBgc = writeBgc
            this@WristBleManager.notifyBgc = notifyBgc
            return true
        }

        override fun onDeviceConnected(address: String) {
        }

        override fun onDeviceDisconnected(address: String) {
        }

        override fun onCharacteristicNotified(address: String, gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            mCallbacks.onReceiveData(characteristic.uuid,characteristic.value)
        }

        override fun onCharacteristicIndicated(address: String, gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            mCallbacks.onReceiveData(characteristic.uuid,characteristic.value)
        }

        override fun onDeviceServiceDiscovered(address: String) {
            if (address != mCallbacks.currentAddress) {
                return
            }
            //设置监听通道
            addRequest(address, Request.newEnableNotificationsRequest(notifyBgc))
        }

    }

    fun writeData(value: ByteArray) {
        writeBgc.value = value
        writeCharacteristic(mCallbacks.currentAddress, writeBgc);
    }

    interface WristBleCallback : BleManagerCallbacks {
        val currentAddress: String

        fun onReceiveData(uuid: UUID, value: ByteArray)
    }
}