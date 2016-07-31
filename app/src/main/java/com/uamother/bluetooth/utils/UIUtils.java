package com.uamother.bluetooth.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import java.lang.reflect.Field;

/**
 * Created by ysq on 16/7/27.
 */
public class UIUtils {

    public static float scale;

    public static int densityDpi;

    public static float fontScale;

    public static int screenWidth;
    public static int screenHeight;

    public static void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        scale = dm.density;
        densityDpi = dm.densityDpi;
        fontScale = dm.scaledDensity;
        if (dm.widthPixels < dm.heightPixels) {
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
        } else {
            screenWidth = dm.heightPixels;
            screenHeight = dm.widthPixels;
        }
        Log.e("screen", "屏幕宽度是:" + screenWidth + " 高度是:" + screenHeight + " dp:" + scale + " fontScale:" + fontScale);
    }

    public static void setTextViewDrawableStart(TextView tv, int resId) {

        tv.setCompoundDrawablesRelative(getBitmapDrawable(tv.getContext(), resId), null, null, null);
    }

    public static void setTextViewDrawableTop(TextView tv, int resId) {
        tv.setCompoundDrawablesRelative(null, getBitmapDrawable(tv.getContext(), resId), null, null);
    }

    public static BitmapDrawable getBitmapDrawable(Context context, int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), bitmap);
        bd.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        return bd;
    }

    /**
     * 根据指定的参数绘画一个圆弧
     */
    public static void drawAnnular(Canvas canvas, Paint paint, float x,
                                   float y, int color, float radius, float startAngle, float angle) {
        paint.setColor(color);
        RectF rf1 = new RectF(x - radius, y - radius, x + radius, y + radius);
        canvas.drawArc(rf1, startAngle, angle, false, paint);
    }


    /**
     * 判断两个点是否足够接近
     */
    public static boolean closeEnough(float cx, float cy, float x, float y,
                                      float dis) {
        return x < cx + dis && x > cx - dis && y < cy + dis && y > cy - dis;
    }

    public static int dpToPx(float dp) {
        return (int) (dp * scale + 0.5f);
    }

    public static float spToPx(float sp) {
        return sp * fontScale;
    }

    /**
     * 获取通知栏的高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
}
