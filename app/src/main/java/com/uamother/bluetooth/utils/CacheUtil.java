package com.uamother.bluetooth.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CacheUtil {

	public static String SP_KEY_FREQUENCY = "sp_key_frequency";
	public static String SP_KEY_COMFORT = "sp_key_comfort";
	public static String SP_KEY_AFFINITY = "sp_key_affinity";
	public static String SP_KEY_GRADELEVEL = "sp_key_gradelevel";

	private static SharedPreferences sharedPreferences;

	/**
	 * 缓存boolean类型数据
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void cacheBooleanData(Context context,String key,boolean value){
		if(sharedPreferences == null){
			sharedPreferences = context.getSharedPreferences(Constants.APP_CACHE_FILE, Context.MODE_PRIVATE);
		}
		//1.拿到编辑器
		Editor edit = sharedPreferences.edit();
		//2.存数据
		edit.putBoolean(key, value);
		//3.提交
		edit.commit();
	}
	
	/**
	 * 获取缓存文件boolean数据
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean getCacheBooleanData(Context context, String key, boolean defaultValue){
		if(sharedPreferences == null){
			sharedPreferences = context.getSharedPreferences(Constants.APP_CACHE_FILE, Context.MODE_PRIVATE);
		}
		
		return sharedPreferences.getBoolean(key, defaultValue);
	}

}
