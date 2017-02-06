/**
 * Project Name:  iCam
 * File Name:     UserInfo.java
 * Package Name:  com.wulian.icam.model
 * @Date:         2014年11月3日
 * Copyright (c)  2014, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

/**
 * @ClassName: UserInfo
 * @Function:User Info
 * @Date: 2014年11月3日
 * @author Wangjj
 * @email wangjj@wuliangroup.cn
 */
public class UserInfo implements Serializable{

	/**
	 * serialVersionUID 作用:TODO
	 */
	private static final long serialVersionUID = -3032108956027417469L;
	private String uuid = "";// User uuid
	private String suid = "";// sips Username
	private String sdomain = "";
	private String avatar = "";
	private String auth = "";// auth
	private int expire;// Discover the test, after the first reflection expire reflection expires
	private int expires;// Checksum Expiration
	private int localExpireStart;// Local computing overtime starting time
	private String username = "";
	private String email = "";
	private String phone = "";
	private String password = "";

	// JavaBean has a default no-argument constructor, if there are other argument constructor, you must make up no-argument constructor

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSuid() {
		return suid;
	}

	public String getSipSuidSdomain() {
		return "sip:" + suid + sdomain;
	}

	public void setSuid(String suid) {
		this.suid = suid;
	}

	public String getSdomain() {

		if (TextUtils.isEmpty(sdomain)) {
			return APPConfig.SERVERNAME;
		}
		return sdomain;
	}

	public void setSdomain(String sdomain) {
		this.sdomain = sdomain;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

	/**
	 * @Function Based on time-out time calculation of mobile phone users
	 * @author Wangjj
	 * @date 2015年5月9日
	 * @return
	 */

	public int getExpires() {
		setExpires(0);//Because the order is uncertain reflection
						// before time expire and localExpireStart expires, called again, and these values must have been a good reflection。
		return expires - 120;// Local not exceeded, the server may have timed out, so to advance 120 seconds
	}

	public void setExpires(int expires) {
		// this.expires = expires;
		if (expire == 0) {
			expire = 3600;
		}
		if (localExpireStart == 0) {
			localExpireStart = (int) (System.currentTimeMillis() / 1000);
		}
		// bug:If you restore from jsp in json string, then the timeout will System.currentTimeMillis () recalculated according postponed accordingly, so localized.
		// this.expires = (int) (System.currentTimeMillis() / 1000) + expire;
		this.expires = localExpireStart + expire;
	}

	public int getLocalExpireStart() {
		return localExpireStart;
	}

	public void setLocalExpireStart(int localExpireStart) {
		this.localExpireStart = localExpireStart;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserInfo [uuid=" + uuid + ", suid=" + suid + ", sdomain="
				+ sdomain + ", auth=" + auth + ", expires=" + expires
				+ ", username=" + username + ", email=" + email + ", phone="
				+ phone + ", password=" + password + "]";
	}

	
//	public Bitmap getAvatar(Context context){
//		return Utils.getBitmap(uuid, context);
//	}

	/**
	 * avatar.
	 *
	 * @return  the avatar
	 */
	public String getAvatar() {
		return avatar;
	}

	/**
	 * avatar
	 * @param   avatar    the avatar to set
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}
