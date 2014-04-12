package com.whr.taskmanager.service;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
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
import com.whr.taskmanager.bean.User;
import com.whr.taskmanager.util.PushUtils;

public class TaskManagerService extends Service {

	private static final String TAG = "TaskManager";

	private static final String LOGIN_URL = "http://www.baidu.com";

	private static final int LOGIN = 0x001;

	private MyIBinder mIBinder = new MyIBinder();

	private static boolean mIsInit = false;

	private User user;

	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

	@Override
	public IBinder onBind(Intent arg0) {
		return mIBinder;
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case LOGIN:

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
					try {
						JSONObject jsonContent = new JSONObject(content);
						String resUserName = jsonContent.getString("UserName");
						// 如果服务器反馈回来的不是正确的信息
						if (userName.equals(resUserName)) {
							String resResult = jsonContent.getString("Result");
							if ("Success".equals(resResult)) {
								long resResponseTest = jsonContent
										.getLong("ResponseTest");
								
							} else if ("Error".equals(resResult)) {

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2,
						Throwable arg3) {
					arg3.printStackTrace();
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
				Log.d(TAG, "onMessage: " + message);
				// 自定义内容的json串
				Log.d(TAG,
						"EXTRA_EXTRA = "
								+ intent.getStringExtra(PushConstants.EXTRA_EXTRA));
			} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
				String content = "";
				if (intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT) != null) {
					content = new String(
							intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
				}

				try {
					JSONObject jsonContent = new JSONObject(content);
					JSONObject params = jsonContent
							.getJSONObject("response_params");
					user.setAppid(params.getString("appid"));
					user.setChannelID(Long.parseLong(params
							.getString("channel_id")));
					user.setUserID(params.getString("user_id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	};

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			user.setLatitude(location.getLatitude());
			user.setLongitude(location.getLongitude());
			user.setAddrStr(location.getAddrStr());
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (!mIsInit) {
			mIsInit = true;

			// 初始化一个用户
			user = new User();

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
			option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
			option.setScanSpan(1000 * 60);// 扫描间隔一分钟
			option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
			option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
			mLocationClient.setLocOption(option);
			mLocationClient.start();

		}
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

}
