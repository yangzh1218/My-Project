/**
 * Project Name:  WulianICamRouteLibrary
 * File Name:     MD5.java
 * Package Name:  com.wulian.routelibrary.utils
 * @Date:         2015年12月6日
 * Copyright (c)  2015, wulian All Rights Reserved.
*/

package com.wulian.icam.demo.view;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName: MD5
 * @Function:  TODO
 * @Date:      2015年12月6日
 * @author     Puml
 * @email      puml@wuliangroup.cn
 */
public class MD5 {
	/**
	 * 
	 * @MethodName MD52
	 * @Function MD5加密
	 * @author Puml
	 * @date: 2014-9-9
	 * @email puml@wuliangroup.cn
	 * @param MD5
	 *            加密字符串
	 * @return
	 */
	public static String MD52(String MD5) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		String part = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5 = md.digest(MD5.getBytes());
			for (int i = 0; i < md5.length; i++) {
				part = Integer.toHexString(md5[i] & 0xFF);
				if (part.length() == 1) {
					part = "0" + part;
				}
				sb.append(part);
			}
		} catch (NoSuchAlgorithmException ex) {
			throw new NoSuchAlgorithmException(ex);
		} 
		return sb.toString();
	}
}

