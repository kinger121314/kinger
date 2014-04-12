package com.whr.taskmanager.view;

import android.app.Activity;
import android.util.Log;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKSearch;

public class MyPoiOverlay extends PoiOverlay {

	private static final String TAG = "TaskManager";
	MKSearch mSearch;

	public MyPoiOverlay(Activity activity, MapView mapView, MKSearch search) {
		super(activity, mapView);
		mSearch = search;
	}

	@Override
	protected boolean onTap(int i) {
		super.onTap(i);
		MKPoiInfo info = getPoi(i);
		Log.d(TAG, "" + info.name);
		Log.d(TAG, "" + info.address);
		Log.d(TAG,
				"" + info.pt.getLatitudeE6() + " " + info.pt.getLongitudeE6());
		return true;
	}
}
