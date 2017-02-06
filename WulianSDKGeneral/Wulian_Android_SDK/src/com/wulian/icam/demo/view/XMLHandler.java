/**
 * Project Name:  iCam
 * File Name:     XMLHandler.java
 * Package Name:  com.wulian.icam.utils
 * @Date:         2014年12月4日
 * Copyright (c)  2014, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import android.text.TextUtils;

/**
 * @ClassName: XMLHandler
 * @Function: XML处理
 * @Date: 2014年12月4日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class XMLHandler {
	private static final String XML_KEY = "context-text:\n";

	public static List<DeviceDescriptionModel> getDeviceList(String json) {
		List<DeviceDescriptionModel> data = new ArrayList<DeviceDescriptionModel>();
		try {
			JSONObject jsonObj = new JSONObject(json);
			int status = jsonObj.getInt("status");// 肯定会有的
			if (status == 1) {
				String dataJson = jsonObj.getString("data");// 肯定会有
				if (!TextUtils.isEmpty(dataJson)) {
					JSONArray jsonArray = new JSONArray(dataJson);
					int size = jsonArray.length();
					for (int i = 0; i < size; i++) {
						JSONObject itemJson = jsonArray.getJSONObject(i);
						String remoteIp = itemJson.getString("ip");// 肯定会有
						String itemData = itemJson.getString("item");// 肯定会有
						DeviceDescriptionModel device = handleDeviceDescriptionXML(
								itemData, remoteIp);
						if (device != null) {
							data.add(device);
						}
					}
				}
			}
		} catch (JSONException e) {
			data.clear();
		}
		return data;
	}

	public static DeviceDescriptionModel handleDeviceDescriptionXML(
			String data, String remoteIP) {
		String realData = data;
		int startIndex = realData.indexOf(XML_KEY);
		if (startIndex > 0) {
			realData = realData.substring(startIndex + XML_KEY.length());
		}
		DeviceDescriptionModel mDeviceDescriptionModel = null;
		try {
			StringReader xmlReader = new StringReader(realData);
			XmlPullParserFactory pullFactory = XmlPullParserFactory
					.newInstance();
			XmlPullParser xmlPullParser = pullFactory.newPullParser();
			xmlPullParser.setInput(xmlReader); // 保存创建的xml
			int eventType = xmlPullParser.getEventType();
			boolean isDone = false;// 具体解析xml
			while ((eventType != XmlPullParser.END_DOCUMENT)
					&& (isDone != true)) {
				String localName = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT: {
					mDeviceDescriptionModel = new DeviceDescriptionModel();
					mDeviceDescriptionModel.setRemoteIP(remoteIP);
				}
					break;
				case XmlPullParser.START_TAG:
					localName = xmlPullParser.getName();
					if ("local_mac".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setLocal_mac((xmlPullParser
								.nextText()));
					} else if ("model".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setLocal_mac((xmlPullParser
								.nextText()));
					} else if ("model".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setModel((xmlPullParser
								.nextText()));
					} else if ("serialnum".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setSerialnum((xmlPullParser
								.nextText()));
					} else if ("version".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setVersion((xmlPullParser
								.nextText()));
					} else if ("hardware".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setHardware((xmlPullParser
								.nextText()));
					} else if ("sipaccount".equalsIgnoreCase(localName)) {
						mDeviceDescriptionModel.setSipaccount((xmlPullParser
								.nextText()));
					} else if ("video_port".equalsIgnoreCase(localName)) {
						Integer video_port = -1;
						try {
							String video_portStr = xmlPullParser.nextText();
							video_port = Integer.parseInt(video_portStr, 10);
						} catch (NumberFormatException e) {
							video_port = -1;
						}
						mDeviceDescriptionModel.setVideo_port(video_port);
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) { // XmlPullParserFactory.newInstance
			e.printStackTrace();
			mDeviceDescriptionModel = null;
		} catch (IllegalArgumentException e) { // xmlSerializer.setOutput
			e.printStackTrace();
			mDeviceDescriptionModel = null;
		} catch (IllegalStateException e) { // xmlSerializer.setOutput
			e.printStackTrace();
			mDeviceDescriptionModel = null;
		} catch (Exception e) {
			e.printStackTrace();
			mDeviceDescriptionModel = null;
		}
		return mDeviceDescriptionModel;
	}
	public static DeviceDetailMsg getDeviceDetailMsg(String xml) {
		DeviceDetailMsg deviceDetailMsg = null;
		StringReader xmlReader = new StringReader(xml);
		XmlPullParserFactory pullFactory;
		try {
			pullFactory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = pullFactory.newPullParser();
			xmlPullParser.setInput(xmlReader); // 保存创建的xml
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String localName = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT: {
					deviceDetailMsg = new DeviceDetailMsg();
				}
					break;
				case XmlPullParser.START_TAG:
					localName = xmlPullParser.getName();
					if ("version".equalsIgnoreCase(localName)) {
						deviceDetailMsg.setVersion(xmlPullParser.nextText());
					} else if ("wifi_ssid".equalsIgnoreCase(localName)) {
						deviceDetailMsg.setWifi_ssid(xmlPullParser.nextText()
								.trim());
					} else if ("wifi_signal".equalsIgnoreCase(localName)) {
						deviceDetailMsg
								.setWifi_signal(xmlPullParser.nextText());
					} else if ("ip".equalsIgnoreCase(localName)) {
						deviceDetailMsg.setWifi_ip(xmlPullParser.nextText());
					} else if ("mac".equalsIgnoreCase(localName)) {
						deviceDetailMsg.setWifi_mac(xmlPullParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) { // XmlPullParserFactory.newInstance
			e.printStackTrace();
			deviceDetailMsg = null;
		} catch (IllegalArgumentException e) { // xmlSerializer.setOutput
			e.printStackTrace();
			deviceDetailMsg = null;
		} catch (IllegalStateException e) { // xmlSerializer.setOutput
			e.printStackTrace();
			deviceDetailMsg = null;
		} catch (Exception e) {
			e.printStackTrace();
			deviceDetailMsg = null;
		}
		return deviceDetailMsg;
	}
	// 解析XML数据
			public static String parseXMLDataGetStatus(String xmlData) {
				String regEx = "<status>[^>]*</status>";
				String result = "null";
				Pattern pattern = Pattern.compile(regEx);
				Matcher m = pattern.matcher(xmlData);
				if (m.find()) {
					result = m.group();
				}
				int start = "<status>".length();
				int end = result.length() - "</status>".length();
				if (!result.equals("null")) {
					result = result.substring(start, end);
					return result;
				}
				return null;
			}
			// 解析XML数据
			public static String parseXMLDataGetFilename(String xmlData) {
				String regEx = "<status>[^>]*</status>";
				String result = "null";
				Pattern pattern = Pattern.compile(regEx);
				Matcher m = pattern.matcher(xmlData);
				if (m.find()) {
					result = m.group();
				}
				int start = "<status>".length();
				int end = result.length() - "</status>".length();
				if (!result.equals("null")) {
					result = result.substring(start, end);
					return result;
				}
				return null;
			}
			// 解析XML数据
			public static String parseXMLDataGetSessionID(String xmlData) {
				String regEx = "<sessionID>[^>]*</sessionID>";
				String result = "null";
				Pattern pattern = Pattern.compile(regEx);
				Matcher m = pattern.matcher(xmlData);
				if (m.find()) {
					result = m.group();
				}
				int start = "<sessionID>".length();
				int end = result.length() - "</sessionID>".length();
				if (!result.equals("null")) {
					result = result.substring(start, end);
					return result;
				}
				return null;
			}
			// 解析XML数据
			public static boolean parseXMLDataJudgeEnd(String xmlData) {
				StringReader xmlReader = new StringReader(xmlData);
				XmlPullParserFactory pullFactory;
				try {
					pullFactory = XmlPullParserFactory.newInstance();
					XmlPullParser xmlPullParser = pullFactory.newPullParser();
					xmlPullParser.setInput(xmlReader); // 保存创建的xml
					int eventType = xmlPullParser.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						String localName = null;
						switch (eventType) {
						case XmlPullParser.START_DOCUMENT: {

						}
							break;
						case XmlPullParser.START_TAG:
							localName = xmlPullParser.getName();
							if ("history".equalsIgnoreCase(localName)) {
								int tailValue = Integer.valueOf(xmlPullParser
										.getAttributeValue(1));
								if (tailValue == 1) {
									return true;
								}
							}
							break;
						case XmlPullParser.END_TAG:
							break;
						}
						eventType = xmlPullParser.next();
					}
				} catch (XmlPullParserException e) { // XmlPullParserFactory.newInstance
					e.printStackTrace();
				} catch (IllegalArgumentException e) { // xmlSerializer.setOutput
					e.printStackTrace();
				} catch (IllegalStateException e) { // xmlSerializer.setOutput
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (xmlReader != null) {
						xmlReader.close();
					}
				}
				return false;
			}
}
