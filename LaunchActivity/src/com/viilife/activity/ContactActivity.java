package com.viilife.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.viilife.R;
import com.viilife.utils.Config;

public class ContactActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main_layout);
		WebView wv = (WebView) findViewById(R.id.main_web);
		wv.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		
		wv.loadUrl(Config.CONTACT_URL);
	}

}
