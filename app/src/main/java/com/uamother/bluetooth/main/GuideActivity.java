package com.uamother.bluetooth.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.adapter.GuideAdapter;
import com.uamother.bluetooth.utils.CacheUtil;
import com.uamother.bluetooth.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 标题：首次安装引导界面
 */
public class GuideActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener {

    ViewPager vp; // 滑动类
    View dotZero;
    View dotOne;
    View dotTwo;
    View dotThree;
    Button startBtn;

    // 存放显示的图片id数组
    private int[] imageIds = {
            R.drawable.launcher_one,
            R.drawable.launcher_two,
            R.drawable.launcher_three,
            R.drawable.launcher_four};
    // 将指示器添加到集合中
    private List<View> alView = new ArrayList<View>();
    // 将显示的图片添加到集合
    private List<ImageView> alImageView = new ArrayList<ImageView>();
    // 记录当前的位置
    private int currentIndex;
    private boolean changed = false;
    private GuideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();
        initData();
    }

    private long backStartTime;

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

   /* @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }*/

    private void initView() {
        vp = (ViewPager) findViewById(R.id.vp);
        dotZero = findViewById(R.id.dot_zero);
        dotOne = findViewById(R.id.dot_one);
        dotTwo = findViewById(R.id.dot_two);
        dotThree = findViewById(R.id.dot_three);
        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStart();
            }
        });
    }

    protected void initData() {
        for (int imageId : imageIds) {
            ImageView _image = new ImageView(this);
            _image.setBackgroundResource(imageId);
            alImageView.add(_image);
        }
        alView.add(dotZero);
        alView.add(dotOne);
        alView.add(dotTwo);
        alView.add(dotThree);

        currentIndex = 0;

        adapter = new GuideAdapter(alImageView);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int touchType) {
        switch (touchType) {
            case 0: {
                // 触摸抬手时触发
                if (changed) {
                    changed = false;
                } else {
                    turnTo();
                }
                break;
            }
            case 1:
                // 触摸按下时触发
                changed = false;
                break;
            case 2:
                // 有翻页时抬手时触发
                changed = true;
                break;
        }
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentDotState(position);
    }

    /**
     * 设置当前小点的位置状态
     * 指示器的位置
     */
    private void setCurrentDotState(int position) {
        alView.get(currentIndex).setBackgroundResource(R.drawable.dot_normal);
        alView.get(position).setBackgroundResource(R.drawable.dot_focused);
        currentIndex = position;

        if (currentIndex == 3) {
            startBtn.setVisibility(View.VISIBLE);
        } else {
            startBtn.setVisibility(View.GONE);
        }
    }

    public void onClickStart() {
        turnTo();
    }

    public void turnTo() {
        if (currentIndex == imageIds.length - 1) {
            startActivity(new Intent(this, MainActivity.class));
            CacheUtil.cacheBooleanData(this, Constants.IS_OPEN_GUIDE, false);
            finish();
        }
    }
}
