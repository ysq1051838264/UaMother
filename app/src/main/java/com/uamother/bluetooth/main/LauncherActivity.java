package com.uamother.bluetooth.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.hdr.wristband.ble.WristBleService;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.other.SpHelper;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.Constants;

/**
 * Created by Administrator on 2016/7/26.
 */
public class LauncherActivity extends Activity {

    private long backStartTime;
    SpHelper spHelper;

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - backStartTime > 2000) {
            Toast.makeText(this, "再次点击退出！", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
        backStartTime = now;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        spHelper = SpHelper.initInstance(this);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                initData();
            }
        }, 800);
    }

    private void initData() {
        Intent intent = new Intent(this, WristBleService.class);
        String mac = spHelper.getString(Constants.SP_KEY_CURRENT_MAC, "");
        intent.putExtra(Constants.SP_KEY_CURRENT_MAC_VALUE,mac);
        startService(intent);

        boolean isOpenGuide = CacheUtil.getCacheBooleanData(this, Constants.IS_OPEN_GUIDE, true);

//        if (isOpenGuide) {
//            startActivity(new Intent(this, GuideActivity.class));
//        } else {
            startActivity(new Intent(this, MainActivity.class));
//        }
        finish();
    }
}
