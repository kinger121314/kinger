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
	 * 纬度
	 */
	private double mLatitude;
	/**
	 * 经度
	 */
	private double mLongitude;

	/**
	 * 地址
	 */
	private String mAddrStr;

	/**
	 * 最后一次服务端更新时间
	 */
	private long mLastServerTime;
	/**
	 * 最后一次客户端更新时间
	 */
	private long mLastRequestTime = 0L;

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

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	public String getAddrStr() {
		return mAddrStr;
	}

	public void setAddrStr(String addrStr) {
		this.mAddrStr = addrStr;
	}

	public long getLastServerTime() {
		return mLastServerTime;
	}

	public void setLastServerTime(long lastServerTime) {
		this.mLastServerTime = lastServerTime;
	}

}
