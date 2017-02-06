/**
 * Project Name:  iCam
 * File Name:     Device.java
 * Package Name:  com.wulian.icam.model
 * @Date:         2014年10月21日
 * Copyright (c)  2014, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.io.Serializable;

import android.text.TextUtils;

/**
 * @ClassName: Device
 * @Function: Equipment bean
 * @Date: 2014年10月21日
 * @author Wangjj
 * @email wangjj@wuliangroup.cn
 */
public class Device implements Serializable {
	private static final long serialVersionUID = 1L;
	private String did;
	private String location;
	private int online;
	private String nick;
	private String description;
	private String sdomain;
	private String username;// Temporarily equivalent device id
	private int shares, protect;
	private long updated;
	private boolean is_BindDevice = false;// Whether it is the primary bonding
											// equipment

	public boolean getIs_BindDevice() {
		return is_BindDevice;
	}

	public void setIs_BindDevice(boolean is_BindDevice) {
		this.is_BindDevice = is_BindDevice;
	}

	public String getDid() {
		return did;
	}

	public void setDid(String device_id) {
		this.did = device_id;
		setUsername(device_id);
	}

	public int getOnline() {
		return online;
	}

	public void setOnline(int is_online) {
		this.online = is_online;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String device_nick) {
		this.nick = device_nick;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String device_desc) {
		this.description = device_desc;
	}

	public String getSdomain() {
		if (TextUtils.isEmpty(sdomain)) {
			return APPConfig.SERVERNAME;
		}
		return sdomain;
	}

	public void setSdomain(String sip_domain) {
		this.sdomain = sip_domain;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String sip_username) {
		this.username = sip_username;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated_at) {
		this.updated = updated_at;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "Device [device_id=" + did + ", is_online=" + online
				+ ", device_nick=" + nick + ", device_desc=" + description
				+ ", sip_domain=" + sdomain + ", sip_username=" + username
				+ ", updated_at=" + updated + ", is_BindDevice="
				+ is_BindDevice + "]";
	}

	public int getShares() {
		return shares;
	}

	public void setShares(int authcount) {
		this.shares = authcount;
	}

	public int getProtect() {
		return protect;
	}

	public void setProtect(int protect) {
		this.protect = protect;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
