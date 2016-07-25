package com.uamother.bluetooth.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.other.SpHelper;
import com.uamother.bluetooth.other.UserConst;
import com.uamother.bluetooth.utils.StatusBarCompat;

public class MainActivity extends AppCompatActivity {

    TextView aboutTv;
    private SpHelper spHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        StatusBarCompat.compat(this);

        spHelper = SpHelper.getInstance();

        SharedPreferences.Editor editor = spHelper.getPersistentEditor();
        editor.putString(UserConst.SP_KEY_SESSION_KEY,"sp_key_session_key");
        editor.apply();

        aboutTv = (TextView) findViewById(R.id.aboutTv);
        aboutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(MainActivity.this,AboutActivity.class));
            }
        });
    }


}
