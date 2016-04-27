package com.bc.ting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 共享数据类
 * */
public class ShareUtil {

	public static final String NAME = "";

	/**
	 * 储存数据
	 * */
	public static void setShare(Context context, String info, String key) {

		SharedPreferences preferences = context.getSharedPreferences(NAME,
				Context.MODE_PRIVATE);

		Editor editor = preferences.edit();
		editor.putString(key, info);
		editor.commit();

	}

	/**
	 * 储存数据
	 * */
	public static void setShareToInt(Context context, int info, String key) {

		SharedPreferences preferences = context.getSharedPreferences(NAME,
				Context.MODE_PRIVATE);

		Editor editor = preferences.edit();
		editor.putInt(key, info);
		editor.commit();

	}

	/**
	 * 取出数据
	 * */
	public static String getShare(Context context, String key) {

		SharedPreferences preferences = context.getSharedPreferences(NAME,
				Context.MODE_PRIVATE);

		return preferences.getString(key, NAME);
	}

	/**
	 * 取出数据
	 * */
	public static int getShareToInt(Context context, String key) {

		SharedPreferences preferences = context.getSharedPreferences(NAME,
				Context.MODE_PRIVATE);

		return preferences.getInt(key, 0);
	}

	/**
	 * 清除数据
	 * */
	public static void romoveShare(Context context, String key) {

		SharedPreferences preferences = context.getSharedPreferences(NAME,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.remove(key);
		editor.commit();

	}
}
