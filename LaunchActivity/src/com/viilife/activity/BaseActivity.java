package com.viilife.activity;

import android.support.v4.app.FragmentActivity;
import com.umeng.analytics.MobclickAgent;


public class BaseActivity extends FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
