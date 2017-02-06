/**
 * Project Name:  FamilyRoute
 * File Name:     Utils.java
 * Package Name:  com.wulian.familyroute.utils
 * @Date:         2014-9-9
 * Copyright (c)  2014, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * @ClassName: Utils
 * @Function: Common auxiliary class
 * @Date: 2014-9-9
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class Utils {
	/**
	 * 
	 * @Function Get Bean object entity deficiencies through reflection: reflection not only reflecting a single entity set could use Gson library to parse
	 * @author Wangjj
	 * @date 2014年11月3日
	 * @param clazz
	 * @param jsonStr
	 * @return
	 */
	public static <T> T parseBean(Class<T> clazz, String jsonStr) {
		Field[] fields = clazz.getDeclaredFields();
		T object = null;

		try {
			// JavaBean has a default no-argument constructor, if there are other argument constructor, you must make up no-argument constructor
			object = clazz.newInstance();
		} catch (Exception e2) {
			e2.printStackTrace();
			return null;
		}

		if (object != null) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(jsonStr);
			} catch (JSONException e1) {
				e1.printStackTrace();
				return null;

			}
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				String fName = field.getName();
				String fUpName = upFirstCharacter(field.getName());
				try {
					if (field.getType() == int.class) {

						Method method = clazz.getMethod("set" + fUpName,
								int.class);
						method.invoke(object, jsonObject.optInt(fName));

					} else if (field.getType() == String.class) {
						Method method = clazz.getMethod("set" + fUpName,
								String.class);
						method.invoke(object, jsonObject.optString(fName));

					} else if (field.getType() == Boolean.class) {
						Method method = clazz.getMethod("set" + fUpName,
								String.class);
						method.invoke(object, jsonObject.optBoolean(fName));

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return object;
	}

	public static <T> T parseBean(Class<T> clazz, JSONObject jsonObject) {
		Field[] fields = clazz.getDeclaredFields();
		T object = null;

		try {
			//JavaBean has a default no-argument constructor, if there are other argument constructor, you must make up no-argument constructor
			object = clazz.newInstance();
		} catch (Exception e2) {
			e2.printStackTrace();
			return null;
		}

		if (object != null) {

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				String fName = field.getName();
				String fUpName = upFirstCharacter(field.getName());
				try {
					if (field.getType() == int.class) {

						Method method = clazz.getMethod("set" + fUpName,
								int.class);
						method.invoke(object, jsonObject.optInt(fName));

					} else if (field.getType() == String.class) {
						Method method = clazz.getMethod("set" + fUpName,
								String.class);
						method.invoke(object, jsonObject.optString(fName));

					} else if (field.getType() == Boolean.class) {
						Method method = clazz.getMethod("set" + fUpName,
								String.class);
						method.invoke(object, jsonObject.optBoolean(fName));

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return object;
	}

	public static String upFirstCharacter(String str) {
		return (str.charAt(0) + "").toUpperCase(Locale.US) + str.substring(1);
	}
	/**
	 * 
	 * @Function 获取当前客户端版本信息
	 * @author Wangjj
	 * @date 2014年11月6日
	 * @param mContext
	 * @return
	 */
	public static PackageInfo getPackageInfo(Context mContext) {
		PackageInfo info = null;
		try {
			info = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0);

		} catch (NameNotFoundException e) {
			e.printStackTrace(System.err);
			info = new PackageInfo();// 即使遇到异常也要返回一个实体对象，不要返回null去让调用者做null判断？
		}
		return info;
	}

	public static String getParamFromXml(String xmlString, String param) {
		// \\w+不能匹配local_mac中的':',如00:11:22
				Pattern p = Pattern.compile("<" + param + ">(.+)</" + param + ">");
				Matcher matcher = p.matcher(xmlString);
				if (matcher.find())
					return matcher.group(1).trim();
				return "";
	}
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	public static void sysoInfo(String info) {
			System.out.println(info);
	}
	/**
     * @Function 获取屏幕尺寸
     * @author Wangjj
     * @date 2014年10月5日
     * @param context
     * @return
     */
    public static DisplayMetrics getDeviceSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }
}
