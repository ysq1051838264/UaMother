package com.uamother.bluetooth.ble.lib;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by hdr on 16/3/30.
 */
public class MarshmallowPermission {

    public static final int REQUEST_CODE_PERMISSION_LOCATION = 101;
    private Activity activity;

    public MarshmallowPermission() {
    }

    public void check(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        this.activity = activity;
        if (isDenied(Manifest.permission.ACCESS_FINE_LOCATION) && isDenied(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    boolean isDenied(String permission) {
        int result = ContextCompat.checkSelfPermission(activity, permission);
        return result != PackageManager.PERMISSION_GRANTED;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            boolean allDenied = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    allDenied = false;
                    break;
                }
            }
            if (allDenied) {
                Toast.makeText(activity, "您至少要同意一个权限,在6.0及以上系统才能进行蓝牙连接", Toast.LENGTH_LONG).show();
            }
        }
        this.activity = null;
    }
}
