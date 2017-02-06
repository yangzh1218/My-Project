/**
 * Project Name:  WulianICamDemo
 * File Name:     PlayVideoActivity.java
 * Package Name:  com.wulian.icam.demo.view
 * @Date:         2016年8月24日
 * Copyright (c)  2016, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.io.File;
import java.util.Locale;

import org.webrtc.videoengine.ViERenderer;

import com.wulian.icam.demo.R;
import com.wulian.siplibrary.api.SipController;
import com.wulian.siplibrary.api.SipHandler;
import com.wulian.siplibrary.api.SipMsgApiType;
import com.wulian.siplibrary.manage.SipCallSession;
import com.wulian.siplibrary.manage.SipManager;
import com.wulian.siplibrary.manage.SipMessage;
import com.wulian.siplibrary.manage.SipProfile;
import com.wulian.siplibrary.utils.WulianLog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @ClassName: PlayVideoActivity
 * @Function: TODO
 * @Date: 2016年8月24日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class PlayVideoActivity extends Activity implements OnClickListener {
	private SurfaceView cameraPreview;

	private ViewGroup mainframe;

	private AudioManager audioManager;

	private static final int SHOWSPEED = 3;
	private static final int SHOWSPEED_INTERVAL = 5;
	private static final int SPEED_RETRY = 5;
	private static final int SPEED_RETRY_FORCE = 6;
	private static final int SPEED_RETRY_TIME = 15000;

	private Button bt_stopVideo;
	private Button bt_sendMessage;
	private Button bt_talk;
	private Button bt_not_talk;
	private Button bt_not_mute;
	private Button bt_mute;
	private Button bt_capture_picture;
	private Button btn_definition1, btn_definition2, btn_definition3;
	private Button btn_up, btn_down, btn_left, btn_right,btn_stop,btn_full_screen;
	private CheckBox cb_pictureReverse;
	
	private LinearLayout ll_up,ll_down,ll_play_container;
	private RelativeLayout rl_video,rl_down;
	private int callId = -1;
	private String deviceCallIp = "";
	private String lastSpeed = "0";
	private TextView tv_speed;
	private Device mCurrentDevice;
	private UserInfo mUserInfo;
	private SipProfile account;
	private static final String capture_picture_dir_path = "/Demo/Pictures/";
	private static final int SHOW_TOAST = 10;
	private int widthRatio = 16;
    private int heightRatio = 9;
    private int minWidth, maxWidth;
    private ImageView iv_snapshot;
    private Button btn_volume_add,btn_volume_mut;
    AudioManager mAudioManager;
    int currentVolume;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playvideo1);
		initView();
		initData();
		setListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// close the preview show
		Log.d("PML", "onPause unregisterReceiver callStateReceiver");
		SipRequestOperation.getInstance().hangupAllCall();
		unregisterReceiver(callStateReceiver);// Close broadcast
		if (callId >= 0) {
			SipController.getInstance().setVideoAndroidRenderer(callId, null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(callStateReceiver,
				new IntentFilter(SipManager.GET_ACTION_SIP_CALL_CHANGED()));// Register
																			// Video
																			// Broadcasting
		registerReceiver(callStateReceiver,
				new IntentFilter(SipManager.GET_ACTION_SIP_MESSAGE_RECEIVED()));// Registration
		// To start get video stream
		SipController.getInstance().makeCall(
				mCurrentDevice.getUsername() + "@"
						+ mCurrentDevice.getSdomain(), account);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	private void initView() {
		btn_volume_add = (Button) findViewById(R.id.btn_volume_add);
		btn_volume_mut = (Button) findViewById(R.id.btn_volume_mut);
		iv_snapshot = (ImageView) findViewById(R.id.iv_snapshot);
		rl_down = (RelativeLayout) findViewById(R.id.rl_down);
		btn_full_screen = (Button) findViewById(R.id.btn_full_screen);
		rl_video = (RelativeLayout) findViewById(R.id.rl_video);
		ll_play_container = (LinearLayout) findViewById(R.id.ll_play_container);
		mainframe = (ViewGroup) findViewById(R.id.mainframe);

		mainframe = (ViewGroup) findViewById(R.id.mainframe);

		bt_stopVideo = (Button) findViewById(R.id.bt_stopVideo);
		bt_sendMessage = (Button) findViewById(R.id.bt_sendMessage);
		bt_talk = (Button) findViewById(R.id.bt_talk);
		bt_not_talk = (Button) findViewById(R.id.bt_not_talk);
		bt_not_mute = (Button) findViewById(R.id.bt_not_mute);
		bt_mute = (Button) findViewById(R.id.bt_mute);
		bt_capture_picture = (Button) findViewById(R.id.bt_capture_picture);
		cb_pictureReverse = (CheckBox) findViewById(R.id.cb_pictureReverse);
		btn_definition1 = (Button) findViewById(R.id.btn_definition1);
		btn_definition2 = (Button) findViewById(R.id.btn_definition2);
		btn_definition3 = (Button) findViewById(R.id.btn_definition3);
		tv_speed = (TextView) findViewById(R.id.tv_speed);
		btn_up = (Button) findViewById(R.id.btn_up);
		btn_down = (Button) findViewById(R.id.btn_down);
		btn_left = (Button) findViewById(R.id.btn_left);
		btn_right = (Button) findViewById(R.id.btn_right);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		ll_up = (LinearLayout) findViewById(R.id.ll_up);
		ll_down = (LinearLayout) findViewById(R.id.ll_down);
		// cameraPreview to show video stream
		if (cameraPreview == null) {
			cameraPreview = ViERenderer.CreateRenderer(this, true,
					cb_pictureReverse.isChecked());
			int deviceHeight = Utils.getDeviceSize(this).heightPixels;
            int cameraPreviewHeight = deviceHeight * 4 / 9;// 根据布局中的上下比例
            int cameraPreviewWidth = (int) ((float) cameraPreviewHeight
                    / heightRatio * widthRatio);
            minWidth = Utils.getDeviceSize(this).widthPixels;
            maxWidth = cameraPreviewWidth;
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    cameraPreviewWidth, cameraPreviewHeight);

            lp.addRule(RelativeLayout.CENTER_IN_PARENT);// 全尺寸时居中显示
            rl_video.addView(cameraPreview, 0, lp);
		} else {
			WulianLog.d("MainActivity", "NO NEED TO Create Local Renderer");
		}
		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);// TEST adjust the
															// volume
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isLandscape()) 
            {
                goPortrait();
            }else{
            	return super.onKeyDown(keyCode, event);
            }
            return true;
        }
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			int inCallStream = AudioManager.STREAM_MUSIC;
			float max = audioManager.getStreamMaxVolume(inCallStream);
			float current = (float) audioManager.getStreamVolume(inCallStream);
			SipController.getInstance().setMediaSpeakerOne(
					(float) current / max);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initData() {
		mCurrentDevice = (Device) getIntent().getSerializableExtra("Device");
		mUserInfo = (UserInfo) getIntent().getSerializableExtra("UserInfo");
		account = SipRequestOperation.getInstance().getSipProfile();

		if (mCurrentDevice == null || account == null) {
			this.finish();
		}
		ViERenderer.setTakePicHandler(mHandler);
		deviceCallIp = mCurrentDevice.getUsername() + "@"
				+ mCurrentDevice.getSdomain();
		String deviceId = mCurrentDevice.getDid();
		if (deviceId.toLowerCase(Locale.ENGLISH).startsWith("cmic01")
				|| deviceId.toLowerCase(Locale.ENGLISH)
						.startsWith("cmic04"))
		{
			ll_up.setVisibility(View.VISIBLE);
			ll_down.setVisibility(View.VISIBLE);
		}
		else
		{
			ll_up.setVisibility(View.GONE);
			ll_down.setVisibility(View.GONE);
		}
		 mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		 currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	private void setListener() {
		btn_volume_add.setOnClickListener(this);
		btn_volume_mut.setOnClickListener(this);
		btn_full_screen.setOnClickListener(this);
		bt_stopVideo.setOnClickListener(this);
		bt_sendMessage.setOnClickListener(this);
		bt_talk.setOnClickListener(this);
		bt_not_talk.setOnClickListener(this);
		bt_not_mute.setOnClickListener(this);
		bt_mute.setOnClickListener(this);
		bt_capture_picture.setOnClickListener(this);
		btn_definition1.setOnClickListener(this);
		btn_definition2.setOnClickListener(this);
		btn_definition3.setOnClickListener(this);
		btn_up.setOnClickListener(this);
		btn_down.setOnClickListener(this);
		btn_left.setOnClickListener(this);
		btn_right.setOnClickListener(this);
		btn_stop.setOnClickListener(this);
	}

	private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("PML", "BroadcastReceiver callStateReceiver:Action is:"
					+ SipManager.GET_ACTION_SIP_CALL_CHANGED());
			if (action.equals(SipManager.GET_ACTION_SIP_CALL_CHANGED())) {

				SipCallSession sipCallSession = (SipCallSession) intent
						.getParcelableExtra("call_info");
				if (sipCallSession != null) {
					int lastCode = sipCallSession.getLastStatusCode();
					if (sipCallSession.getCallState() == SipCallSession.InvState.CONFIRMED) {
						callId = sipCallSession.getCallId();
						Log.d("PML", "callId is:" + callId);
						runOnUiThread(new UpdateUIFromCallRunnable());
					}
				}
			}
			if (action.equals(SipManager.GET_ACTION_SIP_MESSAGE_RECEIVED())) {
				SipMessage sm = (SipMessage) intent
						.getParcelableExtra("SipMessage");
				if (sm != null) {
					SipMsgApiType apiType = SipHandler.parseXMLData(sm
							.getBody());
					if (apiType == SipMsgApiType.PUSH_ALARM_EVENT) {
						// showMsg(sm.getBody());//Testing, see the document:
						// Nanjing IOT IPcamera Access Protocol
					}
				}
			}
		}
	};

	/**
	 * Update the user interface from calls state.
	 **/
	private class UpdateUIFromCallRunnable implements Runnable {
		@Override
		public void run() {
			if (callId >= 0) {
				if (cameraPreview.getVisibility() == View.GONE) {
					cameraPreview.setVisibility(View.VISIBLE);
				}
				SipController.getInstance().setVideoAndroidRenderer(callId,
						cameraPreview);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_stopVideo:// Stop video stream
			// if (callId >= 0)
			// SipController.getInstance().hangupCall(callId);// close all
			// // video stream
			// // if it has
			SipController.getInstance().hangupAllCall();
			SipController.getInstance().setVideoAndroidRenderer(
					SipCallSession.INVALID_CALL_ID, null);// close the preview
			break;
		case R.id.bt_sendMessage:// Send a message to the camera if the camera
									// is online
			String remoteFromStr1 = mCurrentDevice.getUsername() + "@"
					+ mCurrentDevice.getSdomain();// Remote Device Sip
													// Account ,for
			// example:sip:cmicxxxxx@sh.gg
			String msgStr = "";// Need to send message
			// Notice :msgStr That is the message in the remote access protocol
			// required to send, the interface have all been packaged，In
			// SipHandler.java
			// Specific call as follows：
			// SipHandler.ConfigVoiceMute(uri, seq, mute)Voice Control

			SipController.getInstance().sendMessage(remoteFromStr1, msgStr,
					account);
			break;
		case R.id.bt_talk: {// talk to camera or not
			String deviceCallUrl = mCurrentDevice.getUsername() + "@"
					+ mCurrentDevice.getSdomain();
			int seq = 1;
			boolean isTalk = true;
			if (isTalk) {
				silenceControl(true);
			}
			SipController.getInstance().setMicrophoneMute(!isTalk, callId);
			SipController.getInstance().setSpeakerphoneOn(!isTalk, callId);
			configVoiceIntercom(!isTalk ? "output" : "input");
			break;
		}
		case R.id.bt_not_talk: {
			boolean isTalk = false;
			SipController.getInstance().setMicrophoneMute(!isTalk, callId);
			SipController.getInstance().setSpeakerphoneOn(!isTalk, callId);
			configVoiceIntercom(!isTalk ? "output" : "input");
			break;
		}
		case R.id.bt_not_mute: {
			if (callId >= 0) {
				boolean not_mute = true;
				SipController.getInstance().setMicrophoneMute(true, callId);
				SipController.getInstance().setSpeakerphoneOn(not_mute, callId);
				silenceControl(!not_mute);
			}
			break;
		}
		case R.id.bt_mute: {// the mobile phone is mute or not
			boolean not_mute = false;
			SipController.getInstance().setMicrophoneMute(true, callId);
			SipController.getInstance().setSpeakerphoneOn(not_mute, callId);
			silenceControl(!not_mute);
			break;
		}
		case R.id.btn_definition1: {
			sendDpiMessage("320x240");
			break;
		}
		case R.id.btn_definition2: {
			sendDpiMessage("640x480");
			break;
		}
		case R.id.btn_definition3: {
			sendDpiMessage("1280x720");
			break;
		}
		case R.id.bt_capture_picture: {
			ViERenderer.setTakePicHandler(mHandler);// This method is to save
													// picture in mobile phone
			// ViERenderer.setTakePicNotSave();//This method is not to save
			// picture in mobile phone
			String storageState = Environment.getExternalStorageState();
			String savePath = "";
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {
				savePath = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + capture_picture_dir_path;
				File dir = new File(savePath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			// You know
			if (TextUtils.isEmpty(savePath)) {
				mHandler.sendEmptyMessage(ViERenderer.FILE_MOUNT_EXCEPTION);
				return;
			}

			ViERenderer.setTakePic(savePath);
			break;
		}
		case R.id.btn_up:
			SipController.getInstance().sendInfo(deviceCallIp, SipHandler.ControlPTZMovement(deviceCallIp, 0, APPConfig.MOVE_SPEED), callId, account);
			break;
		case R.id.btn_down:
			SipController.getInstance().sendInfo(deviceCallIp, SipHandler.ControlPTZMovement(deviceCallIp, 0, -APPConfig.MOVE_SPEED), callId, account);
			break;
		case R.id.btn_left:
			SipController.getInstance().sendInfo(deviceCallIp, SipHandler.ControlPTZMovement(deviceCallIp, -APPConfig.MOVE_SPEED, 0), callId, account);
			break;
		case R.id.btn_right:
			SipController.getInstance().sendInfo(deviceCallIp, SipHandler.ControlPTZMovement(deviceCallIp, APPConfig.MOVE_SPEED, 0), callId, account);
			break;
		case R.id.btn_stop:
			SipController.getInstance().sendInfo(deviceCallIp, SipHandler.ControlPTZMovement(deviceCallIp, 0, 0), callId, account);
			break;
		case R.id.btn_full_screen:
			if (isPortrait()) {
                goLandscape();
            } else {
                goPortrait();
            }
			break;
		case R.id.btn_volume_add:
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume++, 0);
			break;
		case R.id.btn_volume_mut:
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume--, 0);
			break;
		}
	}
	/**
     * @Function 竖屏模式
     */
    private void goPortrait() {
        ll_play_container.setBackgroundColor(getResources().getColor(
                R.color.background));
        rl_down.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    /**
     * @Function 横屏模式
     */
    private void goLandscape() {
        ll_play_container.setBackgroundColor(getResources().getColor(
                R.color.black));
        // 进入横屏模式 需要layout_weight
        rl_down.setVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }
    public boolean isLandscape() {
        return this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public boolean isPortrait() {
        return this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    private void landscapeDo()
    {
    	RelativeLayout.LayoutParams lp = (LayoutParams) cameraPreview
                .getLayoutParams();
        // 以屏幕宽度 推测 高度
        lp.width = Utils.getDeviceSize(this).widthPixels;
        lp.height = (int) ((float) lp.width / widthRatio * heightRatio);
        cameraPreview.setLayoutParams(lp);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landscapeDo();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
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
					iv_snapshot.setImageBitmap(bitmap);
//					bitmap.recycle();
//					bitmap = null;
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
			case ViERenderer.PICTURE_HAS_COMING:
				break;
			default:
				break;
			}
		}
	};

	private void sendDpiMessage(String dpi) {
		String deviceCallUrl = mCurrentDevice.getUsername() + "@"
				+ mCurrentDevice.getSdomain();
		int seq = 0;
		SipRequestOperation.getInstance().sendMessage(
				deviceCallUrl,
				SipHandler.ConfigEncode("sip:" + deviceCallUrl, seq++, dpi, 15,
						0), account);
	}

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

	private void configVoiceIntercom(String function) {
		String deviceCallUrl = mCurrentDevice.getUsername() + "@"
				+ mCurrentDevice.getSdomain();
		int seq = 1;
		SipController.getInstance().sendInfo(deviceCallUrl,
				SipHandler.ConfigVoiceIntercom(deviceCallUrl, seq, function),
				callId, account);
	}

	private void silenceControl(boolean flag) {
		String deviceCallUrl = mCurrentDevice.getUsername() + "@"
				+ mCurrentDevice.getSdomain();
		int seq = 1;
		SipController.getInstance().sendInfo(
				deviceCallUrl,
				SipHandler.ConfigVoiceMute(deviceCallUrl, seq, flag ? "true"
						: "false"), callId, account);
	}
}
