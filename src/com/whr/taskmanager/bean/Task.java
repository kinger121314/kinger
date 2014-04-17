package com.whr.taskmanager.bean;

import java.util.ArrayList;

public class Task {

	/**
	 * 修改行为
	 * 
	 * @author kinger
	 * 
	 */
	public enum ModifyAction {
		/**
		 * 创建
		 */
		CREATE,
		/**
		 * 更新
		 */
		UPDATE,
		/**
		 * 删除
		 */
		DELETE,
		/**
		 * 非法
		 */
		INVAILD,
	}

	/**
	 * 重复行为
	 * 
	 * @author kinger
	 * 
	 */
	public enum RepeatAction {
		/**
		 * 一次
		 */
		ONCE,
		/**
		 * 每天
		 */
		EVERYDAY,
		/**
		 * 星期一
		 */
		MONDAY,
		/**
		 * 星期二
		 */
		TUESDAY,
		/**
		 * 星期三
		 */
		WEDNESDAY,
		/**
		 * 星期四
		 */
		THURSDAY,
		/**
		 * 星期五
		 */
		FRIDAY,
		/**
		 * 星期六
		 */
		SATURDAY,
		/**
		 * 星期天
		 */
		SUNDAY,
		/**
		 * 非法
		 */
		INVAILD,
	}

	/**
	 * 重要等级
	 * 
	 * @author kinger
	 * 
	 */
	public enum ImportLevel {
		/**
		 * 最低
		 */
		LOWEST,
		/**
		 * 比较低
		 */
		LOWER,
		/**
		 * 普通
		 */
		COMMON,
		/**
		 * 比较高
		 */
		HIGHER,
		/**
		 * 最高
		 */
		HIGHEST,
		/**
		 * 非法
		 */
		INVAILD,
	}

	/**
	 * 任务状态
	 * 
	 * @author kinger
	 * 
	 */
	public enum Status {
		/**
		 * 激活
		 */
		ACTIVE,
		/**
		 * 不激活
		 */
		NOTACTIVE,
		/**
		 * 过期
		 */
		OUTDATE,
	}

	/**
	 * 提醒方式
	 * 
	 * @author kinger
	 * 
	 */
	public enum MentionAction {
		/**
		 * 声音
		 */
		VOICE,
		/**
		 * 震动
		 */
		SHAKE,
	}

	/**
	 * 创建时间
	 */
	private long mCreateTime;
	/**
	 * 最后一次修改时间
	 */
	private long mModifyTime;
	/**
	 * 最后一次修改行为
	 */
	private ModifyAction mModifyAction;
	/**
	 * 任务标题
	 */
	private String mTitle;
	/**
	 * 任务内容
	 */
	private String mContent;
	/**
	 * 到期时间
	 */
	private long mExpireTime;
	/**
	 * 纬度
	 */
	private double mLatitude = -1.0;
	/**
	 * 经度
	 */
	private double mLongitude = -1.0;

	/**
	 * 重复行为
	 */
	private ArrayList<RepeatAction> mReaptAction;
	/**
	 * 重要等级
	 */
	private ImportLevel mImportLevel = ImportLevel.LOWEST;
	/**
	 * 任务状态
	 */
	private Status mStatus;
	/**
	 * 提醒方式
	 */
	private ArrayList<MentionAction> mMentionAction;

	/**
	 * 默认构造函数
	 */
	public Task() {
	}

	/**
	 * 创建任务的构造函数
	 * 
	 * @param createTime
	 *            创建时间
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param expireTime
	 *            到期时间
	 * @param reaptActions
	 *            重复行为
	 * @param importLevel
	 *            重要等级
	 * @param mentionAction
	 *            提醒方式
	 */
	public Task(long createTime, String title, String content, long expireTime,
			ArrayList<RepeatAction> reaptActions, ImportLevel importLevel,
			ArrayList<MentionAction> mentionAction) {
		// 初始化
		this.mCreateTime = createTime;
		this.mTitle = title;
		this.mContent = content;
		this.mExpireTime = expireTime;
		this.mReaptAction = reaptActions;
		this.mImportLevel = importLevel;
		this.mMentionAction = mentionAction;
		// 默认初始化
		this.mModifyTime = this.mCreateTime;
		this.mModifyAction = ModifyAction.CREATE;
		this.mStatus = Status.ACTIVE;
	}

	/**
	 * 带目的地坐标的任务构造函数
	 * 
	 * @param createTime
	 *            创建时间
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param expireTime
	 *            到期时间
	 * @param destX
	 *            目的地X
	 * @param destY
	 *            目的地Y
	 * @param reaptActions
	 *            重复行为
	 * @param importLevel
	 *            重要等级
	 * @param mentionAction
	 *            提醒方式
	 */
	public Task(long createTime, String title, String content, long expireTime,
			double destX, double destY, ArrayList<RepeatAction> reaptActions,
			ImportLevel importLevel, ArrayList<MentionAction> mentionAction) {
		// 初始化
		this.mCreateTime = createTime;
		this.mTitle = title;
		this.mContent = content;
		this.mExpireTime = expireTime;
		this.mLatitude = destX;
		this.mLongitude = destY;
		this.mReaptAction = reaptActions;
		this.mImportLevel = importLevel;
		this.mMentionAction = mentionAction;
		// 默认初始化
		this.mModifyTime = this.mCreateTime;
		this.mModifyAction = ModifyAction.CREATE;
		this.mStatus = Status.ACTIVE;
	}

	public Task(long createTime, long modifyTime, ModifyAction modifyAction,
			String title, String content, long expireTime, double destX,
			double destY, ArrayList<RepeatAction> reaptActions,
			ImportLevel importLevel, Status status,
			ArrayList<MentionAction> mentionAction) {
		super();
		this.mCreateTime = createTime;
		this.mModifyTime = modifyTime;
		this.mModifyAction = modifyAction;
		this.mTitle = title;
		this.mContent = content;
		this.mExpireTime = expireTime;
		this.mLatitude = destX;
		this.mLongitude = destY;
		this.mReaptAction = reaptActions;
		this.mImportLevel = importLevel;
		this.mStatus = status;
		this.mMentionAction = mentionAction;
	}

	public long getCreateTime() {
		return mCreateTime;
	}

	public void setCreateTime(long createTime) {
		this.mCreateTime = createTime;
	}

	public long getModifyTime() {
		return mModifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.mModifyTime = modifyTime;
	}

	public ModifyAction getModifyAction() {
		return mModifyAction;
	}

	public void setModifyAction(ModifyAction modifyAction) {
		this.mModifyAction = modifyAction;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		this.mContent = content;
	}

	public long getExpireTime() {
		return mExpireTime;
	}

	public void setExpireTime(long expireTime) {
		this.mExpireTime = expireTime;
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

	public ArrayList<RepeatAction> getReaptAction() {
		return mReaptAction;
	}

	public void setReaptAction(ArrayList<RepeatAction> reaptAction) {
		this.mReaptAction = reaptAction;
	}

	public ImportLevel getImportLevel() {
		return mImportLevel;
	}

	public void setImportLevel(ImportLevel importLevel) {
		this.mImportLevel = importLevel;
	}

	public Status getStatus() {
		return mStatus;
	}

	public void setStatus(Status status) {
		this.mStatus = status;
	}

	public ArrayList<MentionAction> getMentionAction() {
		return mMentionAction;
	}

	public void setMentionAction(ArrayList<MentionAction> mentionAction) {
		this.mMentionAction = mentionAction;
	}

}
