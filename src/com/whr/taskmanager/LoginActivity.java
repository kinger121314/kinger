package com.whr.taskmanager;

import java.lang.reflect.Field;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.whr.taskmanager.util.AESEncryptor;

public class LoginActivity extends Activity implements OnClickListener {

	private static final int ERROR_MSG = 0x999;
	private static final int LOGIN_SUCCESS = 0x001;
	private static final int CANCEL_DIALOG = 0x002;

	MyIBinder mBinder;
	TaskManagerServiceCallBack mCallBack;
	SharedPreferences mSharedPreferences;
	EditText mUserName;
	EditText mPassword;
	CheckBox mRemember;
	Button mLogin;
	Button mRegister;

	boolean mIsCheck;

	private Dialog mLoadDialog;

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case ERROR_MSG:
				Toast.makeText(LoginActivity.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				mHandler.sendEmptyMessage(CANCEL_DIALOG);
				break;

			case LOGIN_SUCCESS:
				mHandler.sendEmptyMessage(CANCEL_DIALOG);
				// 保存账户密码
				if (mRemember.isChecked()) {
					String userName = mUserName.getText().toString();
					String password = mPassword.getText().toString();
					try {
						userName = AESEncryptor.encrypt("mUserName", userName);
						password = AESEncryptor.encrypt("mPassword", userName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Editor editor = mSharedPreferences.edit();
					editor.putString("username", userName);
					editor.putString("password", password);
					editor.putBoolean("isRememberPassword", true);
					editor.commit();
				}else{
					Editor editor = mSharedPreferences.edit();
					editor.putBoolean("isRememberPassword", false);
					editor.commit();
				}
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				LoginActivity.this.finish();

				break;

			case CANCEL_DIALOG:
				if (mLoadDialog != null) {
					mLoadDialog.dismiss();
				}
				break;

			default:
				break;
			}
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

			@Override
			public void errorHasHappen(String errorMsg) {
				mHandler.obtainMessage(ERROR_MSG, errorMsg).sendToTarget();
			}

			@Override
			public void loginSuccess() {
				mHandler.sendEmptyMessage(LOGIN_SUCCESS);
			}

			@Override
			public void registerSuccess() {
			}
		};
		initView();
		mSharedPreferences = getSharedPreferences("TaskManager", 0);
		// 如果点了保存密码 则，将账号密码显示上去
		mIsCheck = mSharedPreferences.getBoolean("isRememberPassword", false);
		if (mIsCheck) {
			String username = mSharedPreferences.getString("username", "");
			String password = mSharedPreferences.getString("password", "");
			try {
				username = AESEncryptor.decrypt("mUserName", username);
				password = AESEncryptor.decrypt("mPassword", password);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mUserName.setText("" + username);
			mPassword.setText("" + password);
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
			// 启动循环圈等待
			mLoadDialog = new AlertDialog.Builder(LoginActivity.this).create();
			mLoadDialog.show();
			// 注意此处要放在show之后 否则会报异常
			mLoadDialog.setContentView(R.layout.loading_process_dialog_anim);
			// 通知登陆
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
}
