package com.whr.taskmanager;

import java.lang.reflect.Field;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

public class NewTaskActivity extends Activity implements OnClickListener {

	private static final int REQUEST_CODE_SEARCH = 1;
	private static final int SHOW_LOCATION = 0x001;
	Button mDateBtn;
	Button mTimeBtn;

	TextView mLocationHint;
	ImageView mLocaitonImage;

	public Switch mSwitch;
	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_LOCATION:
				mLocationHint.setText((String) msg.obj);
				break;

			default:
				break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_new_task);
		mSwitch = new Switch(this);
		mSwitch.setChecked(false);
		mSwitch.setPadding(0, 0, 0, 0);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM);
		getActionBar().setCustomView(
				mSwitch,
				new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
						ActionBar.LayoutParams.WRAP_CONTENT,
						Gravity.CENTER_VERTICAL | Gravity.RIGHT));
		getActionBar().setTitle("任务详情输入");
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
		}

		mDateBtn = (Button) findViewById(R.id.dateBtn);
		mTimeBtn = (Button) findViewById(R.id.timeBtn);
		mLocationHint = (TextView) findViewById(R.id.locationHint);
		mLocaitonImage = (ImageView) findViewById(R.id.locationImage);

		mLocaitonImage.setOnClickListener(this);
		mDateBtn.setOnClickListener(this);
		mTimeBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dateBtn:
			DatePickerDialog datePicker = new DatePickerDialog(this,
					new OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {

						}
					}, 2013, 7, 20);
			datePicker.show();
			break;

		case R.id.timeBtn:
			TimePickerDialog time = new TimePickerDialog(this,
					new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {

						}
					}, 18, 25, true);
			time.show();
			break;

		case R.id.locationImage:
			Intent intent = new Intent(NewTaskActivity.this,
					PoiSearchActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SEARCH);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_new_task, menu);

		return true;
	}

	
}
