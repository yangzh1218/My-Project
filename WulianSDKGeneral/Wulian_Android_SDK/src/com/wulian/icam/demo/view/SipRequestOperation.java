/**
 * Project Name:  WulianICamDemo
 * File Name:     SipRequestOperation.java
 * Package Name:  com.wulian.icam.demo.view
 * @Date:         2016年8月24日
 * Copyright (c)  2016, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wulian.siplibrary.api.SipController;
import com.wulian.siplibrary.manage.SipProfile;

/**
 * @ClassName: SipRequestOperation
 * @Function: TODO
 * @Date: 2016年8月24日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class SipRequestOperation {
	ExecutorService pjSipThreadExecutor = Executors.newSingleThreadExecutor();
	private boolean isSipCreated = false;
	private boolean isAccountRegister = false;
	private Context mAppContext;
	private SipProfile mSipProfile;
	private static SipRequestOperation gSipRequestOperationInstance;

	public static SipRequestOperation getInstance() {
		if (gSipRequestOperationInstance == null) {
			gSipRequestOperationInstance = new SipRequestOperation();
		}
		return gSipRequestOperationInstance;
	}

	public void setAppContext(Context context) {
		mAppContext = context;
	}

	public void initSip() {
		if (!isSipCreated) {
			pjSipThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (!isSipCreated) {
						isSipCreated = SipController.getInstance().CreateSip(
								mAppContext, false);
					} else {
					}
				}
			});
		} else {
		}

	}

	public void destorySip() {
		pjSipThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				SipController.getInstance().DestroySip();// 必须同一线程?
				isSipCreated = false;
			}
		});
	}

	public void makeCall(final String remoteFrom, final SipProfile profile) {
		pjSipThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (isSipCreated) {
					SipController.getInstance().makeCall(remoteFrom, profile);
				}
			}
		});
	}

	public void hangupAllCall() {
		pjSipThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (isSipCreated) {
					SipController.getInstance().hangupAllCall();
				}
			}
		});
	}

	public SipProfile getSipProfile() {
		return mSipProfile;
	}

	public boolean isAccountRegister() {
		return isAccountRegister;
	}

	// NEED to judge the empty string
	public SipProfile registerAccount(final String accountUserName,
			final String accountServer, final String accountPassword) {
		if (mSipProfile == null && !isAccountRegister) {
			pjSipThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (mSipProfile == null) {
						mSipProfile = SipController.getInstance()
								.registerAccount(accountUserName,
										accountServer, accountPassword);
						isAccountRegister = true;
					} else {
					}
				}
			});
		}
		return mSipProfile;
	}

	public void unRegisterAccount() {
		if (mSipProfile != null && !isAccountRegister) {
			pjSipThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					SipController.getInstance()
							.unregistenerAccount(mSipProfile);
					mSipProfile = null;
					isAccountRegister = false;
				}
			});
		} else {
		}
	}
	 public void sendMessage(final String remoteFrom, final String message, final SipProfile account) {
	        pjSipThreadExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	                SipController.getInstance().sendMessage(
	                        remoteFrom, message, account);
	            }
	        });
	    }
	 public void getCallSpeedInfos(final int callId, final Handler handler, final int messageType) throws Exception {
         pjSipThreadExecutor.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     String speedInfo = SipController.getInstance()
                             .getCallSpeedInfos(callId);
                     Message message = new Message();
                     message.what = messageType;
                     Bundle bundle = new Bundle();
                     bundle.putString("speed", speedInfo);
                     message.setData(bundle);
                     handler.sendMessageDelayed(message,
                             5 * 1000);
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }
         });
     }
}
