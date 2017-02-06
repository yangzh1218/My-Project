package com.wulian.icam.demo.view;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.videoengine.ViERenderer;

import com.wulian.icam.demo.R;
import com.wulian.routelibrary.ConfigLibrary;
import com.wulian.routelibrary.common.ErrorCode;
import com.wulian.routelibrary.common.RouteApiType;
import com.wulian.routelibrary.common.RouteLibraryParams;
import com.wulian.routelibrary.controller.RouteLibraryController;
import com.wulian.routelibrary.controller.TaskResultListener;
import com.wulian.routelibrary.utils.LibraryLoger;
import com.wulian.routelibrary.utils.MD5;
import com.wulian.siplibrary.api.SipController;
//import com.wulian.siplibrary.api.SipController;
//
//import com.wulian.siplibrary.manage.SipProfile;
import com.wulian.siplibrary.utils.WulianLog;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener,
		TaskResultListener {
	/**
	 * The class include several APIs,include : <br />
	 * 1. API to make a video stream and close a video stream <br />
	 * 2. API to init sip and register sip account;<br />
	 * 3. API to login a user account on the Wulian cloud. <br />
	 * 4. API to get and control the audio stream from the cam. <br />
	 * 5. API to call to get connection monitor to detect when the cam
	 * disconnects / reconnects <br />
	 * 6. API to capture a picture <br />
	 **/

	private EditText et_password;
	private EditText et_username;
	private EditText et_gatewayID;
	private EditText et_gatewayPwd,et_smart_password;

	private Button bt_connect_and_bind,btn_password;
	private Button bt_gatewayID_Login;
	private Button bt_userLogout;
	private Button bt_init;
	private Button bt_registerAccount;
	private Button bt_unregisterAccount;
	private Button bt_devicelist;
	private Button bt_web_login;
	private ListView lv_devices;

	UserInfo mUserInfo;
	private boolean isGatewayLogin = false;
	private boolean isInitSip = false;
	private DeviceAdapter mAdapter;
	private Device mCurrentDevice;
	ArrayList<Device> deviceList;// Global Equipment List
	private static final int SHOW_TOAST = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
		setListener();
	}

	private void initView() {

		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		et_gatewayID = (EditText) findViewById(R.id.et_gatewayID);
		et_gatewayPwd = (EditText) findViewById(R.id.et_gatewayPwd);
		bt_connect_and_bind = (Button) findViewById(R.id.bt_connect_and_bind);
		bt_gatewayID_Login = (Button) findViewById(R.id.bt_gatewayID_Login);
		bt_userLogout = (Button) findViewById(R.id.bt_userLogout);
		bt_init = (Button) findViewById(R.id.bt_init);
		bt_registerAccount = (Button) findViewById(R.id.bt_registerAccount);
		bt_unregisterAccount = (Button) findViewById(R.id.bt_unregisterAccount);
		bt_devicelist = (Button) findViewById(R.id.bt_devicelist);
		bt_web_login = (Button) findViewById(R.id.bt_web_login);
		
		lv_devices = (ListView) findViewById(R.id.lv_devices);
		
		et_smart_password = (EditText) findViewById(R.id.et_smart_password);
		btn_password = (Button) findViewById(R.id.btn_password);
	}

	private void initData() {
		setServerPath();
		WulianLog.setLoger(true);
		LibraryLoger.setLoger(true);
		SipRequestOperation.getInstance().setAppContext(getApplicationContext());
		deviceList = new ArrayList<Device>();
		mAdapter = new DeviceAdapter(this);
		mAdapter.setDeviceSettingClickEvent(this);
		lv_devices.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		//Temporarily comment out the code,otherwise you will fail to Re-register
//		if (isInitSip) {
//			SipRequestOperation.getInstance().unRegisterAccount();
//			SipRequestOperation.getInstance().destorySip();
//		}
		// SipController.getInstance().DestroySip();
		super.onDestroy();
	}

	private void setListener() {
		bt_connect_and_bind.setOnClickListener(this);
		bt_gatewayID_Login.setOnClickListener(this);
		bt_userLogout.setOnClickListener(this);
		bt_registerAccount.setOnClickListener(this);
		bt_init.setOnClickListener(this);
		bt_devicelist.setOnClickListener(this);
		btn_password.setOnClickListener(this);
		bt_web_login.setOnClickListener(this);
		
		bt_unregisterAccount.setOnClickListener(this);

		lv_devices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// We must ensure that the device is online, and the second
				// step: initialization and registration Sip sip account
				// successfully
				mCurrentDevice = deviceList.get(position);
				if (!SipRequestOperation.getInstance().isAccountRegister()) {
					return;
				}
				if (mCurrentDevice.getOnline() == 1) {
					// API to get register state , 200 is ok,the other is wrong.
					int lastCode = SipController.getInstance().getAccountInfo(
							SipRequestOperation.getInstance().getSipProfile());
					if (lastCode == 100) {
						CustomToast.show(MainActivity.this,
								R.string.sip_processing_waiting);
					} else if (lastCode == 401) {
						CustomToast.show(MainActivity.this,
								R.string.sip_unauthorized);
					} else if (lastCode == 404) {
						CustomToast.show(MainActivity.this,
								R.string.sip_offline);
					} else if (lastCode == 407) {
						CustomToast.show(MainActivity.this,
								R.string.sip_proxy_authentication_required);
					} else if (lastCode == 408) {
						CustomToast.show(MainActivity.this,
								R.string.sip_request_timeout);
					} else if (lastCode == 486) {
						CustomToast.show(MainActivity.this,
								R.string.sip_device_busy);
					} else if (lastCode == 503) {
						CustomToast.show(MainActivity.this,
								R.string.sip_service_unavailable);
					} else if (lastCode > 500) {
						CustomToast.show(
								MainActivity.this,
								MainActivity.this.getResources().getString(
										R.string.sip_serve_error)
										+ "(" + lastCode + ")");
					} else if (lastCode != 200) {//
						CustomToast.show(MainActivity.this, "account_info:"
								+ lastCode);
					}

					if (lastCode != 200) {
						return;
					}
					Intent it = new Intent(MainActivity.this,
							PlayVideoActivity.class);
					it.putExtra("Device", mCurrentDevice);
					it.putExtra("UserInfo", mUserInfo);
					startActivity(it);
				} else {
					CustomToast.show(MainActivity.this, "Equipment is offline");
				}
			}
		});
	}

	// To set server path
	private void setServerPath() {
		try {
			ApplicationInfo appInfo = this.getPackageManager()
					.getApplicationInfo(getPackageName(),
							PackageManager.GET_META_DATA);
			String httpsPath = appInfo.metaData.getString("httpsPath");
			// Must set
			// please get it from wulian
			String appName = appInfo.metaData.getString("appName");
			if (TextUtils.isEmpty(appName)) {
				throw new RuntimeException();
			}
			setServerPath(httpsPath, appName);
		} catch (NameNotFoundException e) {
			// do nothing
		}
	}

	private void setServerPath(String httpsUrl, String appName) {
		// Must set
		RouteLibraryController.setLibraryPath(httpsUrl);
		ConfigLibrary.setFirstParamerConfig(getApplicationContext(), appName);
	}

	@Override
	public void OnFail(RouteApiType apiType, ErrorCode code) {
		DataReturn(false, apiType, handleErrorInfo(code));
		switch (code) {
		case INVALID_PHONE:
			break;
		default:
			break;
		}
	}
	
	@Override
	public void OnSuccess(RouteApiType apiType, String json) {
		if (apiType == RouteApiType.USER_LOGIN_WLAIKAN)
		{
			DataReturn(true, apiType, json);
		}else if(apiType == RouteApiType.V3_SMARTROOM_LOGIN)
		{
			DataReturn(true, apiType, json);
		}
		else {
			try {
				JSONObject dataJson = new JSONObject(json);
				int status = dataJson.optInt("status");
				if (status == 1) {
					DataReturn(true, apiType, json);// The first three
													// parameters
													// are required json data
				} else {
					try {
						DataReturn(false, apiType,
								handleErrorInfo(ErrorCode
										.getTypeByCode(dataJson
												.optInt("error_code"))));
					} catch (Exception e) {
						DataReturn(false, apiType,
								handleErrorInfo(ErrorCode.UNKNOWN_ERROR));
					}

				}
			} catch (JSONException e) {
				DataReturn(false, apiType, "JsonException:" + json);// The first
																	// three
																	// arguments
																	// for
																	// the
																	// non-json
																	// format
																	// data
																	// returned
																	// by
																	// the
																	// server
			}
		}
	}

	private void DataReturn(boolean success, RouteApiType apiType, String json) {
		if (success) {
			switch (apiType) {
			case V3_LOGIN: {// Re-login is successful, obtain a new
							// authorization code
				try {
					JSONObject jsonObject = new JSONObject(json);
					if(jsonObject.getInt("status") == 1)
					{
						CustomToast.show(getApplicationContext(), "web login success");
					}
					else
					{
						CustomToast.show(getApplicationContext(), "web login fail!");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mUserInfo = Utils.parseBean(UserInfo.class, json);
				isGatewayLogin = false;
				
				break;
			}
			case USER_LOGIN_WLAIKAN:
				try {

					JSONObject jsonData = new JSONObject(json);
					Utils.sysoInfo("USER_LOGIN_WLAIKAN--jsonData: " + jsonData);
					if (jsonData.isNull("retData")) {
						Message msg = new Message();
						msg.what = SHOW_TOAST;
						msg.obj = "Login fail!";
						mUtilsHandler.sendMessage(msg);
						
					} else {
						mUserInfo = new UserInfo();
						String realData = jsonData.getString("retData");
						mUserInfo = Utils.parseBean(UserInfo.class, realData);
						Message msg = new Message();
						msg.what = SHOW_TOAST;
						msg.obj = "Login Success!";
						isGatewayLogin = true;
						mUtilsHandler.sendMessage(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case V3_SMARTROOM_LOGIN:
				try {
					JSONObject jsonData = new JSONObject(json);
					Utils.sysoInfo("V3_SMARTROOM_LOGIN--jsonData: " + jsonData);
					if(jsonData.getInt("status") != 1)
					{
						//V3接口调用失败，再调用云服务登录接口
						String realServer = "acs.wuliancloud.com:33443";// Real
						// Server
						RouteLibraryController.setWulianAESLibraryPath(realServer);// Just
																					// test
																					// server
						RouteLibraryController.getInstance().doRequest(
								this,
								RouteApiType.USER_LOGIN_WLAIKAN,
								RouteLibraryParams.UserWulianAiKan(et_gatewayID
										.getText().toString().trim(), et_gatewayPwd
										.getText().toString().trim()), this);
					}
					else
					{
						mUserInfo = new UserInfo();
						mUserInfo = Utils.parseBean(UserInfo.class, jsonData);
						Message msg = new Message();
						msg.what = SHOW_TOAST;
						msg.obj = "Login Success!";
						isGatewayLogin = true;
						mUtilsHandler.sendMessage(msg);
					}
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case V3_USER_DEVICES:
				DataReturn2(success, apiType, json);
				deviceList.clear();
				Utils.sysoInfo("V3_USER_DEVICES--jsonData: " + json);
				try {
					JSONArray bindDevices = new JSONObject(json)
							.getJSONArray("owned");
					int len = bindDevices.length();
					Device device = null;
					for (int i = 0; i < len; i++) {
						device = handleDevice(bindDevices.getString(i));
						if (device != null) {
							device.setIs_BindDevice(true);
							checkNickName(device);
							deviceList.add(device);
						}
					}
					bindDevices = new JSONObject(json).getJSONArray("shared");
					len = bindDevices.length();
					for (int i = 0; i < len; i++) {
						device = handleDevice(bindDevices.getString(i));
						if (device != null) {
							device.setIs_BindDevice(false);
							checkNickName(device);
							deviceList.add(device);
						}
					}
					mAdapter.refreshList(deviceList);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case V3_SMARTROOM_PASSWORD:
				
				try {
					JSONObject jsonData = new JSONObject(json);
					if(jsonData.getInt("status") == 1 && jsonData.getInt("result") == 1)
					{
						CustomToast.show(this, "Setting success");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		} else {
			DataReturn2(success, apiType, json);
			switch (apiType) {
			case V3_LOGIN:// Re-login is successful, obtain a new
							// authorization code
				break;
			case V3_USER_DEVICES:
				break;
			case V3_SMARTROOM_LOGIN:
				break;
			default:
				break;
			}
		}
	}

	private Device handleDevice(String deviceJson) {
		Device device = null;
		try {
			device = new Device();
			JSONObject json = new JSONObject(deviceJson);
			device.setDid(json.isNull("did") ? "" : json.getString("did"));
			device.setNick(json.isNull("nick") ? "" : json.getString("nick"));
			device.setDescription(json.isNull("description") ? "" : json
					.getString("description"));
			device.setUpdated(json.isNull("updated") ? 0 : json
					.getLong("updated"));
			device.setOnline(json.isNull("online") ? 1 : json.getInt("online"));
			String location = json.isNull("location") ? "" : json
					.getString("location");
			device.setLocation(location);
			device.setSdomain(json.isNull("sdomain") ? APPConfig.SERVERNAME
					: json.getString("sdomain"));
			device.setProtect(json.isNull("protect") ? 0 : json
					.getInt("protect"));
			device.setShares(json.isNull("shares") ? 0 : json.getInt("shares"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return device;
	}

	public void checkNickName(Device device) {
		String nickName = device.getNick();
		if (TextUtils.isEmpty(nickName)) {
			String deviceId = device.getDid();
			// 采用默认的规则命名
			if (deviceId.toLowerCase(Locale.ENGLISH).startsWith("cmic01")
					|| deviceId.toLowerCase(Locale.ENGLISH)
							.startsWith("cmic04")) {
				nickName = getResources().getString(R.string.main_cmic01name)
						+ deviceId.substring(deviceId.length() - 4);
			} else if (deviceId.toLowerCase(Locale.ENGLISH)
					.startsWith("cmic03")
					|| deviceId.toLowerCase(Locale.ENGLISH)
							.startsWith("cmic05")) {
				nickName = getResources().getString(R.string.main_cmic03name)
						+ deviceId.substring(deviceId.length() - 4);
			} else if (deviceId.toLowerCase(Locale.ENGLISH)
					.startsWith("cmic02")) {
				nickName = getResources().getString(R.string.main_cmic02name)
						+ deviceId.substring(deviceId.length() - 4);
			} else {
				nickName = deviceId.substring(deviceId.length() - 4);
			}
			device.setNick(nickName);
		}
	}

	private void DataReturn2(boolean success, RouteApiType apiType, String json) {
		if (success) {
			switch (apiType) {
			case V3_LOGIN: {// Re-login is successful, obtain a new
							// authorization code
				break;
			}
			default:
				break;
			}
		} else {
			CustomToast.show(this, json);// Parent unified error prompt,
											// subclasses can be further
											// processed
		}
	}

	private String handleErrorInfo(ErrorCode code) {
		String result = "";
		Resources rs = getResources();
		switch (code) {
		case NO_INTERNET:
		case TIMEOUT_ERROR:
		case NETWORK_ERROR:
			result = rs.getString(R.string.no_network);
			break;
		case NO_WIFI:
			result = rs.getString(R.string.connect_wifi_first);
			break;
		case AIRPLANE_MODE:
			result = rs.getString(R.string.exception_flight_mode);
			break;
		case NOSDCARD:
			result = rs.getString(R.string.error_no_sdcard);
			break;
		case FILE_EXIST:
			result = rs.getString(R.string.exception_file_exist);
			break;
		case INVALID_REQUEST:
		case INVALID_IO:
			result = rs.getString(R.string.socket_invalid_io);
			break;
		case UNKNOWN_EXCEPTION:
		case UNKNOWN_ERROR:
			result = rs.getString(R.string.unknown_error);
			break;
		case TOKEN_EXPIRED:
			result = rs.getString(R.string.exception_1001);
			break;
		case INVALIDSTRLENGTH:
			result = rs.getString(R.string.exception_1002);
			break;
		case INVALID_DEVICE_BIND:
			result = rs.getString(R.string.exception_1006);
			break;
		case LIMIT_EXCEEDED:
			result = rs.getString(R.string.exception_1010);
			break;
		case PARAM_MISSING:
			result = rs.getString(R.string.exception_1100);
			break;
		case INVALID_MODEL:
			result = rs.getString(R.string.exception_1102);
			break;
		case INVALID_SOURCE:
			result = rs.getString(R.string.exception_1103);
			break;
		case INVALID_TYPE:
			result = rs.getString(R.string.exception_1104);
			break;
		case INVALID_EMAIL:
			result = rs.getString(R.string.exception_1105);
			break;
		case INVALID_DEVICE_ID:
			result = rs.getString(R.string.exception_1106);
			break;
		case INVALID_URL:
			result = rs.getString(R.string.exception_1108);
			break;
		case INVALID_BINDER_USERNAME:
			result = rs.getString(R.string.exception_1108);
			break;
		case INVALID_USER:
		case INVALID_APPSECRET:
		case INVALID_PHONE:
		case INVALID_CODE:
			result = rs.getString(R.string.exception_1111);
			break;
		case INVALID_LOGIN_AUTH:
			result = rs.getString(R.string.exception_1126);
			break;
		case NO_ROWS_AFFECTED:
			result = rs.getString(R.string.exception_2020);
			break;
		case UNAUTHORIZED_DEVICE:
			result = rs.getString(R.string.exception_2021);
			break;
		default:
			result = rs.getString(R.string.unknown_error);
			break;
		}
		return result;
	}

	// To get device list for a cloud account.
	public void getDeviceList() {
		if (mUserInfo == null) {
			return;
		}
		// Check whether the timeout
		if (mUserInfo != null
				&& System.currentTimeMillis() < mUserInfo.getExpires() * 1000L) {
			RouteLibraryController.getInstance().doRequest(
					MainActivity.this,
					RouteApiType.V3_USER_DEVICES,
					RouteLibraryParams.V3UserDevices(mUserInfo.getAuth(),
							"cmic"), this);
		} else {// Has timed out, re-login
			// TODO ReLogin();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_devicesetting: {
			Device device = deviceList.get((Integer) v.getTag());
			Intent it = new Intent(MainActivity.this,
					DeviceSettingActivity.class);
			it.putExtra("UserInfo", mUserInfo);
			it.putExtra("Device", device);
			startActivity(it);
		}
			break;
		case R.id.bt_connect_and_bind:
			if (mUserInfo != null && !TextUtils.isEmpty(mUserInfo.getAuth())) {
				Intent it = new Intent(MainActivity.this,
						InputDeviceIDActivity.class);
				it.putExtra("UserInfo", mUserInfo);
				startActivity(it);// To connect camera and bind
									// function
			} else {
				Message msg = new Message();
				msg.what = SHOW_TOAST;
				msg.obj = "Please Login with Wulian Account or Login with gateway ID ";
				mUtilsHandler.sendMessage(msg);
			}
			break;
		case R.id.bt_gatewayID_Login:// Gateway ID Login
			try {
				// String realServer = "testdemo.wulian.cc:6009";// Real Server
//				String realServer = "acs.wuliancloud.com:33443";// Real
//				// Server
//				RouteLibraryController.setWulianAESLibraryPath(realServer);// Just
//																			// test
//																			// server
//				RouteLibraryController.getInstance().doRequest(
//						this,
//						RouteApiType.USER_LOGIN_WLAIKAN,
//						RouteLibraryParams.UserWulianAiKan(et_gatewayID
//								.getText().toString().trim(), et_gatewayPwd
//								.getText().toString().trim()), this);
				
				//smartroom 登录，先调用v3接口，登录失败，再调用云服务器接口
				HashMap<String, String> map= RouteLibraryParams.V3SmartRoomLogin("sr-"+ 
				et_gatewayID.getText().toString().trim()
				,et_gatewayPwd.getText().toString().trim(),
						"j4fQ1YVpYG4S4x2ENQ9s8MVe5L9rhEDa" );
				RouteLibraryController.getInstance().doRequest(this,
						RouteApiType.V3_SMARTROOM_LOGIN, map,this);
			} catch (Exception e) {

			}
			break;
		case R.id.bt_web_login:
				String username = et_username.getText().toString().trim();
				String password = et_password.getText().toString().trim();
				RouteLibraryController.getInstance().doRequest(this, 
						RouteApiType.V3_LOGIN, RouteLibraryParams.V3Login(username, password), this);
				break;
				
		case R.id.bt_userLogout:
			if (mUserInfo != null) {
				try {
					RouteLibraryController.getInstance().doRequest(this,
							RouteApiType.V3_LOGOUT,
							RouteLibraryParams.V3Logout(mUserInfo.getAuth()),
							this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Message msg = new Message();
				msg.what = SHOW_TOAST;
				msg.obj = "Please Web Login";
				mUtilsHandler.sendMessage(msg);
			}
			break;
		case R.id.bt_init:// Init sip
			SipRequestOperation.getInstance().initSip();
			// SipController.getInstance().CreateSip(this, false);// It must be
			// the
			// same thread
			isInitSip = true;
			break;
		case R.id.bt_registerAccount:// register a sip account
			// //Login Clound account
			// account = SipController.getInstance().registerAccount(
			// mUserInfo.getSuid(), mUserInfo.getSdomain(),
			// et_password.getText().toString());// It must be the same
			// // thread
			// Login V5 account
			try {
				String gatewayIDPwd = et_gatewayPwd.getText().toString().trim();
				SipRequestOperation.getInstance().registerAccount(
						mUserInfo.getSuid(), mUserInfo.getSdomain(),
						MD5.MD52(gatewayIDPwd).substring(0, 16));// It

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.bt_unregisterAccount:// unregister a sip account
			SipRequestOperation.getInstance().unRegisterAccount();
			break;
		case R.id.bt_devicelist:// get device list
			getDeviceList();
			break;
		case R.id.btn_password: //智能家居v5-修改密码
			String password2 = et_smart_password.getText().toString().trim();
			if(TextUtils.isEmpty(password2))
			{
				CustomToast.show(this, "请输入密码");
				return;
			}else if( mUserInfo == null)
			{
				CustomToast.show(this, "auth null");
				return;
			}
			RouteLibraryController.getInstance().doRequest(this, RouteApiType.V3_SMARTROOM_PASSWORD, RouteLibraryParams.V3SmartroomPassword(mUserInfo.getAuth(), password2), this);
			break;
		default:
			break;
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ViERenderer.FILE_OK:// Picture capture ok
				Bundle bundle = msg.getData();
				Bitmap bitmap = bundle.getParcelable(ViERenderer.GET_PICTURE);
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
					Toast.makeText(getApplicationContext(),
							"Capture picture success", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case ViERenderer.TAKE_PICTURE_FAIL:
				Toast.makeText(getApplicationContext(), "Capture picture fail",
						Toast.LENGTH_SHORT).show();
				break;
			case ViERenderer.FILE_MOUNT_EXCEPTION:
				Toast.makeText(getApplicationContext(),
						"Capture picture mount exception", Toast.LENGTH_SHORT)
						.show();
				break;
			case ViERenderer.FILE_PICTURE_CREATE_EXCEPTION:
				Toast.makeText(getApplicationContext(),
						"Capture picture create exception", Toast.LENGTH_SHORT)
						.show();
				break;
			case ViERenderer.FILE_PICTURE_EXCEPTION:
				Toast.makeText(getApplicationContext(),
						"Capture picture wrong", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	Handler mUtilsHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_TOAST:
				Toast.makeText(getApplicationContext(), msg.obj.toString(),
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};

}
