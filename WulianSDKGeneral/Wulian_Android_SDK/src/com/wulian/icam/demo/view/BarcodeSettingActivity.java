/**
 * Project Name:  WulianICamDemo
 * File Name:     BarcodeSettingActivity.java
 * Package Name:  com.wulian.icam.demo.view
 * @Date:         2016年7月25日
 * Copyright (c)  2016, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wulian.icam.demo.R;
import com.wulian.routelibrary.common.ErrorCode;
import com.wulian.routelibrary.common.RouteApiType;
import com.wulian.routelibrary.common.RouteLibraryParams;
import com.wulian.routelibrary.controller.RouteLibraryController;
import com.wulian.routelibrary.controller.TaskResultListener;

/**
 * @ClassName: BarcodeSettingActivity
 * @Function: TODO
 * @Date: 2016年7月25日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class BarcodeSettingActivity extends Activity implements
		OnClickListener, TaskResultListener {
	private int QrWidth;

	private LinearLayout ll_barcode;
	private ImageView iv_barcode;
	private Button btn_hear_voice;
	private String wifi_info;
	private UserInfo mUserInfo;
	private int mCurrentNum = 0;
	private ConfigWiFiInfoModel mConfigWiFiInfo;
	private static final long START_DELAY = 1000;
	private static final int BIND_RESULT_MSG = 1;
	private static final int BIND_RESULT_MSG_FAIL = 2;
	private static final int BIND_RESULT_MSG_SUCCESS = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_setting);
		initView();
		initData();
		setListener();
	}

	private void initView() {
		ll_barcode = (LinearLayout) findViewById(R.id.ll_barcode);
		iv_barcode = (ImageView) findViewById(R.id.iv_barcode);
		btn_hear_voice = (Button) findViewById(R.id.btn_hear_voice);
	}

	private void initData() {
		mUserInfo = (UserInfo) getIntent().getSerializableExtra("UserInfo");
		mConfigWiFiInfo = (ConfigWiFiInfoModel) getIntent().getParcelableExtra(
				"ConfigInfo");
		if (mUserInfo == null) {
			this.finish();
		}
		if (mConfigWiFiInfo == null) {
			this.finish();
		}
		QrWidth = getDeviceSize(this).widthPixels;
		ViewGroup.LayoutParams lp = ll_barcode.getLayoutParams();
		float left_right_width = 30;
		float linearWidth = QrWidth - px2dip(this, left_right_width * 2);
		lp.height = (int) linearWidth;
		lp.width = (int) linearWidth;
		ll_barcode.setLayoutParams(lp);
		left_right_width = 45;
		QrWidth -= px2dip(this, left_right_width * 2);
		handlePicture(mConfigWiFiInfo.getSeed());
	}

	private void setListener() {
		btn_hear_voice.setOnClickListener(this);
	}

	public static DisplayMetrics getDeviceSize(Context context) {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	private void handlePicture(String seed) {
		String originSSid = mConfigWiFiInfo.getWifiName();
		String originSecurity = mConfigWiFiInfo.getSecurity();
		String pwd = mConfigWiFiInfo.getWifiPwd();
		StringBuilder sb = new StringBuilder();
		sb.append(DirectUtils.getTypeSecurityByCap(originSecurity) + "\n");
		sb.append(originSSid + "\n");
		if (DirectUtils.getTypeSecurityByCap(originSecurity) != DirectUtils.SECURITY_OPEN) {
			sb.append(RouteLibraryParams.EncodeMappingString(pwd));
			sb.append("\n");
		}

		sb.append(RouteLibraryParams.EncodeMappingString(seed));

		wifi_info = sb.toString();
		createQRImage(wifi_info, QrWidth, QrWidth);// 暂时
	}

	private Bitmap createQRImage(String qrdata, int qrwidth, int qrheight) {
		Bitmap bitmap = null;
		try {
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(qrdata,
					BarcodeFormat.QR_CODE, qrwidth, qrheight, hints);
			int[] pixels = new int[qrwidth * qrheight];
			for (int y = 0; y < qrheight; y++) {
				for (int x = 0; x < qrwidth; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * qrwidth + x] = 0xff000000;
					} else {
						pixels[y * qrwidth + x] = 0xffffffff;
					}
				}
			}
			bitmap = Bitmap.createBitmap(qrwidth, qrheight,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, qrwidth, 0, 0, qrwidth, qrheight);
			iv_barcode.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}

	private void checkBindingState() {
		RouteLibraryController.getInstance().doRequest(
				this,
				RouteApiType.V3_BIND_RESULT,
				RouteLibraryParams.V3BindResult(mUserInfo.getAuth(),
						mConfigWiFiInfo.getDeviceId()), this);
	}

	private Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BIND_RESULT_MSG:
				if (mCurrentNum <= 15) {
					checkBindingState();
					mCurrentNum++;
					myHandler.sendEmptyMessageDelayed(BIND_RESULT_MSG, 6000);
				} else {
					myHandler.sendEmptyMessage(BIND_RESULT_MSG_FAIL);
				}
				break;
			case BIND_RESULT_MSG_FAIL:
				myHandler.removeMessages(BIND_RESULT_MSG);
				Toast.makeText(getApplicationContext(), "Bind Fail!",
						Toast.LENGTH_LONG).show();
				break;
			case BIND_RESULT_MSG_SUCCESS:
				myHandler.removeMessages(BIND_RESULT_MSG);
				Toast.makeText(getApplicationContext(), "Bind Success!",
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_hear_voice:
			myHandler.sendEmptyMessage(BIND_RESULT_MSG);
			break;

		default:
			break;
		}
	}

	@Override
	public void OnSuccess(RouteApiType apiType, String json) {
		switch (apiType) {
		case V3_BIND_RESULT:
			try {
				JSONObject jsonObject = new JSONObject(json);
				int result = jsonObject.isNull("result") ? 0 : jsonObject
						.getInt("result");
				if (result == 1) {
					myHandler.sendEmptyMessage(BIND_RESULT_MSG_SUCCESS);
				}
			} catch (JSONException e) {

			}
			break;

		default:
			break;
		}
	}

	@Override
	public void OnFail(RouteApiType apiType, ErrorCode code) {
		switch (apiType) {
		case V3_BIND_RESULT:
			myHandler.sendEmptyMessage(BIND_RESULT_MSG_FAIL);
			break;
		default:
			break;
		}
	}
}
