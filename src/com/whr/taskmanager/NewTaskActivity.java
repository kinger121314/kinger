package com.whr.taskmanager;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.bean.Task.ImportLevel;
import com.whr.taskmanager.bean.Task.MentionAction;
import com.whr.taskmanager.bean.Task.ModifyAction;
import com.whr.taskmanager.bean.Task.RepeatAction;
import com.whr.taskmanager.bean.Task.Status;
import com.whr.taskmanager.bean.VoiceMsg;
import com.whr.taskmanager.service.TaskManagerService;
import com.whr.taskmanager.service.TaskManagerService.MyIBinder;
import com.whr.taskmanager.service.TaskManagerServiceCallBack;
import com.whr.taskmanager.util.TaskManagerUtil;

/**
 * 
 * 新任务
 * 
 * @author kinger
 * 
 */
public class NewTaskActivity extends Activity implements OnClickListener {

	private static final String TAG = "TaskManager";

	private static final int REQUEST_CODE_SEARCH = 1;
	private static final int SHOW_LOCATION = 0x001;

	private static final int MODIFY_TASK = 0x002;

	private static final int VOICE_TASK = 0x003;

	private static final int CHANGE_DATE = 0x004;
	private static final int CHANGE_TIME = 0x005;

	private Task mTask;

	private EditText mTitle;

	private EditText mContent;

	TextView mLocationHint;
	ImageView mDeleteLocation;
	ImageView mLocaitonImage;

	ArrayList<CheckBox> mRepatArrs;

	CheckBox mMon;
	CheckBox mTue;
	CheckBox mWeb;
	CheckBox mThu;
	CheckBox mFri;
	CheckBox mSat;
	CheckBox mSun;

	private TextView mDateHint;
	private TextView mTimeHint;
	Button mDateBtn;
	Button mTimeBtn;

	private RatingBar mImportLevel;

	private ToggleButton mVoice;

	private ToggleButton mShake;

