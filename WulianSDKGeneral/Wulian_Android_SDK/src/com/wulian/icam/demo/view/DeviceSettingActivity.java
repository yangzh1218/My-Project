/**
 * Project Name:  WulianICamDemo
 * File Name:     DeviceSettingActivity.java
 * Package Name:  com.wulian.icam.demo.view
 * @Date:         2016年7月26日
 * Copyright (c)  2016, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.wulian.icam.demo.R;
import com.wulian.routelibrary.common.ErrorCode;
import com.wulian.routelibrary.common.RouteApiType;
import com.wulian.routelibrary.common.RouteLibraryParams;
import com.wulian.routelibrary.controller.RouteLibraryController;
import com.wulian.routelibrary.controller.TaskResultListener;
import com.wulian.siplibrary.api.SipHandler;
import com.wulian.siplibrary.api.SipMsgApiType;
import com.wulian.siplibrary.manage.SipManager;
import com.wulian.siplibrary.manage.SipMessage;
import com.wulian.siplibrary.manage.SipProfile;
import com.wulian.siplibrary.model.linkagedetection.DetectionAction;
import com.wulian.siplibrary.model.linkagedetection.LinkageDetectionModel;
import com.wulian.siplibrary.model.linkagedetection.TimePeriod;
import com.wulian.siplibrary.model.linkagedetection.WeekModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @ClassName: DeviceSettingActivity
 * @Function: TODO
 * @Date: 2016年7月26日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class DeviceSettingActivity extends Activity implements OnClickListener,
		TaskResultListener, OnCheckedChangeListener {
	private Button btn_unbind_device,btn_password,btn_bell_wake,btn_bell_sleep;
	private Button btn_change_device_name, bt_ConfigMovementDetection,
			bt_ConfigLinkageArming;
	private EditText et_devivemeta,et_password;
	private int seq = 1;
	private UserInfo mUserInfo;
	private Device mDevice;
	private SipProfile account;
	private VersionInfo cmicWebVersionInfo = null;
	private int deviceVersionCode;
	private boolean isClickToUpdateVersion;
	private Dialog mDialog;
	private CheckBox cb_led, cb_voice;
	private String led_on = "1", audio_online = "1",angle = "0";
	private Button btn_share;
	private EditText et_share_username;
	private List<OauthUserDetail> oauthUserDetails;
	 private boolean isAddAuth;// 用于判断是否重发增加授权绑定

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_setting);
		initView();
		initData();
		initWebData();
		setListener();
	}

	private void initView() {
		btn_bell_sleep = (Button) findViewById(R.id.btn_bell_sleep);
		btn_bell_wake = (Button) findViewById(R.id.btn_bell_wake);
		btn_unbind_device = (Button) findViewById(R.id.btn_unbind_device);
		btn_change_device_name = (Button) findViewById(R.id.btn_change_device_name);
		et_devivemeta = (EditText) findViewById(R.id.et_devivemeta);
		bt_ConfigMovementDetection = (Button) findViewById(R.id.bt_ConfigMovementDetection);
		bt_ConfigLinkageArming = (Button) findViewById(R.id.bt_ConfigLinkageArming);
		cb_led = (CheckBox) findViewById(R.id.cb_led);
		cb_voice = (CheckBox) findViewById(R.id.cb_voice);
		et_share_username = (EditText) findViewById(R.id.et_share_username);
		btn_share = (Button) findViewById(R.id.btn_share);
		et_password = (EditText) findViewById(R.id.et_password);
		btn_password = (Button) findViewById(R.id.btn_password);
	}

	private void initData() {
		mUserInfo = (UserInfo) getIntent().getSerializableExtra("UserInfo");
		mDevice = (Device) getIntent().getSerializableExtra("Device");
		if (mUserInfo == null) {
			this.finish();
			return;
		}
		if (mDevice == null) {
			this.finish();
		}
		account = SipRequestOperation.getInstance().getSipProfile();

		String deviceCallUrl = mDevice.getUsername() + "@"
				+ mDevice.getSdomain();
		getOauthListRequest();
	}
	private void initWebData() {
		queryLedAndVoicePromptInfo();
	}
	private void setListener() {
		btn_unbind_device.setOnClickListener(this);
		btn_change_device_name.setOnClickListener(this);
		bt_ConfigMovementDetection.setOnClickListener(this);
		bt_ConfigLinkageArming.setOnClickListener(this);
		btn_share.setOnClickListener(this);
		btn_password.setOnClickListener(this);
		cb_led.setOnCheckedChangeListener(this);
		cb_voice.setOnCheckedChangeListener(this);
		btn_bell_wake.setOnClickListener(this);
		btn_bell_sleep.setOnClickListener(this);
		
	}

	private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SipManager.GET_ACTION_SIP_MESSAGE_RECEIVED())) {
				SipMessage sm = (SipMessage) intent
						.getParcelableExtra("SipMessage");
				Log.d("PML", "sm.getType()-->" + sm.getType());
				if (sm != null) {
					SipMsgApiType apiType = SipHandler.parseXMLData(sm
							.getBody());
					if (sm.getType() == SipMessage.MESSAGE_TYPE_SENT) {
						if (!sm.getContact().equalsIgnoreCase("200")) {
							SipDataReturn(false, apiType, sm.getBody(),
									sm.getFrom(), sm.getTo());
						} else if (apiType == SipMsgApiType.NOTIFY_WEB_ACCOUNT_INFO) {
							SipDataReturn(false, apiType, sm.getBody(),
									sm.getFrom(), sm.getTo());
						}
					} else if (sm.getType() == SipMessage.MESSAGE_TYPE_INBOX) {
						SipDataReturn(true, apiType, sm.getBody(),
								sm.getFrom(), sm.getTo());
					}
				}
			}
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(callStateReceiver,
				new IntentFilter(SipManager.GET_ACTION_SIP_MESSAGE_RECEIVED()));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(callStateReceiver);
	}

	protected void SipDataReturn(boolean isSuccess, SipMsgApiType apiType,
			String xmlData, String from, String to) {
		if (isSuccess) {
			switch (apiType) {
			case CONFIG_MOVEMENT_DETECTION:
				CustomToast.show(getApplicationContext(), "Setting Success");
				break;
			case CONFIG_LINKAGE_ARMING:
				CustomToast.show(getApplicationContext(), "Setting Success");
				break;
			case NOTIFY_FIREWARE_UPDATE:
				CustomToast.show(getApplicationContext(),
						"Already notified to update firmware!");
				break;
			case QUERY_LED_AND_VOICE_PROMPT_INFO:
				led_on = Utils.getParamFromXml(xmlData, "led_on");
				audio_online = Utils.getParamFromXml(xmlData, "audio_online");
				if (!TextUtils.isEmpty(led_on)) {
					if (led_on.equals("0")) {
						cb_led.setChecked(false);
					} else {
						cb_led.setChecked(true);
					}
				}
				if (!TextUtils.isEmpty(audio_online)) {
					if (audio_online.equals("0")) {
						cb_voice.setChecked(false);
					} else {
						cb_voice.setChecked(true);
					}
				}
				break;
			case CONFIG_LED_AND_VOICE_PROMPT:
				CustomToast.show(getApplicationContext(), "Setting Success");
				break;

			}
		}
		if (!isSuccess) {
			// CustomToast.show(this, "设备离线,sip消息发送失败");
			switch (apiType) {
			case NOTIFY_FIREWARE_UPDATE:
				CustomToast.show(getApplicationContext(), "Failed upgrade");
				break;
			case CONFIG_LED_AND_VOICE_PROMPT:
				CustomToast.show(getApplicationContext(), "Setting failed");
				break;
//			case CONTROL_PRESET_NEW:
//				CustomToast.show(getApplicationContext(), "Setting failed");
//				break;
//			case CONTROL_DELETE_PRESET_NEW:
//				CustomToast.show(getApplicationContext(), "Setting failed");
//				break;
//			case CONTROL_LOCATE_PRESET:
//				CustomToast.show(getApplicationContext(), "Setting failed");
//				break;
			}
		}
	}

	@Override
	public void OnSuccess(RouteApiType apiType, String json) {
		switch (apiType) {
		case V3_USER_DEVICE:
			int UserDeviceResult = 0;
			try {

				JSONObject jsonData = new JSONObject(json);
				if (!jsonData.isNull("status")) {
					int status = jsonData.getInt("status");
					if (status == 1) {
						if (!jsonData.isNull("result")) {
							UserDeviceResult = jsonData.getInt("result");
						}
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (UserDeviceResult == 1) {
				Toast.makeText(DeviceSettingActivity.this,
						"Change Device Nick  Success!", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(DeviceSettingActivity.this,
						"Change Device Nick  Fail!!!!!!!!!!", Toast.LENGTH_LONG)
						.show();
			}

			break;
		case V3_BIND_UNBIND:
			int unBindDeviceResult = 0;
			try {

				JSONObject jsonData = new JSONObject(json);
				if (!jsonData.isNull("status")) {
					int status = jsonData.getInt("status");
					if (status == 1) {
						if (!jsonData.isNull("result")) {
							unBindDeviceResult = jsonData.getInt("result");
						}
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (unBindDeviceResult == 1) {
				Toast.makeText(DeviceSettingActivity.this,
						"UnBind Device   Success!!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(DeviceSettingActivity.this,
						"UnBind Device   Fail!!!!!!!!!!", Toast.LENGTH_LONG)
						.show();
			}
			break;
		case V3_SHARE:
			RouteLibraryController.getInstance().doRequest(this, RouteApiType.V2_NOTICE, RouteLibraryParams.V2Notice(mDevice.getDid(), "add", "sr"+ et_share_username.getText().toString(), mUserInfo.getAuth(), null), this);
			break;
		case V2_NOTICE:
			if(isAddAuth)
			{
				CustomToast.show(getApplicationContext(), "分享邀请已发送");
			}
			break;
		case V3_SHARE_LIST:
			oauthUserDetails = JsonHandler.getBindingOauthDetailList(json);
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
	}

	@Override
	public void OnFail(RouteApiType apiType, ErrorCode code) {
		switch (apiType) {
		case V3_USER_DEVICE:
			Toast.makeText(DeviceSettingActivity.this,
					"Change Device Nick Fail!", Toast.LENGTH_LONG).show();
			break;
		case V3_BIND_UNBIND:
			Toast.makeText(DeviceSettingActivity.this, "UnBind Device   Fail!",
					Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_change_device_name:
			String nick = et_devivemeta.getText().toString().trim();
			if (TextUtils.isEmpty(nick)) {
				Toast.makeText(DeviceSettingActivity.this,
						"Please input device name !", Toast.LENGTH_LONG).show();
			} else {
				RouteLibraryController.getInstance().doRequest(
						this,
						RouteApiType.V3_USER_DEVICE,
						RouteLibraryParams.V3UserDevice(mUserInfo.getAuth(),
								mDevice.getDid(), nick, ""), this);
			}
			break;
		case R.id.btn_unbind_device:
			RouteLibraryController.getInstance().doRequest(
					this,
					RouteApiType.V3_BIND_UNBIND,
					RouteLibraryParams.V3BindUnbind(mUserInfo.getAuth(),
							mDevice.getDid()), this);
			break;
		case R.id.bt_ConfigMovementDetection: {
			// 配置运动侦测
			String sip_ok = "sip:" + mDevice.getUsername() + "@"
					+ mDevice.getSdomain();
			String[] areas = new String[2];
			areas[0] = "0,0,120,120";
			areas[1] = "50,50,100,100";
			SipRequestOperation.getInstance().sendMessage(
					sip_ok.replace("sip:", ""),
					SipHandler.ConfigMovementDetection(sip_ok, seq++, true, 50,
							areas), account);
		}
			break;
		case R.id.bt_ConfigLinkageArming: {
			// 联动布防设置
			String sip_ok = "sip:" + mDevice.getUsername() + "@"
					+ mDevice.getSdomain();
			LinkageDetectionModel detections = new LinkageDetectionModel();
			detections.setUse(true);
			for (int i = 0; i < 7; i++) {
				DetectionAction da = new DetectionAction();
				if (i == 1)
					da.setWeekModel(WeekModel.MONDAY);
				else if (i == 2)
					da.setWeekModel(WeekModel.TUESDAY);
				else if (i == 3)
					da.setWeekModel(WeekModel.WEDNESDAY);
				else if (i == 4)
					da.setWeekModel(WeekModel.THURSDAY);
				else if (i == 5)
					da.setWeekModel(WeekModel.FRIDAY);
				else if (i == 6)
					da.setWeekModel(WeekModel.SATURDAY);
				else if (i == 0)
					da.setWeekModel(WeekModel.SUNDAY);
				TimePeriod tp = new TimePeriod();
				tp.setId(0);
				tp.setStartTime("00:00");
				tp.setEndTime("08:02");
				da.addLinkageDetection(tp);

				TimePeriod tp1 = new TimePeriod();
				tp1.setId(1);
				tp1.setStartTime("11:00");
				tp1.setEndTime("20:59");
				da.addLinkageDetection(tp1);

				TimePeriod tp2 = new TimePeriod();
				tp2.setId(2);
				tp2.setStartTime("21:00");
				tp2.setEndTime("23:59");
				da.addLinkageDetection(tp2);

				detections.addDetectionAction(i, da);
			}
			SipRequestOperation.getInstance().sendMessage(
					sip_ok.replace("sip:", ""),
					SipHandler.ConfigLinkageArming(sip_ok, seq++, 1, 1,
							detections), account);
		}
			break;
		case R.id.btn_share:
			if(mDevice.getIs_BindDevice())
			{
				
				addOauth();
			}
			else
			{
				CustomToast.show(getApplicationContext(), "非主绑定，无法分享");
				return;
			}
			break;
		case R.id.btn_password:
			String password = et_password.getText().toString().trim();
			if(TextUtils.isEmpty(password))
			{
				CustomToast.show(this, "请输入密码");
				return;
			}
			RouteLibraryController.getInstance().doRequest(this, RouteApiType.V3_SMARTROOM_PASSWORD, RouteLibraryParams.V3SmartroomPassword(mUserInfo.getAuth(), password), this);
		case R.id.btn_bell_wake:
			RouteLibraryController.getInstance().doRequest(this, RouteApiType.V3_APP_BELL, RouteLibraryParams.V3AppBell(mUserInfo.getAuth(), mDevice.getDid(), "awake"), this);
			break;
		case R.id.btn_bell_sleep:
			RouteLibraryController.getInstance().doRequest(this, RouteApiType.V3_APP_BELL, RouteLibraryParams.V3AppBell(mUserInfo.getAuth(), mDevice.getDid(), "sleep"), this);
			break;
		default:
			break;
		}
	}

	public void addOauth() {
		String uuid = et_share_username.getText().toString().trim();
		if (TextUtils.isEmpty(uuid)) {
			CustomToast.show(getApplicationContext(), "授权用户名为空");
			return;
		}
		if (uuid.equalsIgnoreCase(mUserInfo.getEmail())
				|| uuid.equalsIgnoreCase(mUserInfo.getPhone())
				|| uuid.equalsIgnoreCase(mUserInfo.getUsername())) {
			CustomToast.show(getApplicationContext(), "不能分享给自己");
			return;
		}
		if(isBinded(uuid))
		{
			CustomToast.show(getApplicationContext(), "该账户已经授权过");
			return;
		}
		isAddAuth = true;
		if(uuid.length() == 12)
		{
			
			addOauthRequest("sr-" + uuid);
		}
		else
		{
			addOauthRequest(uuid);
		}
	}

	private boolean isBinded(String account) {
		for (OauthUserDetail m : oauthUserDetails) {
			if (m.getUsername().equalsIgnoreCase(account)
					|| m.getPhone().equalsIgnoreCase(account)
					|| m.getEmail().equalsIgnoreCase(account)) {
				return true;
			}
		}
		return false;
	}
	// Queries LED and voice prompt
		private void queryLedAndVoicePromptInfo() {
			SipRequestOperation.getInstance().sendMessage(
					mDevice.getUsername() + "@" + mDevice.getSdomain(),
					SipHandler.QueryLedAndVoicePromptInfo(
							"sip:" + mDevice.getUsername() + "@"
									+ mDevice.getSdomain(), seq++), account);
		}

		// Set LED and voice prompt
		/**
		 * @MethodName: configLedAndVoicePrompt
		 * @Function: Set LED and voice prompt
		 * @author yangzhou
		 * @email zhou.yang@wuliangroup.com
		 * @param led_on
		 *            0 : Close LED  1 ：Open LED
		 * @param audio_online
		 *            On-line reminder 0 ：关闭            1 : 开启
		 * @param angle 0: normal    1:invert
		 */
		private void configLedAndVoicePrompt(String led_on, String audio_online,String angle) {
			SipRequestOperation.getInstance().sendMessage(
					mDevice.getUsername() + "@" + mDevice.getSdomain(),
					SipHandler.ConfigLedAndVoicePrompt(
							"sip:" + mDevice.getUsername() + "@"
									+ mDevice.getSdomain(), seq++, led_on,
							audio_online,"0"), account);
		}
	private void getOauthListRequest() {
		RouteLibraryController.getInstance().doRequest(
				this,
				RouteApiType.V3_SHARE_LIST,
				RouteLibraryParams.V3ShareList(mUserInfo.getAuth(),
						mDevice.getDid()), this);
	}

	private void addOauthRequest(String username) {
		RouteLibraryController.getInstance().doRequest(
				this,
				RouteApiType.V3_SHARE,
				RouteLibraryParams.V3Share(mUserInfo.getAuth(),
						mDevice.getDid(),
						username.toLowerCase(Locale.getDefault())), this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		int id = buttonView.getId();
		if (!buttonView.isPressed()) {
			return;
		}
		if (id == R.id.cb_led) {
			if (isChecked) {
				led_on = "1";
			} else {
				led_on = "0";
			}
			configLedAndVoicePrompt(led_on, audio_online,angle);
		}
		if (id == R.id.cb_voice) {
			if (isChecked) {
				audio_online = "1";
			} else {
				audio_online = "0";
			}
			configLedAndVoicePrompt(led_on, audio_online,angle);
		}
	}
}
