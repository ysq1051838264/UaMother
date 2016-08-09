package com.hdr.wristband.ble

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import com.hdr.blelib.BleProfileService
import com.hdr.wristband.utils.BleConst
import com.hdr.wristband.xrz.XrzWristDecoder
import java.util.*

/**
 * Created by ysq on 16/7/4.
 */
class WristBleService : BleProfileService(), WristBleManager.WristBleCallback {

    override var currentAddress: String = ""

    val wristBleManager by lazy { WristBleManager(this) }
    var wristDecoder: WristDecoder? = null
        private set

    override fun initializeManager() = wristBleManager

    override fun onBind(intent: Intent?) = MyBinder()

    inner class MyBinder : Binder() {
        val service: WristBleService
            get() = this@WristBleService
    }

    override fun onReceiveData(uuid: UUID, value: ByteArray) {
        if (value[0].toInt() == 0) {
            return
        }
        wristDecoder?.onReceiveData(uuid,value)
    }

    override fun onServicesDiscovered(address: String) {
        val intent = Intent(BleConst.ACTION_BLE_DISCOVERED)
        intent.putExtra(BleConst.KEY_MAC, address)
        sendBroadcast(intent)

        wristDecoder = XrzWristDecoder(BaseCommandSender(currentAddress, wristBleManager, wristBleManager.writeBgc))
    }

    override fun onDeviceDisconnected(address: String) {
        val intent = Intent(BleConst.ACTION_BLE_DISCONNECTED)
        intent.putExtra(BleConst.KEY_MAC, address)
        sendBroadcast(intent)
    }

    override fun onDeviceConnected(address: String) {
        val intent = Intent(BleConst.ACTION_BLE_CONNECTED)
        intent.putExtra(BleConst.KEY_MAC, address)
        sendBroadcast(intent)
    }

    fun connect(device: BluetoothDevice) {
        this.currentAddress = device.address
        wristBleManager.connect(device)
    }

    fun disconnect() {
        this.currentAddress = ""
        wristBleManager.disconnect(currentAddress)
    }

}