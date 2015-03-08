package com.viilife.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.PairCallback;
import com.lifesense.ble.SearchCallback;
import com.lifesense.ble.bean.LsDeviceInfo;
import com.lifesense.ble.bean.SexType;
import com.lifesense.ble.bean.UnitType;
import com.lifesense.ble.bean.WeightUserInfo;
import com.lifesense.ble.commom.BroadcastType;
import com.lifesense.ble.commom.DeviceType;
import com.viilife.R;
import com.viilife.application.ViiLifeApp;

public class ScanDeviceActivity extends BaseActivity {

	private LsBleManager mLsBleManager;

	private TextView status;

	private LsDeviceInfo lsDevice_;

	Handler handler = new Handler();

	private String action = "";

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.scan_device_layout);
		action = getIntent().getStringExtra("callback");
		initBle();
		findViewById(R.id.action_back).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
        //only test
//		handler.postDelayed(new Runnable() {
//
//			@ Override
//			public void run() {
//				Intent i = new Intent("ACTION_WEIGHT");
//				i.putExtra("weight", 68.8);
//				i.putExtra("callback", action);
//				LocalBroadcastManager.getInstance(ScanDeviceActivity.this)
//						.sendBroadcast(i);
//				finish();
//			}
//		}, 3000);
	}

	private void initBle() {
		status = (TextView) findViewById(R.id.scan_status);
		mLsBleManager = LsBleManager.newInstance();
		mLsBleManager.initialize(getApplicationContext());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mLsBleManager.isOpenBluetooth()) {
			showErrorDialog("请打开蓝牙");
			return;
		}
		if (!mLsBleManager.isSupportLowEnergy()) {
			showErrorDialog("您的设备暂不支持");
			return;
		}
		onAddDevices();
	}

	@Override
	public void onBackPressed() {
		mLsBleManager.stopSearch();
		mLsBleManager.stopDataReceiveService();
		mLsBleManager = null;
		super.onBackPressed();
	}

	private SearchCallback mLsScanCallback = new SearchCallback() {
		// 扫描结果
		public void onSearchResults(final LsDeviceInfo lsDevice) {
			// 仅选第一个返回响应的设备
			if (lsDevice != null) {
				if (lsDevice_ == null
						|| !lsDevice.getDeviceName().equals(
								lsDevice_.getDeviceName())) {
					lsDevice_ = lsDevice;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.i("viilife", lsDevice.getDeviceName());
							status.setText(R.string.weight_pairing);
						}
					});
					if (mLsBleManager != null) {
						mLsBleManager.stopSearch();
					}
					startPairDevice();
				}
			}
		}

	};

	private void onAddDevices() {
		mLsBleManager.stopSearch();
		List<DeviceType> typeList = new ArrayList<DeviceType>();
		// typeList.add(DeviceType.SPHYGMOMANOMETER);
		// typeList.add(DeviceType.FAT_SCALE);
		// 仅扫描电子秤
		typeList.add(DeviceType.WEIGHT_SCALE);
		// typeList.add(DeviceType.HEIGHT_RULER);
		// typeList.add(DeviceType.PEDOMETER);

		mLsBleManager.searchLsDevice(mLsScanCallback, typeList,
				BroadcastType.PAIR);
	}

	private void startPairDevice() {
		WeightUserInfo weightUserInfo = new WeightUserInfo();
		weightUserInfo.setAge(28);
		weightUserInfo.setAthleteActivityLevel(2);
		weightUserInfo.setHeight(1.35f);
		weightUserInfo.setSex(SexType.FEMALE);
		weightUserInfo.setAthlete(true);
		weightUserInfo.setProductUserNumber(2);
		weightUserInfo.setUnit(UnitType.UNIT_KG);
		weightUserInfo.setGoalWeight(75);
		weightUserInfo.setWaistline(89);
		mLsBleManager.setProductUserInfo(weightUserInfo);

		LsDeviceInfo lsDevice = new LsDeviceInfo();
		lsDevice.setDeviceName(lsDevice_.getDeviceName());
		lsDevice.setBroadcastID(lsDevice_.getBroadcastID());
		lsDevice.setDeviceType(lsDevice_.getDeviceType());
		lsDevice.setProtocolType(lsDevice_.getProtocolType());
		lsDevice.setModelNumber(lsDevice_.getModelNumber());

		boolean pairB = mLsBleManager.startPairing(lsDevice,
				new PairCallback() {
					// 发现设备用户信息（用户编号、用户名）
					public void onDiscoverUserInfo(final List userList) {
						// 暂时无用
					}

					public void onPairResults(final LsDeviceInfo device,
							final int status) {
						if (status != 0) {
							handler.postDelayed(new Runnable() {

								@Override
								public void run() {
									startPairDevice();
								}
							}, 500);
						} else if (status == 0) {
							handler.postDelayed(new Runnable() {

								@Override
								public void run() {
									((ViiLifeApp) getApplication())
											.setInfo(device);
									Toast.makeText(
											ScanDeviceActivity.this,
											device.getDeviceName() + " pair ok",
											Toast.LENGTH_LONG).show();
									Intent i = new Intent(
											ScanDeviceActivity.this,
											PairResultActivity.class);
									i.putExtra("callback", action);
									startActivity(i);
									finish();
								}
							}, 500);

						}
					}

				});
		if (!pairB) {
			Toast.makeText(this, "pair error", Toast.LENGTH_SHORT).show();
		}
	}

	private void showErrorDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg);
		builder.setPositiveButton("设置",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						startActivity(new Intent(
								Settings.ACTION_SETTINGS));
					}
				});
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
		builder.create().setCancelable(false);
		builder.show();
	}
}
