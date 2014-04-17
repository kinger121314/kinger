package com.whr.taskmanager.service;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.bean.User;
import com.whr.taskmanager.util.JsonParser;
import com.whr.taskmanager.util.PushUtils;
import com.whr.taskmanager.util.TaskManagerDB;

public class TaskManagerService extends Service {

	private static final String TAG = "TaskManager";

	private static final String LOGIN_URL = "http://www.baidu.com";

	private static final String REGISTER_URL = "http://www.baidu.com";

	private static final String PUSH_PUSH_MSG_URL = "http://www.baidu.com";

	private static final String PUSH_LOCATION_URL = "http://www.baidu.com";

	private static final String PUSH_MSG_URL = "http://www.baidu.com";

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
				mIsIniting = true;
				new Thread(mInitLocalRunnable).start();
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
		// 以apikey的方式登录,启动推送
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY,
				PushUtils.getMetaValue(getApplicationContext(), "api_key"));
		// 动态注册推送广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.baidu.android.pushservice.action.MESSAGE");
		filter.addAction("com.baidu.android.pushservice.action.RECEIVE");
		registerReceiver(mCatchPushMessage, filter);

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
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 注销广播
		try {
			unregisterReceiver(mCatchPushMessage);
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
			// 1.根据账号，初始化数据
			mTotalTasks = TaskManagerDB.getInstance(getApplicationContext())
					.getAllTasks(mUser.getUserName());
			// 2.通知客户端登陆成功，进入主界面
			mIBinder.mCallBack.loginSuccess();
			mIsIniting = true;
			// 3.向服务器上传推送账号
			pushPushMsg(0);
			// 4.启动地理位置，允许向服务器推送地理位置
			mLocationClient.start();
			// 5.根据时间，向服务器上传客户端的任务
			ArrayList<Task> mNeedUpdateToServer = new ArrayList<Task>();
			for (int i = 0; i < mTotalTasks.size(); i++) {
				Task tmp = mTotalTasks.get(i);
				// 将比服务器上一次更新创建时间小的上传
				if (tmp.getCreateTime() > mUser.getLastServerTime()) {
					mNeedUpdateToServer.add(tmp);
				}
			}
			String msg = JsonParser.getJsonStringFromTasks(mNeedUpdateToServer);
			if (msg != null) {
				pushTaskMsg(msg, 1);
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

	private void getMsgFromServer(String msg) {
		ArrayList<Task> getFromServer = JsonParser.getTasksFromJsonString(msg,
				mUser);
		if (getFromServer.size() != 0) {
			// 加进数组中，更新界面，更新数据库
			mTotalTasks.addAll(getFromServer);
			
		}
	}
}
