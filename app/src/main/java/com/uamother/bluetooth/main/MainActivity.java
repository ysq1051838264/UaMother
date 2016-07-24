package com.uamother.bluetooth.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.utils.StatusBarCompat;

public class MainActivity extends AppCompatActivity {

    TextView aboutTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        StatusBarCompat.compat(this);

        aboutTv = (TextView) findViewById(R.id.aboutTv);
        aboutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(MainActivity.this,AboutActivity.class));
            }
        });
    }


}
