package com.uamother.bluetooth.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.other.SpHelper;
import com.uamother.bluetooth.other.UserConst;

/**
 * Created by Administrator on 2016/7/26.
 */
public class LauncherActivity extends Activity {
    ImageView imageView;

    private SpHelper spHelper;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_launcher);
        imageView = (ImageView) findViewById(R.id.imageView);

        spHelper = SpHelper.getInstance();
        initData();
    }

    private void initData() {
        String sessionKey = spHelper.getString(UserConst.SP_KEY_SESSION_KEY, null);
        int integer = sessionKey == null ? 1 : 2;
        switch (integer) {
            case 1:
                startActivity(new Intent().setClass(this, GuideActivity.class));
                finish();
                break;
            case 2:
                startActivity(new Intent().setClass(this, MainActivity.class));
                finish();
                break;
        }
    }
}
