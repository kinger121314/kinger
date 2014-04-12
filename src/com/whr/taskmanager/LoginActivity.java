package com.whr.taskmanager;

import java.lang.reflect.Field;
import java.security.MessageDigest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.whr.taskmanager.service.TaskManagerService;
import com.whr.taskmanager.service.TaskManagerService.MyIBinder;
import com.whr.taskmanager.service.TaskManagerServiceCallBack;

public class LoginActivity extends Activity implements OnClickListener {
	MyIBinder mBinder;
	TaskManagerServiceCallBack mCallBack;

	SharedPreferences mSharedPreferences;
	EditText mUserName;
	EditText mPassword;
	CheckBox mRemember;
	Button mLogin;
	Button mRegister;

	boolean mIsCheck;

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			return false;
		}
	});

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_login);
		ActionBar actionBar = this.getActionBar();
		actionBar.setTitle("登录");
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
		// 启动服务
		Intent service = new Intent(this, TaskManagerService.class);
		startService(service);
		bindService(service, mConn, Context.BIND_AUTO_CREATE);

		mCallBack = new TaskManagerServiceCallBack() {
		};
		initView();
		mSharedPreferences = getSharedPreferences("TaskManager", 0);
		// 如果点了保存密码 则，将账号密码显示上去
		mIsCheck = mSharedPreferences.getBoolean("isRememberPassword", false);
		if (mIsCheck) {
			String username = mSharedPreferences.getString("username", "");
			String password = mSharedPreferences.getString("password", "");
			username = encryptmd5(username);
			password = encryptmd5(password);
			mUserName.setText(""+username);
			mPassword.setText(""+password);
			mRemember.setChecked(true);
		}
	}

	public void initView() {
		mUserName = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mRemember = (CheckBox) findViewById(R.id.checkBox_remember);
		mLogin = (Button) findViewById(R.id.login);
		mRegister = (Button) findViewById(R.id.register);
		mLogin.setOnClickListener(this);
		mRegister.setOnClickListener(this);
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
		case R.id.login:
			String userName = mUserName.getText().toString();
			String password = mPassword.getText().toString();
			// 拦截非法的用户名与密码
			if ("".equals(userName) || "".equals(password)) {
				Toast.makeText(LoginActivity.this, "用户名或密码不能为空",
						Toast.LENGTH_SHORT).show();
				break;
			}
			mBinder.login(userName, password);
			break;

		case R.id.register:
			// 去到注册
			Intent intent = new Intent(LoginActivity.this,
					RegisterActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	// MD5加密，32位
	public static String MD5(String str) {
		// 错误检测
		if (str == null || "".equals(str)) {
			return "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	// 可逆的加密算法
	public static String encryptmd5(String str) {
		// 错误检测
		if (str == null || "".equals(str)) {
			return "";
		}
		char[] a = str.toCharArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (char) (a[i] ^ 'l');
		}
		String s = new String(a);
		return s;
	}

}
