package com.uamother.bluetooth.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.*;
import android.widget.Toast;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.ble.BleConst;
import com.uamother.bluetooth.ble.BleHelper;
import com.uamother.bluetooth.ble.BleUtils;
import com.uamother.bluetooth.ble.ScaleBleService;
import com.uamother.bluetooth.ble.helper.ScaleMultiBleManager;
import com.uamother.bluetooth.ble.lib.*;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.Constants;
import com.uamother.bluetooth.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/26.
 */
public class LauncherActivity extends Activity {

    Handler uiHandler = new Handler(Looper.getMainLooper());
    BluetoothLeScannerCompat bleScanner = BluetoothLeScannerCompat.getScanner();
    ScanFilter.MarshmallowPermission MARSHMALLOW_PERMISSION = new ScanFilter.MarshmallowPermission();

    ScaleBleService bleService;

    ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
            .setMatchOptions(3000, 3000)
            .setUseHardwareBatchingIfSupported(false).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

//        initData();

        BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(this);
        if (adapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙",Toast.LENGTH_SHORT);
            return;
        }
        adapter.enable();

        this.bindService(new Intent(this, ScaleBleService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.postDelayed(startScanAction, 500);
    }

    final Runnable startScanAction = new Runnable() {
        @Override
        public void run() {
            if (BleHelper.isEnable(getBaseContext())) {
                MARSHMALLOW_PERMISSION.check(LauncherActivity.this);
                //开始扫描
                bleScanner.startScan(buildScanFilters(), scanSettings, scanCallback);
            } else {
                bleScanner.stopScan(scanCallback);
                //关闭动画
            }
        }
    };

    List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();

        scanFilters.add(new ScanFilter.Builder().setDeviceName("HMSoft").build());
        scanFilters.add(new ScanFilter.Builder().setDeviceNamePrefix("HMSoft").setServiceUuid(new ParcelUuid(BleConst.CLIENT_CHARACTERISTIC_CONFIG)).build());
        return scanFilters;
    }


    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            LogUtils.saveBleLog("扫描失败:", errorCode);
        }

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            LogUtils.saveBleLog("出现设备,callbackType:", callbackType, result);
            if (result.getScanRecord() == null) {
                return;
            }
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onScan(result);
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            LogUtils.saveBleLog("出现批量结果:");
            for (ScanResult scanResult : results) {
                LogUtils.saveBleLog(scanResult);
            }
        }
    };

    public void onScan(final ScanResult scanResult) {
        String deviceName = scanResult.getScanRecord().getDeviceName();
        String mac = scanResult.getDevice().getAddress();

        bleService.connect(scanResult.getDevice());
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((ScaleBleService.ScaleBleBinder) service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


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
