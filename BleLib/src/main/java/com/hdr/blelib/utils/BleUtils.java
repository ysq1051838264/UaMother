package com.hdr.blelib.utils;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

public final class BleUtils {
    public static final int STATUS_BLE_ENABLED = 0;
    public static final int STATUS_BLUETOOTH_NOT_AVAILABLE = 1;
    public static final int STATUS_BLE_NOT_AVAILABLE = 2;
    public static final int STATUS_BLUETOOTH_DISABLED = 3;

    private BleUtils() {
    }

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager == null ? null : bluetoothManager.getAdapter();
    }

    public static int getBleStatus(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return STATUS_BLE_NOT_AVAILABLE;
        } else {
            BluetoothAdapter adapter = getBluetoothAdapter(context);
            return adapter == null ? STATUS_BLUETOOTH_NOT_AVAILABLE : (!adapter.isEnabled() ? STATUS_BLUETOOTH_DISABLED : STATUS_BLE_ENABLED);
        }
    }

    public static boolean isEnable(Context context) {
        boolean isEnable = false;
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null
                && (mBluetoothAdapter.isEnabled() || mBluetoothAdapter
                .getState() == BluetoothAdapter.STATE_CONNECTING))
            isEnable = true;
        return isEnable;
    }

}

