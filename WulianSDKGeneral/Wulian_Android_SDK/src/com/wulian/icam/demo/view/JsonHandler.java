/**
 * Project Name:  iCam
 * File Name:     JsonHandler.java
 * Package Name:  com.wulian.icam.utils
 * @Date:         2014年12月17日
 * Copyright (c)  2014, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wulian.icam.demo.view.BindingOauthAccountModel;

/**
 * @ClassName: JsonHandler
 * @Function: Json解析
 * @Date: 2014年12月17日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class JsonHandler {
	// 获取授权账户列表
	public static List<BindingOauthAccountModel> getBindingOauthAccoutList(
			String value, String device_id) {
		List<BindingOauthAccountModel> list = new ArrayList<BindingOauthAccountModel>();
		try {
			JSONObject jsonobject = new JSONObject(value);
			JSONArray jsonArray = jsonobject.isNull("data") ? null : jsonobject
					.getJSONArray("data");
			if (jsonArray == null || jsonArray.length() == 0) {
				return list;
			}
			int size = jsonArray.length();
			BindingOauthAccountModel data = null;
			for (int index = 0; index < size; index++) {
				JSONObject item = jsonArray.getJSONObject(index);
				data = new BindingOauthAccountModel();
				data.setUsername(item.isNull("username") ? "" : item
						.getString("username"));
				data.setPhone(item.isNull("phone") ? "" : item
						.getString("phone"));
				data.setEmail(item.isNull("email") ? "" : item
						.getString("email"));
				long timeStamp = 1;

				String updated_at = item.isNull("updated_at") ? "" : item
						.getString("updated_at");
				Date date = new Date();
				if (updated_at.equals("")) {
					timeStamp = date.getTime();
				} else {
					try {
						timeStamp = Long.parseLong(updated_at);
					} catch (NumberFormatException e) {
						timeStamp = date.getTime();
					}
				}
				data.setTimestamp(timeStamp);
				data.setDevice_id(device_id);// 服务器不会返回该字段，需要手动设置
				list.add(data);
			}
		} catch (JSONException e) {
		}
		return list;
	}
	/**
	 * @MethodName: getBindingOauthDetailList
	 * @Function: 获取授权信息详细列表
	 * @author: yuanjs
	 * @date: 2015年7月11日
	 * @email: yuanjsh@wuliangroup.cn
	 * @param value
	 *            其中 count 是查看次数，lasttime是最后查看时间
	 * @param device_id
	 * @return
	 */
	public static List<OauthUserDetail> getBindingOauthDetailList(String value) {
		List<OauthUserDetail> list = new ArrayList<OauthUserDetail>();
		try {
			JSONObject jsonobject = new JSONObject(value);
			JSONArray jsonArray = jsonobject.isNull("data") ? null : jsonobject
					.getJSONArray("data");
			if (jsonArray == null || jsonArray.length() == 0) {
				return list;
			}
			int size = jsonArray.length();
			OauthUserDetail data = null;
			for (int index = 0; index < size; index++) {
				JSONObject item = jsonArray.getJSONObject(index);
				data = new OauthUserDetail();
				data.setUsername(item.isNull("username") ? "" : item
						.getString("username"));
				data.setPhone(item.isNull("phone") ? "" : item
						.getString("phone"));
				data.setEmail(item.isNull("email") ? "" : item
						.getString("email"));
				if (!item.isNull("count")) {
					data.setCount(item.getInt("count"));
				} else {
					data.setCount(0);// 授权用户一次也没有使用摄像机
				}
				long timeStamp = 1;
				long recently_time = 1;
				String updated_at = item.isNull("updated_at") ? "" : item
						.getString("updated_at");
				String recentyTime = item.isNull("lasttime") ? "" : item
						.getString("lasttime");
				Date date = new Date();
				if (updated_at.equals("")) {
					timeStamp = 0;
				} else {
					try {
						timeStamp = Long.parseLong(updated_at);
					} catch (NumberFormatException e) {
						timeStamp = date.getTime();
					}
				}
				if (recentyTime.equals("")) {
					recently_time = date.getTime();
				} else {
					try {
						recently_time = Long.parseLong(recentyTime);
					} catch (NumberFormatException e) {
						recently_time = date.getTime();
					}
				}
				data.setLasttime(recently_time);
				data.setTimestamp(timeStamp);
				list.add(data);
			}
		} catch (JSONException e) {
		}
		return list;
	}
	
}
