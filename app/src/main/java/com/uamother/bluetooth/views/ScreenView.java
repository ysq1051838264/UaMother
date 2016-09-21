package com.uamother.bluetooth.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/8/22.
 */
public class ScreenView extends View {

    private float height;
    private float width;

    private int minSin = 90;
    private int maxSin = 274;
    private float a = 1;

    private float mStartPointX = 100;

    public ScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制sin曲线
        Paint sinPaint = new Paint();
        sinPaint.setStyle(Paint.Style.FILL);
        sinPaint.setColor(Color.WHITE);
        sinPaint.setAntiAlias(true);
        sinPaint.setStrokeWidth(2);

        for (int i = (int) (minSin / a); i < maxSin / a; i++) {
            double x = Sin(i);//获取sin值
            double y = Sin(i + 1);
            canvas.drawLine((float) (width - (maxSin / a - i)), (float) x * mStartPointX + 170, (float) (width - (maxSin / a - 1 - i)), (float) y * mStartPointX + 170, sinPaint);
        }
    }

    public double Sin(int i) {
        double result = 0;
        if (a * i >= 270)
            result = Math.sin(270 * Math.PI / 180);
        else
            result = Math.sin(a * i * Math.PI / 180); //这是一个周期

        return result;
//        就拿sin30°为列：Math.sin(30*Math.PI/180)，思路为PI相当于π，而此时的PI在角度值里相当于180°，
// 所以Math.PI/180得到的结果就是1°，然后再乘以30就得到相应的30°。
//        而如果是想用反正弦函数来求相应的对数的话就应该写成：Math.asin(0.5)*(180/Math.PI)，
// 此时的PI相当于圆周率的值，所以180/Math.PI得到的结果就是一弧度的值，然后再乘以0.5就得到相应的弧度。
    }


    public void setmStartPointX(float mStartPointX, float a) {
        this.mStartPointX = mStartPointX;
        this.a = a;
        invalidate();
    }
}