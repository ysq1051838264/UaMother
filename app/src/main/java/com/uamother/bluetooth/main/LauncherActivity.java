package com.uamother.bluetooth.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.Constants;

/**
 * Created by Administrator on 2016/7/26.
 */
public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                initData();
            }
        }, 800);
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
