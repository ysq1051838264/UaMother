package com.uamother.bluetooth.other;

import android.content.Context;
import android.content.SharedPreferences;

public class SpHelper {

    public static final String CONFIG_SP_NAME = "ua_mother_config";
    public static final String PERSISTENT_SP_NAME = "ua_mother_persistent";

    private SharedPreferences configSp;

    private SharedPreferences persistentSp;

    private static SpHelper instance;

    public static SpHelper initInstance(Context context) {
        if (instance == null) {
            instance = new SpHelper(context);
        }
        return instance;
    }

    public static SpHelper getInstance() {
        return instance;
    }

    private SpHelper(Context context) {
        configSp = context.getSharedPreferences(CONFIG_SP_NAME,
                Context.MODE_PRIVATE);

        persistentSp = context.getSharedPreferences(PERSISTENT_SP_NAME,
                Context.MODE_PRIVATE);
    }

    public SharedPreferences.Editor getConfigEditor() {
        return configSp.edit();
    }

    public SharedPreferences.Editor getPersistentEditor() {
        return persistentSp.edit();
    }

    public int getInt(String key, int defaultValue, boolean isPersistent) {
        return isPersistent ? persistentSp.getInt(key, defaultValue) : configSp.getInt(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return getInt(key, defaultValue, false);
    }

    public float getFloat(String key, float defaultValue, boolean isPersistent) {
        return isPersistent ? persistentSp.getFloat(key, defaultValue) : configSp.getFloat(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return getFloat(key, defaultValue, false);
    }

    public long getLong(String key, long defaultValue, boolean isPersistent) {
        return isPersistent ? persistentSp.getLong(key, defaultValue) : configSp.getLong(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return getLong(key, defaultValue, false);
    }


    public boolean getBoolean(String key, boolean defaultValue, boolean isPersistent) {
        return isPersistent ? persistentSp.getBoolean(key, defaultValue) : configSp.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, false);
    }

    public String getString(String key, String defaultValue, boolean isPersistent) {
        return isPersistent ? persistentSp.getString(key, defaultValue) : configSp.getString(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return getString(key, defaultValue, false);
    }

}
