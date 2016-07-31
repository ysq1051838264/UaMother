package com.uamother.bluetooth.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.uamother.bluetooth.utils.UIUtils;

/**
 * 二阶贝塞尔曲线
 * Created by ysq on 16/7/21.
 */
public class SecondOrderBezier extends View {

    private Paint mPaintBezier;
    private Paint mPaintAuxiliary;
    private Paint mPaintAuxiliaryText;

    private float mAuxiliaryX;
    private float mAuxiliaryY;

    private float mStartPointX;
    private float mStartPointY;

    private float mEndPointX;
    private float mEndPointY;

    private float height;
    private float width;

    private Path mPath = new Path();

    int OpenPumTimeArray[] = {59, 66, 73, 88, 96, 103, 110, 118, 125, 133, 140}; /* OpenPumTimeArray*5  */
    int StopPumTimeArray[] = {123, 130, 135, 141, 147, 156, 163, 169, 175, 184, 192};/* StopPumTimeArray*5  */
    int PWMDutyArray[] = {92, 104, 114, 137, 142, 152, 162, 172, 182, 197, 205};

    public SecondOrderBezier(Context context) {
        super(context);
    }

    public SecondOrderBezier(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintBezier = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBezier.setStyle(Paint.Style.STROKE);
        mPaintBezier.setStrokeWidth(4);
        mPaintBezier.setColor(Color.WHITE);

        //辅助线
        mPaintAuxiliary = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintAuxiliary.setStyle(Paint.Style.STROKE);
        mPaintAuxiliary.setStrokeWidth(2);

        mPaintAuxiliaryText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintAuxiliaryText.setStyle(Paint.Style.STROKE);
        mPaintAuxiliaryText.setTextSize(20);
    }

    public SecondOrderBezier(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStartPointX = w / 20;

        mStartPointY = h - 20;

        mEndPointX = w / 20 * 19;
        mEndPointY = h - 20;

        height = h - 40;
        width = w / 15;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.reset();
        mPath.moveTo(mStartPointX, mStartPointY);
        // 辅助点
        canvas.drawPoint(mAuxiliaryX, mAuxiliaryY, mPaintAuxiliary);

        // 二阶贝塞尔曲线
        mPath.quadTo(mAuxiliaryX, mAuxiliaryY, mEndPointX, mEndPointY);
        canvas.drawPath(mPath, mPaintBezier);
    }

    public void editAuxiliary(int frequency, int comfort, int affinity, int gradeLevel) {
        int a = OpenPumTimeArray[gradeLevel + 2] - OpenPumTimeArray[gradeLevel];
        int b = StopPumTimeArray[gradeLevel + 2] - StopPumTimeArray[gradeLevel];
        int c = PWMDutyArray[gradeLevel + 2] - PWMDutyArray[gradeLevel];

//        mAuxiliaryX = (mEndPointX - mStartPointX) / (a + b) * (frequency - OpenPumTimeArray[gradeLevel] + comfort - StopPumTimeArray[gradeLevel]);

        mAuxiliaryX = ((mEndPointX - mStartPointX) /2) +
                (((mEndPointX - mStartPointX) /2) /a * (frequency - OpenPumTimeArray[gradeLevel]) - ((mEndPointX - mStartPointX) /2) /a *(comfort - StopPumTimeArray[gradeLevel]));

        mAuxiliaryY = -(height / c * (affinity - PWMDutyArray[gradeLevel]));

        invalidate();
    }

}
