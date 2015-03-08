package com.viilife.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.viilife.R;
import com.viilife.activity.BaseActivity;
import com.viilife.activity.MainActivity;
import com.viilife.utils.Config;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

	
	private IWXAPI api;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main_layout);
		api  = WXAPIFactory.createWXAPI(this, Config.WX_APP_ID, false);
		api.handleIntent(getIntent(), this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		api.handleIntent(getIntent(), this);
	}

	@Override
	public void onReq(BaseReq req) {

	}

	@Override
	public void onResp(BaseResp resp) {
		if (resp instanceof SendAuth.Resp && resp.errCode == BaseResp.ErrCode.ERR_OK) {
			SendAuth.Resp sr = (SendAuth.Resp) resp;
			final StringBuffer msg = new StringBuffer();
			msg.append(sr.code);
			Toast.makeText(WXEntryActivity.this, msg.toString(), Toast.LENGTH_LONG).show();
			Log.i("viilife", msg.toString());
			Intent intent = new Intent();
			intent.setAction("ACTION_WX_FINISH");
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			
			intent = new Intent(this, MainActivity.class);
			String url_ = Config.INDEX_URL + "#/?code=" + sr.code
					+ "";
			intent.putExtra("wx_login_refresh", true);
			intent.putExtra("wx_code", url_);
			startActivity(intent);
		}else {
			Toast.makeText(WXEntryActivity.this, resp.errStr, Toast.LENGTH_LONG).show();
		}
		finish();
	}

}
