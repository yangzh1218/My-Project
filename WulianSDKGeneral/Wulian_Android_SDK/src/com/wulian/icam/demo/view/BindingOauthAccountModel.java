/**
 * Project Name:  iCam
 * File Name:     BindingOauthModel.java
 * Package Name:  com.wulian.icam.model
 * @Date:         2014年12月17日
 * Copyright (c)  2014, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

/**
 * @ClassName: BindingOauthAccountModel
 * @Function: 已经授权用户
 * @Date: 2014年12月17日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class BindingOauthAccountModel {

	private String username;
	private String phone;

	public BindingOauthAccountModel() {

	}

	public BindingOauthAccountModel(String username) {
		super();
		this.username = username;
	}

	private String email;
	private long timestamp;// updated_at
	private String device_id;// 服务器不会返回该字段，需要手动设置

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return phone;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getDevice_id() {
		return device_id;
	}
}
