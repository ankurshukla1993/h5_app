package com.viilife.utils;

import com.viilife.activity.CaptureActivity;
import com.viilife.activity.LoginAcitivity;
import com.viilife.activity.MainActivity;
import com.viilife.activity.PairResultActivity;
import com.viilife.activity.ScanDeviceActivity;
import com.viilife.application.ViiLifeApp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class PageTransfer {

	/*
	 * 设置体重秤 viilife://action=setScale&callback=setScaleWeight
	 */
	private final static String ACTION_WEIGHT = "setScale";

	private final static String ACTION_UPLOAD_IMG = "selectPhoto";

	private final static String ACTION_LOGIN = "login";

	private final static String ACTION_TOKEN = "token";

	private final static String ACTION_LOGIN_SUC = "loginSuccess";

	public static boolean onParserUrl(Context context, String url) {
		if (TextUtils.isEmpty(url)) {
			return false;
		}
		if (!url.startsWith("viilife:")) {
			return false;
		}

		if (url.contains(ACTION_LOGIN_SUC)) {
			Log.i("viilife", "ACTION_LOGIN_SUC ----- " + url);
			String newToken = "";
			if (url.contains("&")) {
				String tmp[] = url.split("&");
				for (String str : tmp) {
					if (str.indexOf("token=") != -1) {
						newToken = str.substring(str.indexOf("token=") + 6,
								str.length());
						Log.i("viilife", "Token = " + newToken);
					}
				}
			} else {
				if (url.indexOf("token=") != -1) {
					newToken = url.substring(url.indexOf("token=") + 6,
							url.length());
					Log.i("viilife", newToken);

				}
			}
			String oToken = ((ViiLifeApp) context.getApplicationContext())
					.getToken();
			if (!newToken.equals(oToken)) {
				((ViiLifeApp) context.getApplicationContext())
						.setToken(newToken);
				Intent i = new Intent();
				i.setAction("ACTION_LOGIN_SUC");
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
			}
			return true;
		}

		Intent intent = new Intent();
		if (url.contains(ACTION_WEIGHT)) {
			ViiLifeApp app = (ViiLifeApp) context.getApplicationContext();
			if (app.getInfo() == null) {
				intent.setClass(context, ScanDeviceActivity.class);
			} else {
				intent.setClass(context, PairResultActivity.class);
			}

		} else if (url.contains(ACTION_UPLOAD_IMG)) {
			intent.setClass(context, CaptureActivity.class);
		} else if (url.contains(ACTION_LOGIN)) {
			intent.setClass(context, LoginAcitivity.class);
		} else {
			return false;
		}
		intent.putExtra("callback", parserCallBack(url));
		context.startActivity(intent);
		return true;
	}

	private static String parserCallBack(String url) {
		if (url.contains("callback=")) {
			int s = url.lastIndexOf("callback");
			String tmp = url.substring(s, url.length());
			int ss = tmp.lastIndexOf("=");
			String cb = tmp.substring(ss + 1, tmp.length());
			return cb;
		}
		return "";
	}
}
