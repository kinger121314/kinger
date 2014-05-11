package com.whr.taskmanager.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.whr.taskmanager.AlarmAlertActivity;
import com.whr.taskmanager.R;
import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.bean.Task.MentionAction;
import com.whr.taskmanager.bean.Task.ModifyAction;
import com.whr.taskmanager.bean.Task.RepeatAction;
import com.whr.taskmanager.bean.Task.Status;
import com.whr.taskmanager.bean.User;
import com.whr.taskmanager.util.JsonParser;
import com.whr.taskmanager.util.PushUtils;
import com.whr.taskmanager.util.TaskManagerDB;
import com.whr.taskmanager.util.TaskManagerUtil;

public class TaskManagerService extends Service {

	private static final String TAG = "TaskManager";

	private static final String LOGIN_URL = "http://192.168.0.106:8080/TaskManagerDemo/LoginServlet";

	private static final String REGISTER_URL = "http://192.168.0.106:8080/TaskManagerDemo/UpdateServlet";

	private static final String PUSH_PUSH_MSG_URL = "http://192.168.0.106:8080/TaskManagerDemo/UpdateServlet";

	private static final String PUSH_LOCATION_URL = "http://192.168.0.106:8080/TaskManagerDemo/UpdateServlet";

	private static final String PUSH_MSG_URL = "http://192.168.0.106:8080/TaskManagerDemo/UpdateServlet";

	private static final int ERROR_MESSAGE = 0x999;

	private static final int LOGIN = 0x001;

	private MyIBinder mIBinder = new MyIBinder();

	/**
	 * 是否初始化结束
	 */
	private boolean mIsIniting = false;
	/**
	 * 是否通过推送验证，并获得信息
	 */
	private boolean mIsGetPushMsg = false;

	private boolean mIsGetTruePushMsg = false;

	private ArrayList<Task> mTotalTasks;
	/**
	 * 用户
	 */
	private static User mUser;

	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

	private SharedPreferences mSharedPreferences;

	private Thread mInitThread = null;

	NotificationManager mNotificationManager;

	AlarmBroadCast alarmBroadCast;