	MyIBinder mBinder;
	TaskManagerServiceCallBack mCallBack;
	ServiceConnection mConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBinder.unRegisterCallBack();
			mBinder = null;
		}

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			mBinder = (MyIBinder) arg1;
			mBinder.registerCallBack(mCallBack);
		}
	};

	/**
	 * 是否是新建任务
	 */
	private boolean mIsNewCreate = true;

	private OnCheckedChangeListener mDefaultCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			checkIsShowDateBtn();
		}
	};

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			String dateTime = null;
			long createTime;
			long changeLongTime;
			switch (msg.what) {
			case SHOW_LOCATION:
				mLocaitonImage.setVisibility(View.GONE);
				mDeleteLocation.setVisibility(View.VISIBLE);
				mLocationHint.setText((String) msg.obj);
				break;

			case MODIFY_TASK:
				mIsNewCreate = false;
				createTime = (Long) msg.obj;
				Task origin = mBinder.getTaskMsgFromCreateTime(createTime);
				mTask.setCreateTime(createTime);
				mTitle.setText(origin.getTitle());
				mContent.setText(origin.getContent());
				if (!"".equals(origin.getAdress())) {
					mLocationHint.setText(origin.getAdress());
					mLocaitonImage.setVisibility(View.GONE);
					mDeleteLocation.setVisibility(View.VISIBLE);
				}
				ArrayList<RepeatAction> repeatActions = origin.getReaptAction();
				for (int i = 0; i < repeatActions.size(); i++) {
					RepeatAction tmp = repeatActions.get(i);
					switch (tmp) {
					case MONDAY:
						mMon.setChecked(true);
						break;

					case TUESDAY:
						mTue.setChecked(true);
						break;

					case WEDNESDAY:
						mWeb.setChecked(true);
						break;

					case THURSDAY:
						mThu.setChecked(true);
						break;

					case FRIDAY:
						mFri.setChecked(true);
						break;

					case SATURDAY:
						mSat.setChecked(true);
						break;

					case SUNDAY:
						mSun.setChecked(true);
						break;

					default:
						break;
					}
				}
				mDateHint.setText(TaskManagerUtil.changeToDateFormat(origin
						.getExpireTime()));
				mTimeHint.setText(TaskManagerUtil.changeToTimeFormat(origin
						.getExpireTime()));
				switch (origin.getImportLevel()) {
				case COMMON:
					mImportLevel.setRating(1.0f);
					break;

				case HIGHER:
					mImportLevel.setRating(2.0f);
					break;

				case HIGHEST:
					mImportLevel.setRating(3.0f);
					break;

				default:
					break;
				}
				ArrayList<MentionAction> mentionAction = origin
						.getMentionAction();
				mVoice.setChecked(false);
				mShake.setChecked(false);
				for (int i = 0; i < mentionAction.size(); i++) {
					switch (mentionAction.get(i)) {
					case VOICE:
						mVoice.setChecked(true);
						break;

					case SHAKE:
						mShake.setChecked(true);
						break;

					default:
						break;
					}
					checkIsShowDateBtn();
				}
				break;

			case VOICE_TASK:
				VoiceMsg voiceMsg = (VoiceMsg) msg.obj;
				mTitle.setText("" + voiceMsg.voiceMsg);
				mContent.setText("" + voiceMsg.voiceMsg);
				String date = TaskManagerUtil.changeToDateFormat(voiceMsg.date);
				String time = TaskManagerUtil.changeToTimeFormat(voiceMsg.time);
				if (date != null) {
					mDateHint.setText(date);
				}
				if (time != null) {
					mTimeHint.setText(time);
				}
				break;

			case CHANGE_DATE:
				dateTime = (String) msg.obj + mTimeHint.getText().toString();
				changeLongTime = TaskManagerUtil.changeToLongTime(dateTime);
				if (mDateBtn.isEnabled()
						&& changeLongTime <= System.currentTimeMillis()) {
					Toast.makeText(NewTaskActivity.this,
							dateTime + "设置时间小于现在时间", Toast.LENGTH_SHORT).show();
					break;
				}
				mDateHint.setText((String) msg.obj);
				break;

			case CHANGE_TIME:
				dateTime = mDateHint.getText().toString() + (String) msg.obj;
				changeLongTime = TaskManagerUtil.changeToLongTime(dateTime);
				if (mDateBtn.isEnabled()
						&& changeLongTime <= System.currentTimeMillis()) {
					Toast.makeText(NewTaskActivity.this,
							dateTime + "设置时间小于现在时间", Toast.LENGTH_SHORT).show();
					break;
				}
				mTimeHint.setText((String) msg.obj);
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

		init();

		mTask = new Task();

		mCallBack = new TaskManagerServiceCallBack() {

			@Override
			public void errorHasHappen(String errorMsg) {
			}

			@Override
			public void loginSuccess() {
			}

			@Override
			public void registerSuccess() {
			}

			@Override
			public void initTasksData(ArrayList<Task> tasks) {
			}
		};
		// 启动服务
		Intent service = new Intent(this, TaskManagerService.class);
		startService(service);
		bindService(service, mConn, Context.BIND_AUTO_CREATE);
		// 读取启动数据
		final Intent recIntent = getIntent();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (recIntent != null) {
					// Modify
					long createTime = recIntent.getLongExtra("task", -1);
					if (createTime != -1) {
						mHandler.obtainMessage(MODIFY_TASK, createTime)
								.sendToTarget();
					}
					// new Voice
					else {
						String voiceMsg = recIntent.getStringExtra("voiceMsg");
						String date = recIntent.getStringExtra("date");
						String time = recIntent.getStringExtra("time");
						String repeat = recIntent.getStringExtra("repeat");
						if (voiceMsg != null) {
							VoiceMsg msg = new VoiceMsg();
							msg.voiceMsg = voiceMsg;
							if (date != null) {
								msg.date = date;
							}
							if (time != null) {
								msg.time = time;
							}

							mHandler.obtainMessage(VOICE_TASK, msg)
									.sendToTarget();
						}
					}
				}
			}
		}, 0);
	}

	/**
	 * 初始化函数
	 */
	private void init() {
		mTitle = (EditText) findViewById(R.id.title);
		mContent = (EditText) findViewById(R.id.content);
		mLocationHint = (TextView) findViewById(R.id.locationHint);
		mDeleteLocation = (ImageView) findViewById(R.id.deletelocation);
		mLocaitonImage = (ImageView) findViewById(R.id.locationImage);

		mRepatArrs = new ArrayList<CheckBox>();

		mMon = (CheckBox) findViewById(R.id.cb_mon);
		mMon.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mMon);

		mTue = (CheckBox) findViewById(R.id.cb_tue);
		mTue.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mTue);

		mWeb = (CheckBox) findViewById(R.id.cb_wed);
		mWeb.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mWeb);

		mThu = (CheckBox) findViewById(R.id.cb_thu);
		mThu.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mThu);

		mFri = (CheckBox) findViewById(R.id.cb_fri);
		mFri.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mFri);

		mSat = (CheckBox) findViewById(R.id.cb_sat);
		mSat.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mSat);

		mSun = (CheckBox) findViewById(R.id.cb_sun);
		mSun.setOnCheckedChangeListener(mDefaultCheckedChangeListener);
		mRepatArrs.add(mSun);

		mDateHint = (TextView) findViewById(R.id.dataHint);
		mTimeHint = (TextView) findViewById(R.id.timeHint);
		mDateHint.setText(TaskManagerUtil.changeToDateFormat(System
				.currentTimeMillis()));
		mTimeHint.setText(TaskManagerUtil.changeToTimeFormat(System
				.currentTimeMillis()));

		mDateBtn = (Button) findViewById(R.id.dateBtn);
		mTimeBtn = (Button) findViewById(R.id.timeBtn);

		mImportLevel = (RatingBar) findViewById(R.id.ratingBar);
		mVoice = (ToggleButton) findViewById(R.id.swith_voice);
		mShake = (ToggleButton) findViewById(R.id.swith_shake);

		mDeleteLocation.setOnClickListener(this);
		mLocaitonImage.setOnClickListener(this);
		mDateBtn.setOnClickListener(this);
		mTimeBtn.setOnClickListener(this);
		mImportLevel
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
						if (rating <= 1.0f) {
							mImportLevel.setRating(1.0f);
						}
					}
				});

		mVoice.setChecked(PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("voice_default", false));
		mShake.setChecked(PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("shake_default", false));
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 取消绑定
		try {
			mBinder = null;
			unbindService(mConn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dateBtn:
			// FIXME hardcoding
			String date = mDateHint.getText().toString();

			int year = Integer.parseInt(date.substring(0, date.indexOf("年")));
			int month = Integer.parseInt(date.substring(date.indexOf("年") + 1,
					date.indexOf("月"))) - 1;
			int day = Integer.parseInt(date.substring(date.indexOf("月") + 1,
					date.length() - 1));

			DatePickerDialog datePicker = new DatePickerDialog(this,
					new OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							String date = year + "年" + (monthOfYear + 1) + "月"
									+ dayOfMonth + "日";
							mHandler.obtainMessage(CHANGE_DATE, date)
									.sendToTarget();
						}
					}, year, month, day);

			datePicker.show();
			break;

		case R.id.timeBtn:
			String time = mTimeHint.getText().toString();
			int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
			int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1,
					time.length()));
			TimePickerDialog timePicker = new TimePickerDialog(this,
					new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							String time = "";
							if (hourOfDay < 10) {
								time += "0";
							}
							if (minute < 10) {
								time += hourOfDay + ":0" + minute;
							} else {
								time += hourOfDay + ":" + minute;
							}
							mHandler.obtainMessage(CHANGE_TIME, time)
									.sendToTarget();
						}
					}, hour, minute, true);
			timePicker.show();
			break;

		case R.id.locationImage:
			Intent intent = new Intent(NewTaskActivity.this,
					PoiSearchActivity.class);

			startActivityForResult(intent, REQUEST_CODE_SEARCH);
			break;

		case R.id.deletelocation:
			mLocationHint.setText("");
			mLocaitonImage.setVisibility(View.VISIBLE);
			mDeleteLocation.setVisibility(View.GONE);
			mTask.setAdress("");
			mTask.setLatitude(-1);
			mTask.setLongitude(-1);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SEARCH
				&& resultCode == Activity.RESULT_OK) {
			String name = data.getStringExtra("name");
			long latiude = data.getLongExtra("latiude", -1);
			long longitude = data.getLongExtra("longitude", -1);
			mTask.setAdress(name);
			// 类型转换
			mTask.setLatitude(latiude / 1e6);
			mTask.setLongitude(longitude / 1e6);
			mHandler.obtainMessage(SHOW_LOCATION, name).sendToTarget();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_save:
			// 新建
			if (mIsNewCreate) {
				mTask.setCreateTime(System.currentTimeMillis());
				mTask.setModifyTime(System.currentTimeMillis());
				mTask.setModifyAction(ModifyAction.CREATE);
				mTask.setTitle(mTitle.getText().toString());
				mTask.setContent(mContent.getText().toString());
				String dateTime = mDateHint.getText().toString()
						+ mTimeHint.getText().toString();
				long changeLongTime = TaskManagerUtil
						.changeToLongTime(dateTime);
				mTask.setExpireTime(changeLongTime);
				ArrayList<RepeatAction> reaptAcions = new ArrayList<Task.RepeatAction>();
				RepeatAction reaptAction = null;
				if (mMon.isChecked()) {
					reaptAction = RepeatAction.MONDAY;
					reaptAcions.add(reaptAction);
				}
				if (mTue.isChecked()) {
					reaptAction = RepeatAction.TUESDAY;
					reaptAcions.add(reaptAction);
				}
				if (mWeb.isChecked()) {
					reaptAction = RepeatAction.WEDNESDAY;
					reaptAcions.add(reaptAction);
				}
				if (mThu.isChecked()) {
					reaptAction = RepeatAction.THURSDAY;
					reaptAcions.add(reaptAction);
				}
				if (mFri.isChecked()) {
					reaptAction = RepeatAction.FRIDAY;
					reaptAcions.add(reaptAction);
				}
				if (mSat.isChecked()) {
					reaptAction = RepeatAction.SATURDAY;
					reaptAcions.add(reaptAction);
				}
				if (mSun.isChecked()) {
					reaptAction = RepeatAction.SUNDAY;
					reaptAcions.add(reaptAction);
				}
				mTask.setReaptAction(reaptAcions);

				int importLevel = (int) mImportLevel.getRating();
				ImportLevel imLevel = null;
				switch (importLevel) {
				case 1:
					imLevel = ImportLevel.COMMON;
					break;

				case 2:
					imLevel = ImportLevel.HIGHER;
					break;

				case 3:
					imLevel = ImportLevel.HIGHEST;
					break;

				default:
					break;
				}
				mTask.setImportLevel(imLevel);
				mTask.setStatus(Status.ACTIVE);
				ArrayList<MentionAction> mentionActions = new ArrayList<MentionAction>();
				MentionAction ma = null;
				if (mVoice.isChecked()) {
					ma = MentionAction.VOICE;
					mentionActions.add(ma);
				}
				if (mShake.isChecked()) {
					ma = MentionAction.SHAKE;
					mentionActions.add(ma);
				}
				mTask.setMentionAction(mentionActions);
				mBinder.addNewTask(mTask);
			}
			// 修改
			else {
				mTask.setModifyTime(System.currentTimeMillis());
				mTask.setModifyAction(ModifyAction.UPDATE);
				mTask.setTitle(mTitle.getText().toString());
				mTask.setContent(mContent.getText().toString());
				String dateTime = mDateHint.getText().toString()
						+ mTimeHint.getText().toString();
				long changeLongTime = TaskManagerUtil
						.changeToLongTime(dateTime);
				mTask.setExpireTime(changeLongTime);
				ArrayList<RepeatAction> reaptAcions = new ArrayList<Task.RepeatAction>();
				RepeatAction reaptAction = null;
				if (mMon.isChecked()) {
					reaptAction = RepeatAction.MONDAY;
					reaptAcions.add(reaptAction);
				}
				if (mTue.isChecked()) {
					reaptAction = RepeatAction.TUESDAY;
					reaptAcions.add(reaptAction);
				}
				if (mWeb.isChecked()) {
					reaptAction = RepeatAction.WEDNESDAY;
					reaptAcions.add(reaptAction);
				}
				if (mThu.isChecked()) {
					reaptAction = RepeatAction.THURSDAY;
					reaptAcions.add(reaptAction);
				}
				if (mFri.isChecked()) {
					reaptAction = RepeatAction.FRIDAY;
					reaptAcions.add(reaptAction);
				}
				if (mSat.isChecked()) {
					reaptAction = RepeatAction.SATURDAY;
					reaptAcions.add(reaptAction);
				}
				if (mSun.isChecked()) {
					reaptAction = RepeatAction.SUNDAY;
					reaptAcions.add(reaptAction);
				}
				mTask.setReaptAction(reaptAcions);
				int importLevel = (int) mImportLevel.getRating();
				ImportLevel imLevel = null;
				switch (importLevel) {
				case 1:
					imLevel = ImportLevel.COMMON;
					break;

				case 2:
					imLevel = ImportLevel.HIGHER;
					break;

				case 3:
					imLevel = ImportLevel.HIGHEST;
					break;

				default:
					break;
				}
				mTask.setImportLevel(imLevel);
				mTask.setStatus(Status.ACTIVE);
				ArrayList<MentionAction> mentionActions = new ArrayList<MentionAction>();
				MentionAction ma = null;
				if (mVoice.isChecked()) {
					ma = MentionAction.VOICE;
					mentionActions.add(ma);
				}
				if (mShake.isChecked()) {
					ma = MentionAction.SHAKE;
					mentionActions.add(ma);
				}
				mTask.setMentionAction(mentionActions);

				mBinder.updateTask(mTask, true,true);
			}
			NewTaskActivity.this.finish();
			break;

		case R.id.action_cancel:
			// 取消就关闭该activity
			NewTaskActivity.this.finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_new_task, menu);

		return true;
	}

	/**
	 * 检查是否需要显示日期按钮
	 */
	private boolean checkIsShowDateBtn() {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		// FIXME hardcode 星期
		int week = c.get(Calendar.DAY_OF_WEEK);
		week = week - 1;
		if (week == 0) {
			week = 7;
		}
		Date date = c.getTime();
		boolean isShow = true;
		for (int i = 0; i < mRepatArrs.size(); i++) {
			Calendar tmp = Calendar.getInstance();
			tmp.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
			if (mRepatArrs.get(i).isChecked()) {
				int add = i + 1;
				if (add == 7) {
					add = 0;
				}
				tmp.add(Calendar.DAY_OF_WEEK, add);
				Date tmpDate = tmp.getTime();
				if (isShow) {
					isShow = false;
					date = tmpDate;
				}
				if (tmpDate.before(date)) {
					date = tmpDate;
				}
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
		if (isShow) {
			mDateHint.setText(sdf.format(date));
			mDateBtn.setEnabled(isShow);
			mDateHint.setVisibility(View.VISIBLE);
			mDateBtn.setVisibility(View.VISIBLE);
		} else {
			mDateBtn.setEnabled(isShow);
			mDateHint.setVisibility(View.GONE);
			mDateBtn.setVisibility(View.GONE);
		}
		return isShow;
	}

}
