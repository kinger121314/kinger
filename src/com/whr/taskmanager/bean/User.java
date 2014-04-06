package com.whr.taskmanager.bean;

public class User {
	/**
	 * 用户名
	 */
	private String mUserName;
	/**
	 * 用户密码
	 */
	private String mPassword;
	/**
	 * 频道号
	 */
	private long mChannelID;
	/**
	 * 用户ID
	 */
	private String mUserID;
	/**
	 * 最后一次更新时间
	 */
	private long mLastRequestTime;

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String userName) {
		this.mUserName = userName;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public long getChannelID() {
		return mChannelID;
	}

	public void setChannelID(long channelID) {
		this.mChannelID = channelID;
	}

	public String getUserID() {
		return mUserID;
	}

	public void setUserID(String userID) {
		this.mUserID = userID;
	}

	public long getLastRequestTime() {
		return mLastRequestTime;
	}

	public void setLastRequestTime(long lastRequestTime) {
		this.mLastRequestTime = lastRequestTime;
	}
}
