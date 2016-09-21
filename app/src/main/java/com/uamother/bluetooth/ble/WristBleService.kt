package com.hdr.wristband.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hdr.blelib.BleProfileService
import com.hdr.wristband.utils.BleConst
import com.hdr.wristband.utils.StringUtils
import com.hdr.wristband.xrz.XrzWristDecoder
import com.uamother.bluetooth.other.SpHelper
import com.uamother.bluetooth.utils.Constants
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

    var reconnectTryCount = 0

    val reconnectTryTime = arrayOf(10, 20, 40, 80)

    val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    val reconnectAction = Runnable {
        Log.i("wrist", "正在尝试重新连接")
        reconnectTryCount++
        doConnect()
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var mac = intent?.getStringExtra(Constants.SP_KEY_CURRENT_MAC_VALUE);
            this.currentAddress = mac!!

        return super.onStartCommand(intent, flags, startId)
    }

    fun doConnect() {
        if (!StringUtils.isEmpty(currentAddress)) {
            val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(currentAddress)
            wristBleManager.connect(device)
        }
    }

    fun tryReconnect() {
        val delayTime = if (reconnectTryCount >= reconnectTryTime.size) reconnectTryTime.last() * 1000L else reconnectTryTime[reconnectTryCount] * 1000L
        Log.i("wrist", "连接断开,$delayTime ms 后尝试重连")

        mainHandler.postDelayed(reconnectAction, delayTime)
    }

    override fun onReceiveData(uuid: UUID, value: ByteArray) {
        if (value[0].toInt() == 0) {
            return
        }
        wristDecoder?.onReceiveData(uuid, value)
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

        tryReconnect()
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