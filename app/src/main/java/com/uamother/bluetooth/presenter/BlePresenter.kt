package com.hdr.wristband

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.net.ConnectivityManager
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import com.hdr.blelib.utils.BleUtils
import com.hdr.wristband.ble.BleSubscriber
import com.hdr.wristband.ble.WristBleService
import com.hdr.wristband.model.BleDevice
import com.hdr.wristband.utils.BleConst
import no.nordicsemi.android.support.v18.scanner.*
import org.jetbrains.anko.toast
import rx.Observable
import java.util.*

/**
 * 蓝牙操作实现类
 * Created by ysq on 16/7/24.
 */
class BlePresenter(val view: BleView) {

    interface BleView {

        val ctx: Context

        fun newScanDevice(device: BleDevice)

        fun updateScanDevice(index: Int, device: BleDevice)

        fun connectSuccess()
    }

    var bleService: WristBleService? = null

    val bleScanner = BluetoothLeScannerCompat.getScanner()

    val deviceList = ArrayList<BleDevice>()

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name?.isBlank() ?: true) {
                return
            }
            connect(result.device)
        }
    }

    internal fun buildScanFilters(): List<ScanFilter> {
        val scanFilters = ArrayList<ScanFilter>()

        scanFilters.add(ScanFilter.Builder().setDeviceName("HMSoft").build())
        scanFilters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid( UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))).build())
        return scanFilters
    }

    val scanFilter: ScanFilter
        get() {
            return ScanFilter.Builder()
                    .setDeviceAddress("88:4A:EA:14:F4:68")
                    .build()
        }

    internal var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            bleService = (service as WristBleService.MyBinder).service
            startScan()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bleService = null
        }
    }

    val bleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) {
                return
            }
            val action = intent.action
            when (action) {

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    //蓝牙开关发生了变化

                }
                BleConst.ACTION_BLE_CONNECTED -> {
                    val address = intent.getStringExtra(BleConst.KEY_MAC)
                }
                BleConst.ACTION_BLE_DISCONNECTED -> {
                    val address = intent.getStringExtra(BleConst.KEY_MAC)
                }
                BleConst.ACTION_BLE_DISCOVERED -> {
                    Log.i("ysq", "连接成功")
                    view.connectSuccess()
                }
                ConnectivityManager.CONNECTIVITY_ACTION -> {

                }
            }

        }

    }

    fun init() {
        val intentFilter = IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BleConst.ACTION_BLE_RECEIVE_DATA);
        intentFilter.addAction(BleConst.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BleConst.ACTION_BLE_DISCONNECTED);
        intentFilter.addAction(BleConst.ACTION_BLE_DISCOVERED);

        view.ctx.registerReceiver(bleReceiver, intentFilter);

        view.ctx.bindService(Intent(view.ctx, WristBleService::class.java), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    fun release() {
        view.ctx.unregisterReceiver(this.bleReceiver)
        view.ctx.unbindService(serviceConnection)
    }

    fun startScan() {
        if (!BleUtils.isEnable(view.ctx)) {
            view.ctx.toast("蓝牙没有打开")
            return
        }
        deviceList.clear()
//        bleScanner.startScan(listOf(scanFilter), ScanSettings.Builder().build(), scanCallback)
        bleScanner.startScan(buildScanFilters(), ScanSettings.Builder().build(), scanCallback)
    }

    fun stopScan() {
        bleScanner.stopScan(scanCallback)
    }

    fun connect(device: BluetoothDevice) {
        stopScan()
        bleService?.connect(device)
    }

    fun disconnect() {
        bleService?.disconnect()
    }

    fun <T : Any> Observable<T>.bleSubscribe(action: ((T) -> Unit)) {
        this.subscribe(BleSubscriber(action))
    }
}