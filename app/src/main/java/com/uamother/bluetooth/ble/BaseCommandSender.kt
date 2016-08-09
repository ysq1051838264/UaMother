package com.hdr.wristband.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.hdr.blelib.MultiBleManager
import com.hdr.wristband.utils.StringUtils

/**
 * Created by ysq on 16/7/31.
 */
class BaseCommandSender(val address: String, val bleManager: MultiBleManager<*>, val writeBgc: BluetoothGattCharacteristic) : CommandSender() {
    override fun send(value: ByteArray) {
        writeBgc.value = value
        bleManager.writeCharacteristic(address, writeBgc)
    }

}