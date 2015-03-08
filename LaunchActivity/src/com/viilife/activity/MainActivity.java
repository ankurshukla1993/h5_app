package com.viilife.activity;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.viilife.R;
import com.viilife.application.ViiLifeApp;
import com.viilife.application.ViiLifeApp.StackListener;
import com.viilife.fragment.VideoFragment;
import com.viilife.utils.Config;
import com.viilife.utils.CrashHandler;
import com.viilife.utils.NetworkUtils;
import com.viilife.utils.PageTransfer;

public class MainActivity extends BaseActivity {

	private WebView wView;

	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	private FragmentManager manager;

	private ProgressDialog pd;

	private boolean isPhonelogin = false;
	
	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.main_layout);
		isPhonelogin = getIntent()
				.getBooleanExtra("phone_login", false);
		ViiLifeApp app = (ViiLifeApp) getApplication();
		app.pushStack(getClass().getName(), new StackListener() {
			
			@Override
			public void callback() {
				finish();
			}
		});
		
		manager = getSupportFragmentManager();
		CrashHandler.getInstance().init();
		initView();

		IntentFilter iff = new IntentFilter();
		iff.addAction("ACTION_WEIGHT");
		iff.addAction("ACTION_UPLOAD_IMG");
		iff.addAction("ACTION_WX_LOGIN");
		iff.addAction("ACTION_WX_FINISH");
		iff.addAction("ACTION_FINISH");
		iff.addAction("ACTION_LOGIN");
		iff.addAction("ACTION_LOGIN_SUC");
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, iff);

		boolean refresh = getIntent()
				.getBooleanExtra("wx_login_refresh", false);
		if (!refresh) {
			startLoad(null);
		} else {
			String newUrl = getIntent().getStringExtra("wx_code");
			startLoad(newUrl);
		}
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onDestroy();
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("ACTION_WEIGHT")) {
				double weight = intent.getDoubleExtra("weight", -1);
				String cb = intent.getStringExtra("callback");
				setWeight(weight, cb);
			} else if (action.equals("ACTION_UPLOAD_IMG")) {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				String url = intent.getStringExtra("url");
				String cb = intent.getStringExtra("callback");
				boolean up = intent.getBooleanExtra("upload", false);
				if (up) {
					uploadImg(url, cb);
				} else {
					upImg(url, cb);
				}

			} else if (action.equals("ACTION_WX_LOGIN")) {
				// demo :
				// http://wxvf.mydour.com/#/?code=031ef09e363193510b38c378971f2c6b
				String code = intent.getStringExtra("wx_code");
				String url_ = Config.INDEX_URL + "#/?code=" + code;
				startLoad(url_);
			} else if (action.equals("ACTION_WX_FINISH")) {
				finish();
			} else if (action.equals("ACTION_FINISH")) {
				finish();
			} else if (action.equals("ACTION_LOGIN")) {
				String token = getSharedPreferences("token",
						Context.MODE_PRIVATE).getString("tokenKey", "");
				String url_ = "";
				if (!TextUtils.isEmpty(token)) {
					url_ = Config.INDEX_URL + "#/?token=" + token;
				}
				startLoad(url_);
			} else if (action.equals("ACTION_LOGIN_SUC")) {
				Intent i = new Intent(context, MainActivity.class);
				startActivity(i);
			}
		}
	};

	private void onRunJS(String callback, String params) {
		String js = "javascript:window." + callback + "('" + params + "');";
		wView.loadUrl(js);
		if (Config.DEBUG) {
			Log.i("viilife", js);
		}
	}

	private void upImg(String url, String cb) {
		File f = new File(url);
		if (f.exists()) {
			if (pd == null || !pd.isShowing()) {
				pd = new ProgressDialog(this);
				pd.show();
			}
			ViiLifeApp app = (ViiLifeApp) getApplication();
			NetworkUtils.updateImg(this, app.getToken(), f, cb);
		}
	}

	private void uploadImg(String url, String callback) {
		JSONObject json = new JSONObject();
		try {
			json.put("url", url);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		onRunJS(callback, json.toString());
	}

	private void setWeight(double weight, String callback) {
		JSONObject json = new JSONObject();
		try {
			json.put("weight", weight);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		onRunJS(callback, json.toString());
		onRunJS("isSetScale", 1 + "");
	}

	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void initView() {
		wView = (WebView) findViewById(R.id.main_web);

		wView.getSettings().setJavaScriptEnabled(true);
		wView.getSettings().setUseWideViewPort(false);
		wView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		wView.getSettings().setBuiltInZoomControls(false);
		wView.getSettings().setCacheMode(
				android.webkit.WebSettings.LOAD_NO_CACHE);
		wView.getSettings().setSupportMultipleWindows(true);
		wView.getSettings().setDomStorageEnabled(true);
		wView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		String appCachePath = getApplicationContext().getCacheDir()
				.getAbsolutePath();
		wView.getSettings().setAllowContentAccess(true);
		wView.getSettings().setPluginState(PluginState.ON);
		wView.getSettings().setAppCachePath(appCachePath);
		wView.getSettings().setAllowFileAccess(true);
		wView.getSettings().setAppCacheEnabled(true);
		wView.getSettings().setAllowUniversalAccessFromFileURLs(true);
		wView.setWebChromeClient(chromeClient);
		wView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(final WebView view, String url) {
				// wView.loadUrl("javascript:{alert(window.appFrom)}");
				view.loadUrl("javascript:{window.appFrom='android';}");
				// wView.loadUrl("javascript:{this.app=window.appFrom;alert(window.appFrom);}");
				super.onPageFinished(view, url);
				// showLoading(false);
				if (Config.DEBUG) {
					Log.i("viilife", "finish : " + url);
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// wView.loadUrl("javascript:{alert(window.appFrom)}");
				view.loadUrl("javascript:{window.appFrom='android';}");
				// wView.loadUrl("javascript:{alert(window.appFrom)}");
				super.onPageStarted(view, url, favicon);

				// showLoading(true);
				if (Config.DEBUG) {
					Log.i("viilife", "start : " + url);
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Config.DEBUG) {
					Log.i("viilife", "override : " + url);
					// wView.loadUrl("javascript:{alert(window.appFrom)}");
				}

//				String newUrl = parserUrl(url);
//				if (!TextUtils.isEmpty(newUrl)) {
//					view.loadUrl(newUrl);
//					return true;
//				}

				if (isPhonelogin && url.equals("viilife://login")) {
					return true;
				}
				
				if (!PageTransfer.onParserUrl(MainActivity.this, url)) {
					// if (url.startsWith("viilife://")) {
					// return true;
					// }
					view.loadUrl(url);
				}
				return true;
			}

		});
	}

	private String parserUrl(String url) {
		if (url.startsWith("viilife://")) {
			if (url.contains("loginSuccess")) {
				return "";
			}

			if (url.contains("token=")) {
				int s = url.indexOf("?token");
				String token = url.substring(s, url.length());
				return Config.INDEX_URL + "#/" + token;
			}
		}
		return "";
	}

	private WebChromeClient chromeClient = new WebChromeClient() {

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			showDisplayWindow(view, callback);
		}

		@Override
		public void onHideCustomView() {
			hideDisplayWindow();
		}

	};

	private void showDisplayWindow(View view, CustomViewCallback callback) {
		mCustomViewCallback = callback;
		FragmentTransaction ft = manager.beginTransaction();
		Fragment prev = manager.findFragmentByTag("show_display");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		VideoFragment vf = new VideoFragment();
		vf.setCustomView(view);
		ft.replace(R.id.show_video, vf, "show_display")
				.commitAllowingStateLoss();
	}

	private void hideDisplayWindow() {
		FragmentTransaction ft = manager.beginTransaction();
		Fragment prev = manager.findFragmentByTag("show_display");
		if (prev != null) {
			ft.remove(prev).commitAllowingStateLoss();
		}

		if (manager.getBackStackEntryCount() > 0) {
			manager.popBackStackImmediate();
			return;
		}

		if (mCustomViewCallback != null) {
			mCustomViewCallback.onCustomViewHidden();
		}
	}

	private void showLoading(boolean show) {
		findViewById(R.id.loading).setVisibility(
				show ? View.VISIBLE : View.GONE);
	}

	private void startLoad(String url) {
		wView.loadUrl("javascript:{window.appFrom='android';}");
		// wView.loadUrl("javascript:{alert(window.appFrom)}");
		// wView.loadUrl("javascript:{alert(appFrom)}");
		if (TextUtils.isEmpty(url)) {
			ViiLifeApp app = (ViiLifeApp) getApplication();
			String token = app.getToken();
			if (!TextUtils.isEmpty(token)) {
				url = Config.INDEX_URL + "#/?token=" + token;
			}else {
				url = Config.INDEX_URL;
			}
		} else {
			url = "javascript:{window.location.href='" + url + "';}";
		}
		wView.loadUrl(url);
		wView.loadUrl("javascript:{window.appFrom='android';}");
	}

	@Override
	public void onBackPressed() {
		if (manager.getBackStackEntryCount() > 0) {
			manager.popBackStackImmediate();
			return;
		}
		if (wView.canGoBack()) {
			wView.goBack();
			return;
		}
		SharedPreferences sp = getSharedPreferences("login",
				Context.MODE_PRIVATE);
		boolean login = sp.getBoolean("login", false);
		if (!login) {
			PageTransfer.onParserUrl(this, "viilife://login");
			return;
		}

		super.onBackPressed();
	}

	private void onCheck() {

	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_MENU) {
	// PageTransfer.onParserUrl(this,
	// "viilife://selectPhoto?callback=setUserAvatar");
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	// private void testAppFrom() {
	// wView.loadUrl("javascript:alert(window.appFrom);");
	// }
}
