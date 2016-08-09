package com.hdr.wristband.model

import android.bluetooth.BluetoothDevice

/**
 * Created by hdr on 16/7/4.
 */
data class BleDevice(
        val device: BluetoothDevice,
        val rssi:Int
)