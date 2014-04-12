package com.whr.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.whr.taskmanager.R;

public class BaseFragment extends Fragment {
	public View mMainView;
	public static ArrayList<Map<String, Object>> mlistItems;
	public Context mContext;
	public SimpleAdapter adapter;
	public ListView listView;

	static {
		mlistItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 20; i++) {
			Map<String, Object> tmp = new HashMap<String, Object>();
			mlistItems.add(tmp);
		}
	}

	public BaseFragment() {
		super();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity.getApplicationContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMainView = inflater.inflate(R.layout.fragment_pager, container, false);
		listView = (ListView) mMainView.findViewById(R.id.list);
		adapter = new SimpleAdapter(mContext, mlistItems,
				R.layout.listview_item, new String[] { "name", "sex" },
				new int[] {});
		listView.setAdapter(adapter);
		return mMainView;
	}

}
