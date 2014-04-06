package com.whr.taskmanager.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TaskManagerService extends Service {

	private MyIBinder mIBinder = new MyIBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mIBinder;
	}

	public class MyIBinder extends Binder {
		TaskManagerServiceCallBack mCallBack;

		public void registerCallBack(TaskManagerServiceCallBack callBack) {
			this.mCallBack = callBack;
		}

		public void unRegisterCallBack() {
			this.mCallBack = null;
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
}
