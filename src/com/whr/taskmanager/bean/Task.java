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
	 * 目的地坐标X
	 */
	private double mDestX;
	/**
	 * 目的地坐标Y
	 */
	private double mDestY;
	/**
	 * 重复行为
	 */
	private ArrayList<RepeatAction> mReaptAction;
	/**
	 * 重要等级
	 */
	private ImportLevel mImportLevel;
	/**
	 * 任务状态
	 */
	private Status mStatus;
	/**
	 * 提醒方式
	 */
	private MentionAction mMentionAction;

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
	 * @param reaptAction
	 *            重复行为
	 * @param importLevel
	 *            重要等级
	 * @param mentionAction
	 *            提醒方式
	 */
	public Task(long createTime, String title, String content, long expireTime,
			ArrayList<RepeatAction> reaptAction, ImportLevel importLevel,
			MentionAction mentionAction) {
		// 初始化
		this.mCreateTime = createTime;
		this.mTitle = title;
		this.mContent = content;
		this.mExpireTime = expireTime;
		this.mReaptAction = reaptAction;
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
	 * @param reaptAction
	 *            重复行为
	 * @param importLevel
	 *            重要等级
	 * @param mentionAction
	 *            提醒方式
	 */
	public Task(long createTime, String title, String content, long expireTime,
			double destX, double destY, ArrayList<RepeatAction> reaptAction,
			ImportLevel importLevel, MentionAction mentionAction) {
		// 初始化
		this.mCreateTime = createTime;
		this.mTitle = title;
		this.mContent = content;
		this.mExpireTime = expireTime;
		this.mDestX = destX;
		this.mDestY = destY;
		this.mReaptAction = reaptAction;
		this.mImportLevel = importLevel;
		this.mMentionAction = mentionAction;
		// 默认初始化
		this.mModifyTime = this.mCreateTime;
		this.mModifyAction = ModifyAction.CREATE;
		this.mStatus = Status.ACTIVE;
	}
}
