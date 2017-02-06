/**
 * Project Name:  WulianICamDemo
 * File Name:     CameraConnectWiFiAP.java
 * Package Name:  com.wulian.icam.demo.view
 * @Date:         2015年12月6日
 * Copyright (c)  2015, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.realtek.simpleconfiglib.SCLibrary;
import com.realtek.simpleconfiglib.wulian.SimpleConfigController;
import com.wulian.icam.demo.R;
import com.wulian.lanlibrary.LanController;
import com.wulian.routelibrary.common.ErrorCode;
import com.wulian.routelibrary.common.RouteApiType;
import com.wulian.routelibrary.common.RouteLibraryParams;
import com.wulian.routelibrary.controller.RouteLibraryController;
import com.wulian.routelibrary.controller.TaskResultListener;
import com.wulian.routelibrary.model.HttpRequestModel;
import com.wulian.routelibrary.utils.MD5;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @ClassName: CameraConnectWiFiAP
 * @Function: API to associate a camera to existing Wifi AP
 * @Date: 2015年12月6日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class CameraConnectWiFiAP extends Activity implements OnClickListener,
		TaskResultListener {
	/**
	 * Notice :before you associate a camera to existing Wifi AP using a mobile
	 * phone ,you need to click 'Set' button twice in the lookever camera.<br />
	 */
	/**
	 * The class include serveral apis,include :<br />
	 * 1. API to associate a camera to existing Wifi AP at the first time;<br />
	 * 2. API to Discover Wulian cams on LAN without a login credentials.<br />
	 * 3. API to Register new user account / Unregister existing user account on
	 * the Wulian cloud.<br />
	 * 4. API to Associate / Unassociate camera to/from existing Wulian user
	 * account.<br />
	 **/

	private static final String TAG = "TestWifiDirect";

	private TextView tv_step1;
	private TextView tv_step2;
	private TextView tv_step3;
	private TextView tv_step4;

	private SimpleConfigController mSimpleConfigController;
	private WiFiLinker mWiFiLinker;
	private ConfigWiFiInfoModel mConfigWiFiInfo;
	private String mDeviceID;
	private UserInfo mUserInfo;
	private CheckBind cb;
	private String DeviceRealIp;
	private static final int MSG_STOP_SCLIB = 1;
	private static final int STOP_MULTICAST = 20;
	private static final int SHOW_TEXT = 30;
	private static final int SHOW_TOAST = 40;
	private static final int BIND_FAIL = 50;
	private static final int BIND_SUCCESS = 60;
	private boolean bStartMultcase = false;
	private boolean bMulcastSuccess = false;
	private boolean isInitRtk = false;
	private static final int BIND_RESULT_MSG = 1;
	private int mCurrentBindNum = 0;
	private int seq = 1;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.wifi_direct);
		initView();
		initData();
		setListener();
	}

	public void onDestroy() {
		super.onDestroy();
		releaseData();
	}

	private void initView() {
		tv_step1 = (TextView) findViewById(R.id.tv_step1);
		tv_step2 = (TextView) findViewById(R.id.tv_step2);
		tv_step3 = (TextView) findViewById(R.id.tv_step3);
		tv_step4 = (TextView) findViewById(R.id.tv_step4);
	}

	private void initData() {// associate a camera to existing Wifi AP ->init
								// data
		mUserInfo = (UserInfo) getIntent().getSerializableExtra("UserInfo");
		mConfigWiFiInfo = getIntent().getParcelableExtra("ConfigInfo");
		if (mUserInfo == null) {
			this.finish();
		}
		if (mConfigWiFiInfo == null) {
			this.finish();
		}
		mSimpleConfigController = new SimpleConfigController();
		/** 网络操作初始化 */
		mWiFiLinker = new WiFiLinker();
		mWiFiLinker.WifiInit(this);

		isInitRtk = false;
		bStartMultcase = false;
		startWifiDirect();
		showStep(1);
	}

	private void releaseData() {
		if (mSimpleConfigController != null && isInitRtk) {
			mSimpleConfigController.DestroyData();
			mSimpleConfigController = null;
		}
	}

	private void showStep(int step) {
		switch (step) {
		case 1:
			tv_step1.setTextColor(getResources().getColor(R.color.green));
			break;
		case 2:
			tv_step2.setTextColor(getResources().getColor(R.color.green));
			break;
		case 3:
			tv_step3.setTextColor(getResources().getColor(R.color.green));
			break;
		case 4:
			tv_step4.setTextColor(getResources().getColor(R.color.green));
			break;
		default:
			break;
		}
	}

	private void BindSeedSet() {
		stopSCLib();
		stopMultcase();
		RouteLibraryController.getInstance().doRequest(
				CameraConnectWiFiAP.this,
				RouteApiType.BindSeedSet,
				RouteLibraryParams.BindSeedSet(DeviceRealIp, mDeviceID,
						mConfigWiFiInfo.getSeed()), this);
	}

	private void setListener() {
	}

	Handler okHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_STOP_SCLIB:
				startMultcast();
				break;
			case STOP_MULTICAST:
				stopSCLib();
				stopMultcase();
				okHandler.sendEmptyMessage(BIND_FAIL);
				break;

			case SHOW_TOAST:
				String msgTst = msg.obj.toString();
				Toast.makeText(getApplicationContext(), msgTst,
						Toast.LENGTH_LONG).show();
				break;
			case BIND_FAIL:
				Toast.makeText(getApplicationContext(), "Bind Fail!",
						Toast.LENGTH_LONG).show();
				break;
			case BIND_SUCCESS:
				Toast.makeText(getApplicationContext(), "Bind Success!",
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};

	private void startWifiDirect() {
		startSCLib();
		okHandler.sendEmptyMessageDelayed(MSG_STOP_SCLIB, 30000);
	}

	private void startSCLib() {
		if (isInitRtk)
			return;
		mSimpleConfigController.initData(this);
		mSimpleConfigController.StartConfig(mConfigWiFiInfo.getWifiName(),
				mConfigWiFiInfo.getWifiPwd(), mConfigWiFiInfo.getBssid());
		isInitRtk = true;
	}

	private void stopSCLib() {
		if (isInitRtk) {
			mSimpleConfigController.stopConfig();
			isInitRtk = false;
		}
	}

	private void startMultcast() {
		String localMac = "";
		WifiInfo wifiInfo = mWiFiLinker.getWifiInfo();
		if (wifiInfo != null) {
			localMac = wifiInfo.getMacAddress();
			if (!TextUtils.isEmpty(localMac)) {
				RouteLibraryController.getInstance().doRequest(
						CameraConnectWiFiAP.this,
						RouteApiType.getAllDeviceInformationByMulticast,
						RouteLibraryParams.getAllDeviceInformation(localMac),
						CameraConnectWiFiAP.this);
				bStartMultcase = true;
				okHandler.sendEmptyMessageDelayed(STOP_MULTICAST, 90000);
			} else {
			}
		} else {
		}
	}

	private void stopMultcase() {
		if (bStartMultcase) {
			LanController.stopRequest();
			bStartMultcase = false;
		}
	}

	private Handler myBindHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BIND_RESULT_MSG:
				if (mCurrentBindNum <= 15) {
					checkBindingState();
					mCurrentBindNum++;
					myBindHandler
							.sendEmptyMessageDelayed(BIND_RESULT_MSG, 6000);
				} else {
					okHandler.sendEmptyMessage(BIND_FAIL);
				}
				break;
			default:
				break;
			}
		};
	};

	private void checkBindingState() {
		RouteLibraryController.getInstance().doRequest(
				this,
				RouteApiType.V3_BIND_RESULT,
				RouteLibraryParams.V3BindResult(mUserInfo.getAuth(),
						mConfigWiFiInfo.getDeviceId()), this);
	}

	@Override
	public void OnSuccess(RouteApiType apiType, final String json) {

		Log.d("PML",
				"Device ID is:" + mDeviceID + ";apiName is:" + apiType.name()
						+ ":" + json);
		switch (apiType) {
		case getAllDeviceInformationByMulticast:
			if (!bStartMultcase) {
				return;
			}
			boolean result = false;
			DeviceRealIp = "";
			List<DeviceDescriptionModel> tempDeviceDesList = XMLHandler
					.getDeviceList(json);
			DeviceDescriptionModel deviceDescription = null;
			for (DeviceDescriptionModel item : tempDeviceDesList) {
				String deviceId = item.getSipaccount();
				int start = deviceId.indexOf("cmic");
				int end = deviceId.indexOf("@");
				deviceId = deviceId.substring(start, end);
				Log.d("PML", "rec deviceId is:" + deviceId);
				if (deviceId.equalsIgnoreCase(mDeviceID)) {
					Log.d("PML", "Set Success");
					result = true;
					bMulcastSuccess = true;
					deviceDescription = item;
					break;
				}
			}
			if (result) {
				DeviceRealIp = deviceDescription.getRemoteIP();
				Message msg = new Message();
				msg.obj = "Setting WIFI set successfully";
				msg.what = SHOW_TOAST;
				okHandler.sendMessage(msg);
				showStep(3);
				BindSeedSet();
			}
			break;
		case V3_BIND_RESULT: {
			try {
				JSONObject jsonObject = new JSONObject(json);
				int bindResult = jsonObject.isNull("result") ? 0 : jsonObject
						.getInt("result");
				if (bindResult == 1) {
					myBindHandler.removeCallbacksAndMessages(null);
					okHandler.sendEmptyMessage(BIND_SUCCESS);
				}
			} catch (JSONException e) {

			}
			break;
		}
		case BindSeedSet:
			showStep(4);
			myBindHandler.sendEmptyMessage(BIND_RESULT_MSG);
			break;
		default:
			break;
		}
	}

	public void OnFail(RouteApiType apiType, ErrorCode code) {
		switch (apiType) {
		case getAllDeviceInformationByMulticast: {
			Message msg = new Message();
			msg.obj = "Get device failed";
			msg.what = SHOW_TOAST;
			okHandler.sendMessage(msg);
			break;
		}
		case V3_BIND_CHECK: {
			Message msg = new Message();
			msg.obj = "Binding equipment failure";
			msg.what = SHOW_TOAST;
			okHandler.sendMessage(msg);
			break;
		}
		case V3_BIND_RESULT: {
			Message msg = new Message();
			msg.obj = "The equipment is bound to fail";
			msg.what = SHOW_TOAST;
			okHandler.sendMessage(msg);
			break;
		}
		case V3_BIND_UNBIND: {
			Message msg = new Message();
			msg.obj = "The device failed to unbind";
			msg.what = SHOW_TOAST;
			okHandler.sendMessage(msg);
			break;
		}
		default:
			break;
		}
	};

	static {
		System.loadLibrary("simpleconfiglib");
	}

	@Override
	public void onClick(View v) {

	}
}
