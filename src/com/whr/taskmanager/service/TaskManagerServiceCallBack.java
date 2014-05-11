package com.whr.taskmanager.service;

import java.util.ArrayList;

import com.whr.taskmanager.bean.Task;

public interface TaskManagerServiceCallBack {
	public void errorHasHappen(String errorMsg);

	public void loginSuccess();

	public void registerSuccess();

	public void initTasksData(ArrayList<Task> tasks);
}
