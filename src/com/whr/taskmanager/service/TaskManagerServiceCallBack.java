package com.whr.taskmanager.service;

public interface TaskManagerServiceCallBack {
	public void errorHasHappen(String errorMsg);
	
	public void loginSuccess();
	
	public void registerSuccess();
}
