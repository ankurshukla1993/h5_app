package com.viilife.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.viilife.R;

public class LaunchActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.launch_layout);
		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				onGo();
			}
		}, 2000);
	}

	private void onGo() {
		SharedPreferences sp = getSharedPreferences("index",
				Context.MODE_PRIVATE);
		boolean init = sp.getBoolean("init", false);
		if (!init) {
			findViewById(R.id.logo_layout).setVisibility(View.GONE);
			initPager();
			return;
		}
		startActivity(new Intent(LaunchActivity.this, MainActivity.class));
		finish();
	}

	private void initPager() {
		SharedPreferences sp = getSharedPreferences("index",
				Context.MODE_PRIVATE);
		sp.edit().putBoolean("init", true).commit();
		ViewPager vp = (ViewPager) findViewById(R.id.pager);
		vp.setAdapter(new MyViewPagerAdapter());
	}

	public class MyViewPagerAdapter extends PagerAdapter {

		int[] ids = new int[] { R.drawable.p1, R.drawable.p2, R.drawable.p3 };

		public MyViewPagerAdapter() {
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
			View v = LayoutInflater.from(LaunchActivity.this).inflate(
					R.layout.splash_layout, null);
			ImageView iv = (ImageView) v.findViewById(R.id.img);
			iv.setImageResource(ids[position]);
			container.addView(v);// 添加页卡
			if (position == 2) {
				v.findViewById(R.id.login_).setVisibility(View.VISIBLE);
				v.findViewById(R.id.login_).setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								onGo();
							}
						});
			}
			return v;
		}

		@Override
		public int getCount() {
			return 3;// 返回页卡的数量
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// 官方提示这样写
		}
	}
}
