package com.viilife.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.viilife.R;
import com.viilife.application.ViiLifeApp;
import com.viilife.application.ViiLifeApp.StackListener;
import com.viilife.utils.Config;

public class LoginAcitivity extends BaseActivity {

	private IWXAPI api;

	private AlertDialog dialog;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.login_layout);
		api = WXAPIFactory.createWXAPI(this, Config.WX_APP_ID, true);
		api.registerApp(Config.WX_APP_ID);
		initView();
		ViiLifeApp app = (ViiLifeApp) getApplication();
		app.pushStack(getClass().getName(), new StackListener() {

			@Override
			public void callback() {
				finish();
			}
		});
		app.setToken("");
		SharedPreferences sp_ = getSharedPreferences("login",
				Context.MODE_PRIVATE);
		sp_.edit().putBoolean("login", false).commit();
	}

	private void initView() {
		findViewById(R.id.wx_login).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (check()) {
							wxLogin();
						}
					}
				});
		findViewById(R.id.phone_login).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (check()) {
							onPhoneLogin();
						}
					}
				});
		findViewById(R.id.contact).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent i = new Intent(LoginAcitivity.this,
								ContactActivity.class);
						startActivity(i);
					}
				});
	}

	private boolean check() {
		CheckBox cb = (CheckBox) findViewById(R.id.contact_cb);
		return cb.isChecked();
	}

	private void onPhoneLogin() {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("phone_login", true);
		startActivity(i);
		finish();
	}
	
	private void wxLogin() {
		if (!api.isWXAppInstalled()) {
			Toast.makeText(this, "您未安装微信，请先安装最新版本微信", Toast.LENGTH_LONG).show();
			return;
		}
		if (!api.isWXAppSupportAPI()) {
			Toast.makeText(this, "您安装微信版本过低，请先安装最新版本微信", Toast.LENGTH_LONG)
					.show();
			return;
		}
		SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "wechat_sdk_demo_test";
		req.openId = Config.WX_APP_ID;
		api.sendReq(req);
	}

	private void showExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("是否退出应用");
		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog = null;
			}
		});
		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog = null;
				ViiLifeApp app = (ViiLifeApp) getApplication();
				app.clearStack();
				finish();
				System.exit(0);
			}
		});
		dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		if (dialog == null || !dialog.isShowing()) {
			showExit();
			return;
		}
		super.onBackPressed();
	}
}
