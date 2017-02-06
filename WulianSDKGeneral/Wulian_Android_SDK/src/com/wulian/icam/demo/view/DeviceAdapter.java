/**
 * Project Name:  IntegerSmartRoot
 * File Name:     DeviceAdapter.java
 * Package Name:  cc.wulian.smarthomev5
 * @Date:         2015年4月20日
 * Copyright (c)  2015, wulian All Rights Reserved.
 */

package com.wulian.icam.demo.view;

import java.util.ArrayList;
import java.util.List;

import com.wulian.icam.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * @ClassName: DeviceAdapter
 * @Function: TODO
 * @Date: 2015年4月20日
 * @author Puml
 * @email puml@wuliangroup.cn
 */
public class DeviceAdapter extends BaseAdapter {
	private List<Device> deviceList;
	private Context context;
	private LayoutInflater mLayoutInflater;
	private OnClickListener settingClickEvent;

	public DeviceAdapter(Context context) {
		this.context = context;
		deviceList = new ArrayList<Device>();
		mLayoutInflater = LayoutInflater.from(context);
	}

	public void refreshList(List<Device> list) {
		deviceList.clear();
		if (list != null) {
			deviceList.addAll(list);
		}
		this.notifyDataSetChanged();
	}

	public void setDeviceSettingClickEvent(OnClickListener click) {
		settingClickEvent = click;
	}

	@Override
	public int getCount() {
		return deviceList.size();
	}

	@Override
	public Device getItem(int position) {
		return deviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = mLayoutInflater.inflate(R.layout.item_device_layout,
				parent, false);
		TextView tv = (TextView) view.findViewById(R.id.tv_devicename);
		Button bt = (Button) view.findViewById(R.id.bt_devicesetting);
//		Button bt_protect = (Button) view.findViewById(R.id.bt_protect);
		Device device = getItem(position);
		tv.setText(device.getNick());
		bt.setTag(position);
//		bt_protect.setTag(position);
		if (settingClickEvent != null) {
			bt.setOnClickListener(settingClickEvent);
//			bt_protect.setOnClickListener(settingClickEvent);
		}
		return view;
	}

}
