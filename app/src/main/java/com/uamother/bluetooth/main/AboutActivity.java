package com.uamother.bluetooth.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.utils.StatusBarCompat;

/**
 * Created by ysq on 16/7/23.
 */
public class AboutActivity extends AppCompatActivity{
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        StatusBarCompat.compat(this);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
