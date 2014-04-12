package com.whr.taskmanager.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.whr.taskmanager.service.TaskManagerService;

public class TaskManagerBroadCastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent newIntent = new Intent(context, TaskManagerService.class);
			context.startService(newIntent);
		}
	}

}
