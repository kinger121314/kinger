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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.whr.taskmanager.service.TaskManagerService;
import com.whr.taskmanager.service.TaskManagerService.MyIBinder;
import com.whr.taskmanager.service.TaskManagerServiceCallBack;

public class RegisterActivity extends Activity implements OnClickListener {

	private static final int ERROR_MSG = 0x999;
	private static final int LOGIN_SUCCESS = 0x001;
	private static final int CANCEL_DIALOG = 0x002;

	MyIBinder mBinder;
	TaskManagerServiceCallBack mCallBack;

	EditText mUserName;
	EditText mPassword;
	EditText mRePassword;
	Button mRegister;
	Button mCancel;

	private Dialog mLoadDialog;
	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case ERROR_MSG:
				Toast.makeText(RegisterActivity.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				mHandler.sendEmptyMessage(CANCEL_DIALOG);
				break;

			case LOGIN_SUCCESS:
				mHandler.sendEmptyMessage(CANCEL_DIALOG);
				Intent intent = new Intent(RegisterActivity.this,
						MainActivity.class);
				startActivity(intent);
				RegisterActivity.this.finish();

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
		setContentView(R.layout.layout_register);
		ActionBar actionBar = this.getActionBar();
		actionBar.setTitle("注册");
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
			}

			@Override
			public void registerSuccess() {
				// TODO Auto-generated method stub

			}
		};
		initView();
	}

	public void initView() {
		mUserName = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mRePassword = (EditText) findViewById(R.id.re_password);
		mRegister = (Button) findViewById(R.id.register);
		mCancel = (Button) findViewById(R.id.cancel);
		mRegister.setOnClickListener(this);
		mCancel.setOnClickListener(this);
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
		case R.id.register:
			String userName = mUserName.getText().toString();
			String password = mPassword.getText().toString();
			String rePassword = mRePassword.getText().toString();

			// 拦截非法的用户名与密码
			if ("".equals(userName) || "".equals(password)
					|| "".equals(rePassword)) {
				Toast.makeText(RegisterActivity.this, "用户名或密码不能为空",
						Toast.LENGTH_SHORT).show();
				break;
			}
			if (!password.equals(rePassword)) {
				Toast.makeText(RegisterActivity.this, "repeat password error!",
						Toast.LENGTH_SHORT).show();
				break;
			}
			// 启动循环圈等待
			mLoadDialog = new AlertDialog.Builder(RegisterActivity.this)
					.create();
			mLoadDialog.show();
			// 注意此处要放在show之后 否则会报异常
			mLoadDialog.setContentView(R.layout.loading_process_dialog_anim);
			mBinder.register(userName, password);

			break;

		case R.id.cancel:
			this.finish();
			break;

		default:
			break;
		}
	}

}
