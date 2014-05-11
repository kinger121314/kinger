package com.whr.taskmanager.view;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKSearch;

public class MyPoiOverlay extends PoiOverlay {

	private static final String TAG = "TaskManager";
	MKSearch mSearch;
	private Context mContext;
	
	private onItemClickShow  mItemClickShow;

	public MyPoiOverlay(Activity activity, MapView mapView, MKSearch search) {
		super(activity, mapView);
		mSearch = search;
		mContext = activity;
	}

	@Override
	protected boolean onTap(int i) {
		super.onTap(i);
		MKPoiInfo info = getPoi(i);
		mItemClickShow.click(info);
		return true;
	}
	
	public interface onItemClickShow{
		public void click(MKPoiInfo info);
	}
	
	public void regeistOnItemClickShow(onItemClickShow itemClickShow){
		mItemClickShow = itemClickShow;
	}
}
