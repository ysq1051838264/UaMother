package com.uamother.bluetooth.main;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.hdr.wristband.BlePresenter;
import com.hdr.wristband.model.BleDevice;
import com.hdr.wristband.utils.BleConst;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.other.DiscreteSeekBar;
import com.uamother.bluetooth.other.SpHelper;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.StatusBarCompat;
import com.uamother.bluetooth.views.PulsatorLayout;
import com.uamother.bluetooth.views.SecondOrderBezier;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BlePresenter.BleView {

//    int OpenPumTimeArray[] = {59, 66, 73, 88, 96, 103, 110, 118, 125, 133, 140}; /* OpenPumTimeArray*5  */
//    int StopPumTimeArray[] = {123, 130, 135, 141, 147, 156, 163, 169, 175, 184, 192};/* StopPumTimeArray*5  */
//    int PWMDutyArray[] = {92, 104, 114, 137, 142, 152, 162, 172, 182, 197, 205};

    //关于
    TextView aboutTv;
    Button saveBtn;

    //吸奶频率，舒适度，亲和力的显示值
    TextView frequencyTv;
    TextView comfortTv;
    TextView affinityTv;

    SecondOrderBezier orderBezier;

    //缺省设定
    TextView[] textViews = new TextView[9];

    BlePresenter blePresenter;

    //吸奶频率，舒适度，亲和力
    DiscreteSeekBar frequencyBar;
    DiscreteSeekBar comfortBar;
    DiscreteSeekBar affinityBar;

    PulsatorLayout pulsator;

    int frequency = 66;
    int comfort = 129;
    int affinity = 103;
    int gradeLevel = 0; //档次

    SpHelper spHelper;

    private long backStartTime;

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - backStartTime > 2000) {
            Toast.makeText(this, "再次点击退出！",Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
        backStartTime = now;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spHelper = SpHelper.initInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        StatusBarCompat.compat(this);

        initData();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            //启动修改蓝牙可见性的Intent
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置蓝牙可见性的时间，方法本身规定最多可见300秒
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivityForResult(intent, 1);
        }

        int g = spHelper.getInt(CacheUtil.SP_KEY_GRADELEVEL, 0);
        if (g != 0) {
            resetRadios(g);
        }

        int f = spHelper.getInt(CacheUtil.SP_KEY_FREQUENCY, 66);
        int c = spHelper.getInt(CacheUtil.SP_KEY_COMFORT, 129);
        int a = spHelper.getInt(CacheUtil.SP_KEY_AFFINITY, 103);

        frequencyBar.setProgress(f);
        comfortBar.setProgress(c);
        affinityBar.setProgress(a);

        blePresenter = new BlePresenter(this);

        blePresenter.init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "用户未开始蓝牙", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        blePresenter.release();
    }

    public void initData() {
        orderBezier = (SecondOrderBezier) findViewById(R.id.orderBezier);

        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();

        saveBtn = (Button) findViewById(R.id.saveBtn);

        frequencyBar = (DiscreteSeekBar) findViewById(R.id.discrete1);
        comfortBar = (DiscreteSeekBar) findViewById(R.id.discrete2);
        affinityBar = (DiscreteSeekBar) findViewById(R.id.discrete3);

        frequencyBar.setOnProgressChangeListener(new mySeekBarListener());
        comfortBar.setOnProgressChangeListener(new mySeekBarListener());
        affinityBar.setOnProgressChangeListener(new mySeekBarListener());


        aboutTv = (TextView) findViewById(R.id.aboutTv);

        frequencyTv = (TextView) findViewById(R.id.frequencyTv);
        comfortTv = (TextView) findViewById(R.id.comfortTv);
        affinityTv = (TextView) findViewById(R.id.affinityTv);

        textViews[0] = (TextView) findViewById(R.id.oneTv);
        textViews[1] = (TextView) findViewById(R.id.twoTv);
        textViews[2] = (TextView) findViewById(R.id.threeTv);
        textViews[3] = (TextView) findViewById(R.id.fourTv);
        textViews[4] = (TextView) findViewById(R.id.fiveTv);
        textViews[5] = (TextView) findViewById(R.id.sixTv);
        textViews[6] = (TextView) findViewById(R.id.sevenTv);
        textViews[7] = (TextView) findViewById(R.id.eightTv);
        textViews[8] = (TextView) findViewById(R.id.nineTv);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/PingFang_Light_0.ttf");
        for (int i = 0; i < 9; i++) {
            textViews[i].setTypeface(typeFace);
        }

        aboutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(MainActivity.this, AboutActivity.class));
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeData(frequency, comfort, affinity,0x0a);
                SharedPreferences.Editor configEditor = spHelper.getConfigEditor();
                configEditor.putInt(CacheUtil.SP_KEY_FREQUENCY, frequency);
                configEditor.putInt(CacheUtil.SP_KEY_COMFORT, comfort);
                configEditor.putInt(CacheUtil.SP_KEY_AFFINITY, affinity);
                configEditor.putInt(CacheUtil.SP_KEY_GRADELEVEL, gradeLevel);
                configEditor.commit();
            }
        });

        for (int i = 0; i < 9; i++) {
            textViews[i].setOnClickListener(this);
        }

      /*  Button read = (Button) findViewById(R.id.readBtn);
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blePresenter.getBleService() != null && blePresenter.getBleService().getWristDecoder() != null)
                    blePresenter.getBleService().getWristDecoder().getSaveValue();
            }
        });*/

    }

    @NotNull
    @Override
    public Context getCtx() {
        return this;
    }

    @Override
    public void newScanDevice(@NotNull BleDevice device) {
    }

    @Override
    public void updateScanDevice(int index, @NotNull BleDevice device) {
    }

    @Override
    public void connectSuccess() {
        Log.i("ysq", "连接成功,关闭蓝牙扫描");
        Toast.makeText(getCtx(), "母婴设备连接成功", Toast.LENGTH_SHORT).show();
        pulsator.stop();
        pulsator.setVisibility(View.GONE);

        if (blePresenter.getBleService() != null && blePresenter.getBleService().getWristDecoder() != null)
            blePresenter.getBleService().getWristDecoder().getSaveValue();
    }

    public void writeData(int frequency, int comfort, int affinity,int flag) {
        if (blePresenter != null && blePresenter.getBleService() != null && blePresenter.getBleService().getWristDecoder() != null)
            blePresenter.getBleService().getWristDecoder().writeData(frequency, comfort, affinity,flag);
        else
            Toast.makeText(this, "未连接蓝牙，请重启设备或者重启app", Toast.LENGTH_SHORT).show();
    }

    public class mySeekBarListener implements DiscreteSeekBar.OnProgressChangeListener {
        @Override
        public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            if (seekBar == frequencyBar) {
                frequencyTv.setText(value + "");
                frequency = value;
            } else if (seekBar == comfortBar) {
                comfortTv.setText(value + "");
                comfort = value;
            } else {
                affinityTv.setText(value + "");
                affinity = value;
            }

            initView();
        }

        @Override
        public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

        }
    }


    public void initView() {
        writeData(frequency, comfort, affinity,1);
        orderBezier.editAuxiliary(frequency, comfort, affinity, gradeLevel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oneTv:
                resetRadios(0);
                break;
            case R.id.twoTv:
                resetRadios(1);
                break;
            case R.id.threeTv:
                resetRadios(2);
                break;
            case R.id.fourTv:
                resetRadios(3);
                break;
            case R.id.fiveTv:
                resetRadios(4);
                break;
            case R.id.sixTv:
                resetRadios(5);
                break;
            case R.id.sevenTv:
                resetRadios(6);
                break;
            case R.id.eightTv:
                resetRadios(7);
                break;
            case R.id.nineTv:
                resetRadios(8);
                break;
            default:
                break;
        }

    }

    /**
     * 背景重置，并把当前选中
     */
    private void resetRadios(int tIndex) {
        gradeLevel = tIndex;
        for (int i = 0; i < 9; i++) {
            textViews[i].setBackgroundResource(R.drawable.shape_circle_background2);
            textViews[i].setTextColor(Color.BLACK);
        }
        textViews[tIndex].setBackgroundResource(R.drawable.shape_circle_background);
        textViews[tIndex].setTextColor(Color.WHITE);

        handlerViewMessage(tIndex);
    }

    int OpenPumTimeArray[] = {59, 66, 73, 88, 95, 102, 111, 118, 125, 132, 139}; /* OpenPumTimeArray*5  */
    int StopPumTimeArray[] = {123, 130, 135, 140, 147, 156, 163, 170, 175, 184, 193};/* StopPumTimeArray*5  */
    int PWMDutyArray[] = {92, 104, 114, 136, 142, 152, 162, 172, 182, 196, 205};

    private void handlerViewMessage(int index) {

        frequencyBar.setMax(OpenPumTimeArray[index + 2]);
        frequencyBar.setMin(OpenPumTimeArray[index]);
        frequencyBar.setProgress(((OpenPumTimeArray[index + 2] + OpenPumTimeArray[index]) / 2));

        comfortBar.setMax(StopPumTimeArray[index + 2]);
        comfortBar.setMin(StopPumTimeArray[index]);
        comfortBar.setProgress(((StopPumTimeArray[index + 2] + StopPumTimeArray[index]) / 2));

        affinityBar.setMax(PWMDutyArray[index + 2]);
        affinityBar.setMin(PWMDutyArray[index]);
        affinityBar.setProgress(((PWMDutyArray[index + 2] + PWMDutyArray[index]) / 2));

        frequencyTv.setText(frequencyBar.getProgress() + "");
        comfortTv.setText(comfortBar.getProgress() + "");
        affinityTv.setText(affinityBar.getProgress() + "");

        frequency = frequencyBar.getProgress();
        comfort = comfortBar.getProgress();
        affinity = affinityBar.getProgress();

        initView();
    }
}
