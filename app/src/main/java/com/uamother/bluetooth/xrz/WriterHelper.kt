package com.hdr.wristband.xrz

import android.bluetooth.BluetoothGattCharacteristic
import java.util.*

/**
 * Created by ysq on 16/7/24.
 */
class WriterHelper(val writeBgc: BluetoothGattCharacteristic) {

    private final val random = Random()

    init {
        if (writeBgc.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 0 && writeBgc.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE == 0) {
            throw RuntimeException("非可写的 BluetoothGattCharacteristic")
        }
    }

    private val cmdCache = ArrayList<ByteArray>()

    /**
     * 拆包下发命令
     */
    fun flush(cmd: ByteArray) {
        if (!this.cmdCache.isEmpty()) {
            this.cmdCache.clear()
        }
        if (cmd.size <= 20) {
            this.cmdCache.add(cmd)
        } else {
            val rest = cmd.size % 20

            var current = 0
            while (current + 20 <= cmd.size) {
                val bytes = ByteArray(20)
                System.arraycopy(cmd, current, bytes, 0, 20)
                this.cmdCache.add(bytes)
                current += 20
            }

            if (rest > 0) {
                val bytes = ByteArray(rest)
                System.arraycopy(cmd, cmd.size - rest, bytes, 0, rest)
                this.cmdCache.add(bytes)
            }
        }
    }

}