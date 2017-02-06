/**
 * Project Name:  WulianICamDemo
 * File Name:     InputDeviceIDActivity.java
 * Package Name:  com.wulian.icam.demo.view
 * @Date:         2016年7月25日
 * Copyright (c)  2016, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.wulian.icam.demo.R;
import com.wulian.routelibrary.common.ErrorCode;
import com.wulian.routelibrary.common.RouteApiType;
import com.wulian.routelibrary.common.RouteLibraryParams;
import com.wulian.routelibrary.controller.RouteLibraryController;
import com.wulian.routelibrary.controller.TaskResultListener;
import com.wulian.routelibrary.model.HttpRequestModel;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @ClassName: InputDeviceIDActivity
 * @Function: TODO
 * @Date: 2016年7月25日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class InputDeviceIDActivity extends Activity implements OnClickListener,
		TaskResultListener {
	private EditText et_deviceID;
	private Button btn_confirm;
	private Button btn_qr;
	private Button btn_wifi_direct;
	private EditText et_current_wifi_name;
	private EditText et_current_wifi_pwd;
	private Button bt_config_link;
	private UserInfo mUserInfo;
	private String mDeviceID;
	private String mBindSeed;
	private int mCurrentConfigType;
	private String mCurrentWiFiName;
	private String mCurrentWiFiPwd;
	private String mCurrentWiFIBSSID;
	private boolean isSmartConnect;
	private boolean isQrConnect;
	private boolean isCurrentWiFiInit;
	private DeviceType mDeviceType;
	private ConfigWiFiInfoModel mConfigWiFiInfo;
	private WiFiLinker mWiFiLinker;
	private static final int SHOW_TEXT = 30;
	private static final int SHOW_TOAST = 40;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_device);
		initView();
		initData();
		setListener();
	}

	private void initView() {
		et_deviceID = (EditText) findViewById(R.id.et_deviceID);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		btn_qr = (Button) findViewById(R.id.btn_qr);
		btn_wifi_direct = (Button) findViewById(R.id.btn_wifi_direct);
		et_current_wifi_name = (EditText) findViewById(R.id.et_current_wifi_name);
		et_current_wifi_pwd = (EditText) findViewById(R.id.et_current_wifi_pwd);
		bt_config_link = (Button) findViewById(R.id.bt_config_link);
	}

	private void initData() {
		mUserInfo = (UserInfo) getIntent().getSerializableExtra("UserInfo");
		if (mUserInfo == null) {
			this.finish();
		}
		/** 网络操作初始化 */
		mWiFiLinker = new WiFiLinker();
		mWiFiLinker.WifiInit(this);
		isCurrentWiFiInit = false;
		mConfigWiFiInfo = new ConfigWiFiInfoModel();
	}

	private void setListener() {
		btn_confirm.setOnClickListener(this);
		btn_qr.setOnClickListener(this);
		btn_wifi_direct.setOnClickListener(this);
		bt_config_link.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm:
			String deviceID = et_deviceID.getText().toString().trim();
			if (!TextUtils.isEmpty(deviceID)
					&& (deviceID.startsWith("cmic") || deviceID
							.startsWith("CMIC")) && deviceID.length() == 20) {
				mDeviceID = deviceID;
				startBindingCheck();
			} else {
				Toast.makeText(getApplicationContext(),
						"Please input device ID(20 letters)", Toast.LENGTH_LONG)
						.show();
				return;
			}
			break;
		case R.id.btn_qr:
			if (!isCurrentWiFiInit) {
				Toast.makeText(getApplicationContext(),
						"Please input Valid WiFi info)", Toast.LENGTH_LONG)
						.show();
				return;
			}
			mCurrentConfigType = iCamConstants.CONFIG_BARCODE_WIFI_SETTING;
			mConfigWiFiInfo.setDeviceId(mDeviceID.toLowerCase(Locale
					.getDefault()));
			mConfigWiFiInfo.setAddDevice(true);
			if (true) {
				mConfigWiFiInfo.setSeed(mBindSeed);
			}
			mConfigWiFiInfo.setDeviceType(mDeviceType);
			mConfigWiFiInfo.setQrConnect(isQrConnect);
			mConfigWiFiInfo.setSmartConnect(isSmartConnect);
			mConfigWiFiInfo.setConfigWiFiType(mCurrentConfigType);
			Intent it = new Intent(InputDeviceIDActivity.this,
					BarcodeSettingActivity.class);
			it.putExtra("UserInfo", mUserInfo);
			it.putExtra("ConfigInfo", mConfigWiFiInfo);
			startActivity(it);
			this.finish();
			break;
		case R.id.btn_wifi_direct:
			if (!isCurrentWiFiInit) {
				Toast.makeText(getApplicationContext(),
						"Please input Valid WiFi info)", Toast.LENGTH_LONG)
						.show();
				return;
			}
			mCurrentConfigType = iCamConstants.CONFIG_BARCODE_WIFI_SETTING;

			mConfigWiFiInfo.setDeviceId(mDeviceID.toLowerCase(Locale
					.getDefault()));
			mConfigWiFiInfo.setAddDevice(true);
			if (true) {
				mConfigWiFiInfo.setSeed(mBindSeed);
			}
			mConfigWiFiInfo.setDeviceType(mDeviceType);
			mConfigWiFiInfo.setQrConnect(isQrConnect);
			mConfigWiFiInfo.setSmartConnect(isSmartConnect);
			mConfigWiFiInfo.setConfigWiFiType(mCurrentConfigType);
			Intent intent = new Intent(InputDeviceIDActivity.this,
					CameraConnectWiFiAP.class);
			intent.putExtra("UserInfo", mUserInfo);
			intent.putExtra("ConfigInfo", mConfigWiFiInfo);
			startActivity(intent);
			this.finish();
			break;
		case R.id.bt_config_link:
			okHandler.removeCallbacksAndMessages(null);
			switch (mWiFiLinker.WifiStatus()) {
			case WifiManager.WIFI_STATE_ENABLING:
				Toast.makeText(InputDeviceIDActivity.this, "Wi-Fi is enabling",
						Toast.LENGTH_SHORT).show();
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				handleConfig();// API to associate a camera to existing Wifi AP
				break;
			default:
				Toast.makeText(InputDeviceIDActivity.this,
						"Confirm Wi-Fi is enable", Toast.LENGTH_SHORT).show();
				break;
			}
			break;
		default:
			break;
		}
	}

	private void handleConfig() {
		String wifiSSID = mWiFiLinker.getConnectedWifiSSID().replace("\"", "");
		String pwd = "";
		WifiInfo info = mWiFiLinker.getWifiInfo();
		ScanResult result = null;
		if (!TextUtils.isEmpty(wifiSSID)) {
			List<ScanResult> scanList = mWiFiLinker.WifiGetScanResults();
			for (ScanResult item : scanList) {
				if (item.SSID.equalsIgnoreCase(wifiSSID)) {
					result = item;
					break;
				}
			}
			if (DirectUtils.isAdHoc(result.capabilities)) {
				Toast.makeText(this,
						"AdHoc is not supported by Android platform yet!",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (TextUtils.isEmpty(wifiSSID) || info.getHiddenSSID()) {
				Toast.makeText(this, "Confirm Wi-Fi is not hidden",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (mWiFiLinker.getWifiIpInt() == 0) {
				Toast.makeText(this, "Allocating IP, please wait a moment",
						Toast.LENGTH_LONG).show();
				return;
			}

			et_current_wifi_name.setText(wifiSSID);
			pwd = et_current_wifi_pwd.getText().toString().trim();
			if (DirectUtils.isOpenNetwork(result.capabilities)) {
				mCurrentWiFiPwd = null;
			} else {
				if (TextUtils.isEmpty(pwd)) {
					Toast.makeText(this, "Please input the password",
							Toast.LENGTH_LONG).show();
					return;
				} else {
					mCurrentWiFiPwd = pwd;
				}
			}
			mCurrentWiFiName = wifiSSID;
			mCurrentWiFIBSSID = result.BSSID;
			mConfigWiFiInfo.setWifiName(mCurrentWiFiName);
			mConfigWiFiInfo.setWifiPwd(mCurrentWiFiPwd);
			mConfigWiFiInfo.setBssid(mCurrentWiFIBSSID);
			mConfigWiFiInfo.setSecurity(DirectUtils
					.getStringSecurityByCap(result.capabilities));
			isCurrentWiFiInit = true;
			Toast.makeText(this, "WiFi Info input OK!", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(InputDeviceIDActivity.this,
					"Confirm Wi-Fi is enable", Toast.LENGTH_SHORT).show();
		}
	}
//查询摄像机是否被绑定
	private void startBindingCheck() {
		RouteLibraryController.getInstance().doRequest(this,
				RouteApiType.V3_BIND_CHECK,
				RouteLibraryParams.V3BindCheck( mUserInfo.getAuth(),mDeviceID),
				this);
	}

	private void getDeviceFlag() {
		RouteLibraryController.getInstance().doRequest(this,
				RouteApiType.V3_APP_FLAG,
				RouteLibraryParams.V3AppFlag(mDeviceID, mUserInfo.getAuth()),
				this);
	}

	Handler okHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_TEXT:
				break;
			case SHOW_TOAST:
				String msgTst = msg.obj.toString();
				Toast.makeText(getApplicationContext(), msgTst,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private void handleData(String json) {
		if (!TextUtils.isEmpty(json)) {
			try {
				JSONObject jsonObj = new JSONObject(json);
				// JSONObject flagObj = jsonObj.getJSONObject("flag");
				isQrConnect = false;
				isSmartConnect = false;
				boolean wiredConnect = false;
				boolean apConnect = false;
				int currentConfigType = iCamConstants.CONFIG_UNKNOWN;
				if (!jsonObj.isNull("qr") && jsonObj.getInt("qr") == 1) {
					isQrConnect = true;
				}
				if (!jsonObj.isNull("sc") && jsonObj.getInt("sc") == 1) {
					isSmartConnect = true;
				}
				mDeviceType = DeviceType.getDevivceTypeByDeviceID(mDeviceID);
				switch (mDeviceType) {
				case INDOOR:
					apConnect = true;
					isSmartConnect = false;
					wiredConnect = false;
					break;
				case OUTDOOR:
					isQrConnect = false;
					isSmartConnect = true;
					apConnect = false;
					wiredConnect = true;
					break;
				case SIMPLE:
					if (isSmartConnect) {
						apConnect = false;
					} else {
						apConnect = true;
					}
					wiredConnect = false;
					break;
				case INDOOR2:
				case SIMPLE_N:
					apConnect = false;
					wiredConnect = false;
					isSmartConnect = true;
					break;
				case DESKTOP:
					isQrConnect = false;
					apConnect = false;
					isSmartConnect = true;
					wiredConnect = true;
					break;
				default:
					Message msg = new Message();
					msg.what = SHOW_TOAST;
					msg.obj = "Device is unsupported";
					okHandler.sendMessage(msg);
					break;
				}
				if (isQrConnect) {
					btn_qr.setEnabled(true);
					// currentConfigType =
					// iCamConstants.CONFIG_BARCODE_WIFI_SETTING;
				}
				if (isSmartConnect) {
					btn_wifi_direct.setEnabled(true);
					// currentConfigType =
					// iCamConstants.CONFIG_DIRECT_WIFI_SETTING;
				}

			} catch (JSONException e) {
				Message msg = new Message();
				msg.what = SHOW_TOAST;
				msg.obj = "Something wrong";
				okHandler.sendMessage(msg);
				finish();
			}
		}
	}

	@Override
	public void OnSuccess(RouteApiType apiType, String json) {
		switch (apiType) {
		case V3_BIND_CHECK:
			try {
				JSONObject jsonObject = new JSONObject(json);
				System.out.println("V3_BIND_CHECK json:" + jsonObject);
				String seed = jsonObject.isNull("seed") ? "" : jsonObject
						.getString("seed");
				if (!TextUtils.isEmpty(seed)) {
					String timestamp = jsonObject.isNull("timestamp") ? ""
							: jsonObject.getString("timestamp");
					seed = RouteLibraryParams.getDecodeString(seed, timestamp);
					if (TextUtils.isEmpty(seed)) {
						Message msg = new Message();
						msg.what = SHOW_TOAST;
						msg.obj = "Unknown error";
						okHandler.sendMessage(msg);
					} else {
						mBindSeed = seed;
						getDeviceFlag();
					}
				} else {
					Message msg = new Message();
					msg.what = SHOW_TOAST;
					msg.obj = "Device is binded by the other people";
					okHandler.sendMessage(msg);
				}
			} catch (JSONException e) {
			}
			break;
		case V3_APP_FLAG:
			System.out.println("V3_APP_FLAG json:" + json);
			handleData(json);
			break;
		default:
			break;
		}
	}

	@Override
	public void OnFail(RouteApiType apiType, ErrorCode code) {

	}
}
