package com.uamother.bluetooth.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.Constants;
import com.uamother.bluetooth.utils.StatusBarCompat;

/**
 * Created by Administrator on 2016/7/26.
 */
public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initData();
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
