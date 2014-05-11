package com.whr.taskmanager.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.whr.taskmanager.R;
import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.util.TaskManagerUtil;

public class BaseFragment extends Fragment {
	public View mMainView;
	public ArrayList<Task> mlistItems = new ArrayList<Task>();
	public Context mContext;
	public MyAdapter adapter;
	public ListView listView;

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
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
		return mMainView;
	}

	public class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mlistItems.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.listview_item, null);
			ImageView level = (ImageView) convertView.findViewById(R.id.l1);
			TextView time = (TextView) convertView.findViewById(R.id.time);
			TextView title = (TextView) convertView.findViewById(R.id.name);
			TextView content = (TextView) convertView
					.findViewById(R.id.lastmsg);
			Task tmp = mlistItems.get(position);
			switch (tmp.getImportLevel()) {
			case COMMON:
				level.setImageResource(R.drawable.level3);
				break;

			case HIGHER:
				level.setImageResource(R.drawable.level4);
				break;

			case HIGHEST:
				level.setImageResource(R.drawable.level5);
				break;

			default:
				break;
			}
			time.setText(TaskManagerUtil.changeToDateAndTimeFormat(tmp
					.getExpireTime()));
			title.setText(tmp.getTitle());
			content.setText(tmp.getContent());
			return convertView;
		}
	}

}
