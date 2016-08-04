package com.uamother.bluetooth.utils;

import android.util.Log;

/**
 * Created by ysq on 2016/8/5.
 */
public class LogUtils {
    public static void saveBleLog(Object... objects) {

        String log = getString(objects);
        Log.d("ysq:", log);
    }


    public static String getString(Object[] messages) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : messages) {
            sb.append(obj);
            sb.append(' ');
        }
        return sb.toString();
    }
}
