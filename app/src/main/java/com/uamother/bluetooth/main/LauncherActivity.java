package com.uamother.bluetooth.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;
import com.litesuits.bluetooth.LiteBleGattCallback;
import com.litesuits.bluetooth.LiteBluetooth;
import com.litesuits.bluetooth.exception.BleException;
import com.litesuits.bluetooth.exception.hanlder.DefaultBleExceptionHandler;
import com.litesuits.bluetooth.log.BleLog;
import com.litesuits.bluetooth.scan.PeriodScanCallback;
import com.litesuits.bluetooth.utils.BluetoothUtil;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.Constants;
import com.uamother.bluetooth.utils.StatusBarCompat;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Administrator on 2016/7/26.
 */
public class LauncherActivity extends Activity {

    BluetoothManager bluetoothManager;
    BluetoothAdapter mBTAdapter;

    String TAG = "ysq";

    /**
     * 蓝牙主要操作对象，建议单例。
     */
    private static LiteBluetooth liteBluetooth;
    /**
     * 默认异常处理器
     */
    private DefaultBleExceptionHandler bleExceptionHandler;

    public String UUID_SERVICE = "00001101-0000-1000-8000-00805F9B34FB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

//        init();

        initData();
    }

    private void init() {
        if (liteBluetooth == null) {
            liteBluetooth = new LiteBluetooth(this);
        }
        liteBluetooth.enableBluetoothIfDisabled(this, 1);
        bleExceptionHandler = new DefaultBleExceptionHandler(this);

        liteBluetooth.startLeScan(new PeriodScanCallback(10000) {
            @Override
            public void onScanTimeout() {
            }

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                liteBluetooth.scanAndConnect(device.getAddress(), false, new LiteBleGattCallback() {
                    @Override
                    public void onConnectSuccess(BluetoothGatt gatt, int status) {
                        // discover services !
                        gatt.discoverServices();
//                        initData();

                        getBluetoothState();
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        BluetoothUtil.printServices(gatt);
                        dialogShow(device.getAddress() + " Services Discovered SUCCESS !");
                    }

                    @Override
                    public void onConnectFailure(BleException exception) {
                        bleExceptionHandler.handleException(exception);
                        dialogShow(device.getAddress() + " Services Discovered FAILURE !");
                    }
                });
            }
        });

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager != null) {
            mBTAdapter = bluetoothManager.getAdapter();
        }

        if (mBTAdapter == null || !mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1000);
        }

//        mBTAdapter.startLeScan(new UUID[]{UUID.fromString(UUID_SERVICE)}, new BluetoothAdapter.LeScanCallback() {
//            @Override
//            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        liteBluetooth.scanAndConnect(device.getAddress(), false, new LiteBleGattCallback() {
//                            @Override
//                            public void onConnectSuccess(BluetoothGatt gatt, int status) {
//                                // discover services !
//                                gatt.discoverServices();
//                                initData();
//
//                                getBluetoothState();
//                            }
//
//                            @Override
//                            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                                BluetoothUtil.printServices(gatt);
//                                dialogShow(device.getAddress() + " Services Discovered SUCCESS !");
//                            }
//
//                            @Override
//                            public void onConnectFailure(BleException exception) {
//                                bleExceptionHandler.handleException(exception);
//                                dialogShow(device.getAddress() + " Services Discovered FAILURE !");
//                            }
//                        });
//
//                    }
//                });
//            }
//        });
//
    }

    private void getBluetoothState() {
        BleLog.i(TAG, "liteBluetooth.getConnectionState: " + liteBluetooth.getConnectionState());
        BleLog.i(TAG, "liteBluetooth isInScanning: " + liteBluetooth.isInScanning());
        BleLog.i(TAG, "liteBluetooth isConnected: " + liteBluetooth.isConnected());
        BleLog.i(TAG, "liteBluetooth isServiceDiscoered: " + liteBluetooth.isServiceDiscoered());
        if (liteBluetooth.getConnectionState() >= LiteBluetooth.STATE_CONNECTING) {
            BleLog.i(TAG, "lite bluetooth is in connecting or connected");
        }
        if (liteBluetooth.getConnectionState() == LiteBluetooth.STATE_SERVICES_DISCOVERED) {
            BleLog.i(TAG, "lite bluetooth is in connected, services have been found");
            addNewCallbackToOneConnection();
        }
    }

    private void addNewCallbackToOneConnection() {
        BluetoothGattCallback liteCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            }
        };

        if (liteBluetooth.isConnectingOrConnected()) {
            liteBluetooth.addGattCallback(liteCallback);
            liteBluetooth.removeGattCallback(liteCallback);
        }

        liteBluetooth.refreshDeviceCache();
    }

    public void dialogShow(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lite BLE");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void initData() {

        boolean isOpenGuide = CacheUtil.getCacheBooleanData(this, Constants.IS_OPEN_GUIDE, true);

        if (isOpenGuide) {
            startActivity(new Intent(this, GuideActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();

    }
}