	@Override
	public IBinder onBind(Intent arg0) {
		return mIBinder;
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case ERROR_MESSAGE:
				// 反馈错误信息
				mIBinder.mCallBack.errorHasHappen(msg.getData().getString(
						"errorMsg"));
				break;

			case LOGIN:
				// 启动初始化线程
				mSharedPreferences.edit()
						.putString("lastLoginUserName", (String) msg.obj)
						.commit();
				mUser.setUserName((String) msg.obj);
				mIsIniting = true;
				if (mInitThread != null) {
					mInitThread = null;
				}
				if (mInitThread == null) {
					mInitThread = new Thread(mInitLocalRunnable);
					mInitThread.start();
				}

				break;

			default:
				break;
			}
			return false;
		}
	});

	public class MyIBinder extends Binder {
		TaskManagerServiceCallBack mCallBack;

		public void login(final String userName, final String password) {
			if ("admin".equals(userName) && "123".equals(password)) {
				// 添加服务器最后一次更新时间
				mUser.setLastServerTime(0);
				mHandler.obtainMessage(LOGIN, userName).sendToTarget();
				return;
			}
			// 添加服务器最后一次更新时间
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("UserName", userName);
			params.put("Password", password);
			client.post(LOGIN_URL, params, new AsyncHttpResponseHandler() {

				@Override
				public void onSuccess(String content) {
					Log.d(TAG, "Login:" + content);
					Message message = new Message();
					Bundle bundle = new Bundle();
					try {
						JSONObject jsonContent = new JSONObject(content);
						String resUserName = jsonContent.getString("UserName");
						// 如果服务器反馈回来的不是正确的信息
						if (userName.equals(resUserName)) {
							String resResult = jsonContent.getString("Result");
							if ("Success".equals(resResult)) {
								long resResponseTest = jsonContent
										.getLong("ResponseTest");
								// 添加服务器最后一次更新时间
								mUser.setLastServerTime(resResponseTest);
								mHandler.obtainMessage(LOGIN, userName)
										.sendToTarget();
							} else if ("Error".equals(resResult)) {
								String errorMsg = jsonContent
										.getString("ResponseTest");
								message.what = ERROR_MESSAGE;
								bundle.putString("errorMsg", errorMsg);
								message.setData(bundle);
								mHandler.sendMessage(message);
							}
						} else {
							String errorMsg = "服务器反馈信息出错";
							message.what = ERROR_MESSAGE;
							bundle.putString("errorMsg", errorMsg);
							message.setData(bundle);
							mHandler.sendMessage(message);
						}
					} catch (Exception e) {
						e.printStackTrace();
						onFailure(0, null, null, e);
					}
				}

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2,
						Throwable arg3) {
					Message message = new Message();
					Bundle bundle = new Bundle();
					String errorMsg = "服务器无响应或网络出错";
					message.what = ERROR_MESSAGE;
					bundle.putString("errorMsg", errorMsg);
					message.setData(bundle);
					mHandler.sendMessage(message);
				}
			});

		}

		public void register(final String userName, final String password) {
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("UserName", userName);
			params.put("Password", password);

			client.post(REGISTER_URL, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					Log.d(TAG, "Register:" + content);
					Message message = new Message();
					Bundle bundle = new Bundle();
					try {
						JSONObject jsonContent = new JSONObject(content);
						String resUserName = jsonContent.getString("UserName");
						// 如果服务器反馈回来的不是正确的信息
						if (userName.equals(resUserName)) {
							String resResult = jsonContent.getString("Result");
							if ("Success".equals(resResult)) {
								long resResponseTest = jsonContent
										.getLong("ResponseTest");
								// 添加服务器最后一次更新时间,last request time
								mUser.setLastServerTime(resResponseTest);
								mUser.setLastRequestTime(resResponseTest);
								mHandler.obtainMessage(LOGIN, userName)
										.sendToTarget();
							} else if ("Error".equals(resResult)) {
								String errorMsg = jsonContent
										.getString("ResponseTest");
								message.what = ERROR_MESSAGE;
								bundle.putString("errorMsg", errorMsg);
								message.setData(bundle);
								mHandler.sendMessage(message);
							}
						} else {
							String errorMsg = "服务器反馈信息出错";
							message.what = ERROR_MESSAGE;
							bundle.putString("errorMsg", errorMsg);
							message.setData(bundle);
							mHandler.sendMessage(message);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2,
						Throwable arg3) {
					arg3.printStackTrace();

					Message message = new Message();
					Bundle bundle = new Bundle();
					String errorMsg = "服务器无响应或网络出错";
					message.what = ERROR_MESSAGE;
					bundle.putString("errorMsg", errorMsg);
					message.setData(bundle);
					mHandler.sendMessage(message);
				}
			});
		}

		public void requestDataList() {
			mCallBack.initTasksData(mTotalTasks);
		}

		public void changeTaskStatus(long createTime, boolean status) {
			Task tmp = null;
			int changeIndex = -1;
			for (int i = 0; i < mTotalTasks.size(); i++) {
				tmp = mTotalTasks.get(i);
				if (tmp.getCreateTime() == createTime) {
					tmp.setModifyAction(ModifyAction.UPDATE);
					tmp.setModifyTime(System.currentTimeMillis());
					if (!status) {
						tmp.setStatus(Status.NOTACTIVE);
						cancelAlarm(tmp);
					} else {
						tmp.setStatus(Status.ACTIVE);
						startAlarm(tmp);
					}
					changeIndex = i;
					break;
				}
			}
			mTotalTasks.remove(changeIndex);
			mTotalTasks.add(tmp);
			// 反馈数据回去
			mCallBack.initTasksData(mTotalTasks);

		}

		public void deleteTask(long createTime) {
			int deleteIndex = -1;
			for (int i = 0; i < mTotalTasks.size(); i++) {
				Task tmp = mTotalTasks.get(i);
				if (tmp.getCreateTime() == createTime) {
					deleteIndex = i;
					break;
				}
			}
			Task delete = mTotalTasks.remove(deleteIndex);
			delete.setModifyAction(ModifyAction.DELETE);
			delete.setModifyTime(System.currentTimeMillis());
			cancelAlarm(delete);
			// 更新界面
			mCallBack.initTasksData(mTotalTasks);
			// 提交服务器
			String msg = JsonParser.getJsonStringFromTasks(delete);
			if (msg != null) {
				pushTaskMsg(msg, 1);
			}
			// 进入数据库
			TaskManagerDB.getInstance(TaskManagerService.this).deleteTask(
					mUser.getUserName(), delete);

		}

		public Task getTaskMsgFromCreateTime(long createTime) {
			Task tmp = null;
			for (int i = 0; i < mTotalTasks.size(); i++) {
				if (mTotalTasks.get(i).getCreateTime() == createTime) {
					return mTotalTasks.get(i);
				}
			}
			return tmp;
		}

		public void addNewTask(Task task) {

			mTotalTasks.add(task);
			startAlarm(task);
			// 提交服务器
			String msg = JsonParser.getJsonStringFromTasks(task);
			if (msg != null) {
				pushTaskMsg(msg, 1);
			}
			// 进入数据库
			TaskManagerDB.getInstance(TaskManagerService.this).insertTask(
					mUser.getUserName(), task);
		}

		public void updateTask(Task task, boolean isCallAlarm) {
			int updateIndex = -1;
			for (int i = 0; i < mTotalTasks.size(); i++) {
				if (mTotalTasks.get(i).getCreateTime() == task.getCreateTime()) {
					updateIndex = i;
					break;
				}
			}
			if (updateIndex == -1) {
				return;
			}
			mTotalTasks.remove(updateIndex);
			mTotalTasks.add(task);
			// 提交服务器
			String msg = JsonParser.getJsonStringFromTasks(task);
			if (msg != null) {
				pushTaskMsg(msg, 1);
			}
			cancelAlarm(task);
			if (isCallAlarm) {
				startAlarm(task);
			}
			// 进入数据库
			TaskManagerDB.getInstance(TaskManagerService.this).updateTask(
					mUser.getUserName(), task);
		}

		public double getLatitude() {
			return mUser.getLatitude();
		}

		public double getLongitude() {
			return mUser.getLongitude();
		}

		/**
		 * 注册回调
		 * 
		 * @param callBack
		 */
		public void registerCallBack(TaskManagerServiceCallBack callBack) {
			this.mCallBack = callBack;
		}

		/**
		 * 注销回调
		 */
		public void unRegisterCallBack() {
			this.mCallBack = null;
		}
	}

	private BroadcastReceiver mInternetStatus = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean success = false;
			// 获得网络连接服务
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			// 获取WIFI网络连接状态
			State state = connManager.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();
			// 判断是否正在使用WIFI网络
			if (State.CONNECTED == state) {
				success = true;
			}
			// 获取GPRS网络连接状态
			state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState();
			// 判断是否正在使用GPRS网络
			if (State.CONNECTED == state) {
				success = true;
			}
		}
	};

	private BroadcastReceiver mCatchPushMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
				// 获取消息内容
				String message = intent.getExtras().getString(
						PushConstants.EXTRA_PUSH_MESSAGE_STRING);
				// 消息的用户自定义内容读取方式
				Log.d(TAG, "PushMsg: " + message);
				getMsgFromServer(message);

			} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
				String content = "";
				int errorCode = intent.getIntExtra(
						PushConstants.EXTRA_ERROR_CODE,
						PushConstants.ERROR_SUCCESS);

				if (intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT) != null) {
					content = new String(
							intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
				}
				if (errorCode == 0) {
					try {
						JSONObject jsonContent = new JSONObject(content);
						JSONObject params = jsonContent
								.getJSONObject("response_params");
						Log.d(TAG, "content:" + content);
						mUser.setChannelID(Long.parseLong(params
								.getString("channel_id")));
						mUser.setUserID(params.getString("user_id"));
						mIsGetTruePushMsg = true;
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				mIsGetPushMsg = true;
			}
		}
	};

	public class AlarmBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long createTime = intent.getLongExtra("createTime", -1L);
			Task task = mIBinder.getTaskMsgFromCreateTime(createTime);
			Intent i = new Intent(context, AlarmAlertActivity.class);
			Bundle bundleRet = new Bundle();
			bundleRet.putString("title", "" + task.getTitle());
			bundleRet.putString("content", "" + task.getContent());
			for (int j = 0; j < task.getMentionAction().size(); j++) {
				MentionAction action = task.getMentionAction().get(j);
				if (action.equals(MentionAction.VOICE)) {
					bundleRet.putBoolean("voice", true);
				}
				if (action.equals(MentionAction.SHAKE)) {
					bundleRet.putBoolean("vibrator", true);
				}
			}
			i.putExtras(bundleRet);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);

			// 更新任务
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
			for (int j = 0; j < task.getReaptAction().size(); j++) {
				Calendar tmp = Calendar.getInstance();
				tmp.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
				RepeatAction repeat = task.getReaptAction().get(j);
				int add = 0;
				switch (repeat) {
				case MONDAY:
					add = 1;
					break;
				case TUESDAY:
					add = 2;
					break;
				case WEDNESDAY:
					add = 3;
					break;
				case THURSDAY:
					add = 4;
					break;
				case FRIDAY:
					add = 5;
					break;
				case SATURDAY:
					add = 6;
					break;
				case SUNDAY:
					add = 0;
					break;

				default:
					break;
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
			if (isShow) {
				task.setStatus(Status.NOTACTIVE);
				mIBinder.updateTask(task, false);
			} else {
				Date nextDate = new Date(task.getExpireTime());
				nextDate.setDate(date.getDate());
				nextDate.setMonth(date.getMonth());
				nextDate.setYear(date.getYear());
				task.setExpireTime(nextDate.getTime());
				mIBinder.updateTask(task, true);
			}
		}
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			mUser.setLatitude(location.getLatitude());
			mUser.setLongitude(location.getLongitude());
			mUser.setAddrStr(location.getAddrStr());
			pushLocationMsg(0);
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化一个用户
		mUser = new User();
		// 初始化sharePreference
		mSharedPreferences = getSharedPreferences("TaskManager", 0);
		// 获取最后一次登录用户名
		mUser.setUserName(mSharedPreferences.getString("lastLoginUserName", ""));
		// 获取通知管理器对象
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// 以apikey的方式登录,启动推送
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY,
				PushUtils.getMetaValue(getApplicationContext(), "api_key"));
		// 闹钟
		alarmBroadCast = new AlarmBroadCast();
		IntentFilter filterAlarm = new IntentFilter();
		filterAlarm.addAction("com.whr.alarm");
		registerReceiver(alarmBroadCast, filterAlarm);
		// 动态注册推送广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.baidu.android.pushservice.action.MESSAGE");
		filter.addAction("com.baidu.android.pushservice.action.RECEIVE");
		registerReceiver(mCatchPushMessage, filter);

		// 动态注册网络广播
		IntentFilter filterForNet = new IntentFilter();
		filterForNet.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mInternetStatus, filterForNet);

		// 获取自身所在地理位置
		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度
		option.setScanSpan(1000 * 60 * 5);// 扫描间隔5分钟
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		// 4.启动地理位置，允许向服务器推送地理位置
		mLocationClient.start();
		// 启动初始化线程
		if (mInitThread != null) {
			mInitThread = null;
		}

		if (mInitThread == null) {
			mInitThread = new Thread(mInitLocalRunnable);
			mInitThread.start();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销广播
		try {
			unregisterReceiver(mCatchPushMessage);
			unregisterReceiver(mInternetStatus);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 初始化
	 */
	private Runnable mInitLocalRunnable = new Runnable() {

		@Override
		public void run() {
			if (!"".equals(mUser.getUserName())) {
				// 1.根据账号，初始化数据
				mTotalTasks = TaskManagerDB
						.getInstance(getApplicationContext()).getAllTasks(
								mUser.getUserName());
				mIsIniting = true;
				// 3.向服务器上传推送账号
				pushPushMsg(0);
				// 4.关闭闹钟
				for (int i = 0; i < mTotalTasks.size(); i++) {
					Task tmp = mTotalTasks.get(i);
					if (tmp.getStatus() == Status.ACTIVE) {
						cancelAlarm(tmp);
					}
				}
				// 5.根据时间，向服务器上传客户端的任务
				ArrayList<Task> mNeedUpdateToServer = new ArrayList<Task>();
				for (int i = 0; i < mTotalTasks.size(); i++) {
					Task tmp = mTotalTasks.get(i);
					// 将比服务器上一次更新创建时间小的上传
					if (tmp.getCreateTime() > mUser.getLastServerTime()) {
						mNeedUpdateToServer.add(tmp);
					}
					if (tmp.getStatus() == Status.ACTIVE) {
						startAlarm(tmp);
					}
				}
				String msg = JsonParser
						.getJsonStringFromTasks(mNeedUpdateToServer);
				if (msg != null) {
					pushTaskMsg(msg, 1);
				}
				// 通知登录成功，注册成功
				if (mIBinder.mCallBack != null) {
					mIBinder.mCallBack.loginSuccess();
					mIBinder.mCallBack.registerSuccess();
				}
			}
		}
	};

	/**
	 * 向服务器上传推送账号
	 * 
	 * @param ctnCount
	 *            续传次数
	 */
	private void pushPushMsg(final int ctnCount) {
		if (!mIsIniting || !mIsGetPushMsg) {
			// 3秒后重发
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (ctnCount > 0) {
						pushPushMsg(ctnCount - 1);
					}
				}
			}, 3000);
			return;
		}
		// 如果正确获取了推送账号
		if (mIsGetTruePushMsg) {
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("UserName", mUser.getUserName());
			params.put("ChannelID", mUser.getChannelID());
			params.put("UserID", mUser.getUserID());
			params.put("LastRequestTime", mUser.getLastRequestTime());
			client.post(PUSH_PUSH_MSG_URL, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							arg3.printStackTrace();
							// 是否需要重发
							if (ctnCount > 0) {
								pushPushMsg(ctnCount - 1);
							}
						}
					});
		}
	}

	/**
	 * 向服务器推送地理位置
	 * 
	 * @param ctnCount
	 *            续传次数
	 */
	private void pushLocationMsg(final int ctnCount) {
		if (!mIsIniting) {
			return;
		}
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("UserName", mUser.getUserName());
		params.put("Longitude", mUser.getLongitude());
		params.put("Latitude", mUser.getLatitude());
		client.post(PUSH_LOCATION_URL, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				arg3.printStackTrace();
				// 是否需要重发
				if (ctnCount > 0) {
					pushLocationMsg(ctnCount - 1);
				}
			}
		});
		GeoPoint pt1 = new GeoPoint((int) (mUser.getLatitude() * 1e6 ),
				(int) (mUser.getLongitude() * 1e6));
		Log.d(TAG, "location:" + (int) (mUser.getLongitude() * 1e6) + " "
				+ (int) (mUser.getLatitude() * 1e6));
		for (int i = 0; i < mTotalTasks.size(); i++) {
			Task tmp = mTotalTasks.get(i);
			if (tmp.getStatus() != Status.ACTIVE) {
				continue;
			}
			if (tmp.getLatitude() > 0 && tmp.getLongitude() > 0) {
				GeoPoint pt2 = new GeoPoint((int) (tmp.getLatitude() * 1e6),
						(int) (tmp.getLongitude() * 1e6));
				Log.d(TAG, "task:" + (int) (tmp.getLongitude() * 1e6) + " "
						+ (int) (tmp.getLatitude() * 1e6));
				double distance = DistanceUtil.getDistance(pt1, pt2);
				Log.d(TAG, "distance:" + distance);
				if (distance <= 300.0) {
					showNotification(i, tmp.getTitle(), tmp.getAdress() + "剩余"
							+ distance + "米");
				}
			}
		}
	}

	public void showNotification(int id, String title, String content) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				TaskManagerService.this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(content);
		mBuilder.setTicker("地理位置提醒");// 第一次提示消息的时候显示在通知栏上
		mBuilder.setAutoCancel(true);// 自己维护通知的消失
		mBuilder.setDefaults(Notification.DEFAULT_ALL);
		mNotificationManager.notify(id, mBuilder.build());
	}

	/**
	 * 推送任务信息与服务器
	 * 
	 * @param msg
	 * @param ctnCount
	 */
	private void pushTaskMsg(final String msg, final int ctnCount) {
		if (!mIsIniting) {
			return;
		}
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("UserName", mUser.getUserName());
		params.put("Tasks", msg);
		client.post(PUSH_MSG_URL, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				arg3.printStackTrace();
				// 是否需要重发
				if (ctnCount > 0) {
					pushTaskMsg(msg, ctnCount - 1);
				}
			}
		});
	}

	/**
	 * 从服务器获取信息
	 * 
	 * @param msg
	 */
	private void getMsgFromServer(String msg) {
		ArrayList<Task> getFromServer = JsonParser.getTasksFromJsonString(msg,
				mUser);
		if (getFromServer.size() != 0) {
			// 加进数组中，更新界面，更新数据库
			mTotalTasks.addAll(getFromServer);

		}
	}

	private void startAlarm(Task task) {
		Intent intent = new Intent("com.whr.alarm");
		int id = (int) task.getCreateTime();
		intent.putExtra("createTime", task.getCreateTime());
		PendingIntent sender = PendingIntent.getBroadcast(
				TaskManagerService.this, id, intent, 0);
		AlarmManager am;
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		String msg = TaskManagerUtil.changeToDateAndTimeFormat(task
				.getExpireTime());

		int year = Integer.parseInt(msg.substring(0, msg.indexOf("年")));
		int month = Integer.parseInt(msg.substring(msg.indexOf("年") + 1,
				msg.indexOf("月"))) - 1;
		int day = Integer.parseInt(msg.substring(msg.indexOf("月") + 1,
				msg.indexOf("日")));
		int hour = Integer.parseInt(msg.substring(msg.indexOf("日") + 1,
				msg.indexOf(":")));
		int minute = Integer.parseInt(msg.substring(msg.indexOf(":") + 1,
				msg.length()));
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(year, month, day, hour, minute, 0);

		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}

	private void cancelAlarm(Task task) {
		int id = (int) task.getCreateTime();
		Intent intent = new Intent(TaskManagerService.this,
				TaskManagerService.AlarmBroadCast.class);
		PendingIntent sender = PendingIntent.getBroadcast(
				TaskManagerService.this, id, intent, 0);
		AlarmManager am;

		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
}
