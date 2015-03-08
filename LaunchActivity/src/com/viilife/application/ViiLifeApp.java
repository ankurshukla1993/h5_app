package com.viilife.application;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.lifesense.ble.bean.LsDeviceInfo;

public class ViiLifeApp extends Application {

	private LsDeviceInfo info = null;

	public HashMap<String, StackListener> stacks = new HashMap<String, StackListener>();

	public String getToken() {
		SharedPreferences sp = getSharedPreferences("token",
				Context.MODE_PRIVATE);
		String token = sp.getString("tokenkey", "");
		Log.i("viilife", "get Token = " + token);
		return token;
	}

	public void setToken(String token) {
		Log.i("viilife", "Set Token = " + token);
		SharedPreferences sp = getSharedPreferences("token",
				Context.MODE_PRIVATE);
		sp.edit().putString("tokenkey", token).commit();
		
		if (!TextUtils.isEmpty(token)) {
			SharedPreferences sp_ = getSharedPreferences("login",
					Context.MODE_PRIVATE);
			sp_.edit().putBoolean("login", true).commit();
		}
	}

	public static interface StackListener {
		public void callback();
	}

	public LsDeviceInfo getInfo() {
		return info;
	}

	public void setInfo(LsDeviceInfo info) {
		this.info = info;
	}

	public void pushStack(String key, StackListener listener) {
		if (stacks.containsKey(key)) {
			stacks.get(key).callback();
		}
		stacks.put(key, listener);
	}

	public void clearStack() {
		for (String key : stacks.keySet()) {
			stacks.get(key).callback();
		}
		stacks.clear();
	}

}
