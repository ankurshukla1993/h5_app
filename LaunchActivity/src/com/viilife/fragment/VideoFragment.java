package com.viilife.fragment;

import com.viilife.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class VideoFragment extends Fragment {

	private LinearLayout mView; 
	private View mCustomView;
	
	public void setCustomView(View view) {
		mCustomView = view;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mView = (LinearLayout) inflater.inflate(R.layout.show_layout, null);
		LinearLayout pp = (LinearLayout) mView.findViewById(R.id.show_layout);
		if (mCustomView != null) {
			pp.addView(mCustomView);
		}
		return mView;
	}

	@Override
	public void onDestroyView() {
		mView.removeAllViews();
		mCustomView = null;
		super.onDestroyView();
	}

}
