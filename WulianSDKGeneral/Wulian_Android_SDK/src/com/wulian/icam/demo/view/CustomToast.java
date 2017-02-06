package com.wulian.icam.demo.view;

import android.content.Context;
import android.widget.Toast;

/**
 * @Function: Global toast
 * @date: 2014年11月4日
 * @author Wangjj
 */
public abstract class CustomToast {

	public static void show(Context context, CharSequence text) {
		text = text.toString().replaceAll("\\(\\d{3}\\)", "");
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	public static void show(Context context, int textid) {
		String content = context.getResources().getString(textid);
		content = content.toString().replaceAll("\\(\\d{3}\\)", "");
		Toast.makeText(context, content, Toast.LENGTH_LONG).show();
	}
}