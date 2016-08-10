package com.uamother.bluetooth.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.hdr.wristband.BlePresenter;
import com.hdr.wristband.model.BleDevice;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.other.DiscreteSeekBar;
import com.uamother.bluetooth.utils.StatusBarCompat;
import com.uamother.bluetooth.views.PulsatorLayout;
import com.uamother.bluetooth.views.SecondOrderBezier;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BlePresenter.BleView {

    int OpenPumTimeArray[] = {59, 66, 73, 88, 96, 103, 110, 118, 125, 133, 140}; /* OpenPumTimeArray*5  */
    int StopPumTimeArray[] = {123, 130, 135, 141, 147, 156, 163, 169, 175, 184, 192};/* StopPumTimeArray*5  */
    int PWMDutyArray[] = {92, 104, 114, 137, 142, 152, 162, 172, 182, 197, 205};

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
    int comfort = 130;
    int affinity = 104;
    int gradeLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        StatusBarCompat.compat(this);

        initData();

        blePresenter = new BlePresenter(this);

        blePresenter.init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        blePresenter.release();
    }

    public void initData() {
        orderBezier = (SecondOrderBezier) findViewById(R.id.orderBezier);

        pulsator= (PulsatorLayout) findViewById(R.id.pulsator);
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

                if (blePresenter.getBleService() != null && blePresenter.getBleService().getWristDecoder() != null)
                    blePresenter.getBleService().getWristDecoder().getSaveValue();
            }
        });

        for (int i = 0; i < 9; i++) {
            textViews[i].setOnClickListener(this);
        }

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


    private void handlerViewMessage(int index) {

        frequencyBar.setMax(OpenPumTimeArray[index + 2]);
        frequencyBar.setMin(OpenPumTimeArray[index]);
        frequencyBar.setProgress(OpenPumTimeArray[index + 1]);

        comfortBar.setMax(StopPumTimeArray[index + 2]);
        comfortBar.setMin(StopPumTimeArray[index]);
        comfortBar.setProgress(StopPumTimeArray[index + 1]);

        affinityBar.setMax(PWMDutyArray[index + 2]);
        affinityBar.setMin(PWMDutyArray[index]);
        affinityBar.setProgress(PWMDutyArray[index + 1]);

        frequencyTv.setText(frequencyBar.getProgress() + "");
        comfortTv.setText(comfortBar.getProgress() + "");
        affinityTv.setText(affinityBar.getProgress() + "");

        frequency = OpenPumTimeArray[index + 1];
        comfort = StopPumTimeArray[index + 1];
        affinity = PWMDutyArray[index + 1];

        initView();
    }
}
