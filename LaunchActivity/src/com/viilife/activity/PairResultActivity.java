package com.viilife.activity;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.ReceiveDataCallback;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.WeightData_A2;
import com.lifesense.ble.bean.WeightData_A3;
import com.lifesense.ble.bean.WeightUserInfo;
import com.viilife.R;
import com.viilife.application.ViiLifeApp;
import com.viilife.utils.Config;

public class PairResultActivity extends BaseActivity {

	private LsBleManager mLsBleManager;

	private ArrayList<WeightData_A2> weightList = new ArrayList<WeightData_A2>();
	private ArrayList<WeightData_A3> weightList3 = new ArrayList<WeightData_A3>();

	private boolean isReceived = false;

	private boolean startReceive = false;

	private String action = "";

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			if (what == 9527) {
				sortWeights();
			} else if (what == 9528) {
				sortWeights();
			}
		}

	};

	private long startTime = System.currentTimeMillis();

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.pair_resule_layout);
		action = getIntent().getStringExtra("callback");
		mLsBleManager = LsBleManager.newInstance();
		mLsBleManager.initialize(getApplicationContext());
		final LsDeviceInfo info = ((ViiLifeApp) getApplication()).getInfo();
		if (info != null) {
			mLsBleManager.addMeasureDevice(info);
			onReceiveData();
		}
		findViewById(R.id.action_back).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		findViewById(R.id.action_unpair).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mLsBleManager.stopDataReceiveService();
						if (info != null) {
							mLsBleManager.deleteMeasureDevice(info
									.getBroadcastID());
							((ViiLifeApp) getApplication()).setInfo(null);
						}
						finish();
					}
				});
        findViewById(R.id.save_btn).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
	}

	private void catchData(double weight) {
		if (isFinishing()) {
			return;
		}
		mLsBleManager.stopDataReceiveService();
		BigDecimal bd = new BigDecimal(weight);
		final double weight_ = bd.setScale(1, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("您本次称重为:" + weight_ + "公斤");
		builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent i = new Intent("ACTION_WEIGHT");
				i.putExtra("weight", weight_);
				i.putExtra("callback", action);
				LocalBroadcastManager.getInstance(PairResultActivity.this)
						.sendBroadcast(i);
				finish();
			}
		});
		builder.create().show();
	}

	private void fetchWeights(WeightData_A2 data) {
		weightList.add(data);
	}

	private void fetchWeights(WeightData_A3 data) {
		weightList3.add(data);
	}

	private void sortWeights() {
		double weight = 0;
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		if (!weightList.isEmpty()) {
			Collections.sort(weightList, new Comparator<WeightData_A2>() {

				@Override
				public int compare(WeightData_A2 a, WeightData_A2 b) {
					try {
						long at = sdf.parse(a.getDate()).getTime();
						long bt = sdf.parse(b.getDate()).getTime();
						return at >= bt ? 1 : -1;
					} catch (Exception e) {
					}
					return 0;

				}
			});
			weight = weightList.get(0).getWeight();
		} else if (!weightList3.isEmpty()) {
			Collections.sort(weightList3, new Comparator<WeightData_A3>() {

				@Override
				public int compare(WeightData_A3 a, WeightData_A3 b) {
					try {
						long at = sdf.parse(a.getDate()).getTime();
						long bt = sdf.parse(b.getDate()).getTime();
						return at >= bt ? 1 : -1;
					} catch (Exception e) {
					}
					return 0;

				}
			});
			weight = weightList3.get(0).getWeight();
		}

		catchData(weight);
	}

	private boolean checkData(String data_) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());
		try {
			long wd = sdf.parse(data_).getTime();
			if (Config.DEBUG) {
				Log.i("viilife", startTime + "--" + wd);
			}
			return wd >= startTime;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return true;
	}

	private void onReceiveData() {
		mLsBleManager.stopDataReceiveService();
		mLsBleManager.startDataReceiveService(new ReceiveDataCallback() {
			public void onReceiveWeightDta_A2(final WeightData_A2 data) {
				if (Config.DEBUG) {
					Log.i("viilife", data.getWeight() + " -- " + data.getDate());
				}
				if (!checkData(data.getDate())) {
					return;
				}
				if (!startReceive) {
					startReceive = true;
					// handler.sendEmptyMessageDelayed(9527, 30000);
				}
				if (startReceive) {
					if (handler.hasMessages(9528)) {
						handler.removeMessages(9528);
					}
					handler.sendEmptyMessageDelayed(9528, 5000);
				}
				fetchWeights(data);
				// if (!isReceived) {
				// isReceived = true;
				// catchData(data.getWeight());
				// }
			}

			public void onReceiveWeightData_A3(final WeightData_A3 data) {
				if (Config.DEBUG) {
					Log.i("viilife", data.toString());
				}

				if (!isReceived) {
					isReceived = true;
					catchData(data.getWeight());
				}
			}

			public void onReceiveUserInfo(WeightUserInfo proUserInfo) {
			}
		});
	}

	@Override
	public void onBackPressed() {
		mLsBleManager.stopDataReceiveService();
		super.onBackPressed();
	}

}
