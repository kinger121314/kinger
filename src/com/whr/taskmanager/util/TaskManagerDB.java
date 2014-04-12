package com.whr.taskmanager.util;

import java.util.ArrayList;

import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.bean.Task.ImportLevel;
import com.whr.taskmanager.bean.Task.MentionAction;
import com.whr.taskmanager.bean.Task.ModifyAction;
import com.whr.taskmanager.bean.Task.RepeatAction;
import com.whr.taskmanager.bean.Task.Status;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskManagerDB extends SQLiteOpenHelper {

	private static final String TAG = "TaskManager";

	private static final String DATABASE_NAME = "task.db";
	private static final int DATABASE_VERSION = 1;
	private static TaskManagerDB mInstance;
	private static SQLiteDatabase db;

	private TaskManagerDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
		db = this.getWritableDatabase();
	}

	public static synchronized TaskManagerDB getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new TaskManagerDB(context, DATABASE_NAME, null,
					DATABASE_VERSION);
		}
		return mInstance;
	}

	private static final String TaskTable = "task";
	private static final String TaskCreateTime = "createTime";
	private static final String TaskModifyTime = "modifyTime";
	private static final String TaskModifyAction = "modifyAction";
	private static final String TaskTitle = "title";
	private static final String TaskContent = "content";
	private static final String TaskExpireTime = "expireTime";
	private static final String TaskDestX = "destX";
	private static final String TaskDestY = "destY";
	private static final String TaskReaptAction = "reaptAction";
	private static final String TaskImportLevel = "importLevel";
	private static final String TaskStatus = "status";
	private static final String TaskMentionAction = "mentionAction";
	private static final String TaskUserName = "userName";

	private static final String CreateTask = "create table %s0 (%s1 Long,%s2 Long, %s3 text,%s4 text,%s5 text,%s6 Long,%s7 Double,%s8 Double,%s9 text, "
			.replace("%s0", TaskTable).replace("%s1", TaskCreateTime)
			.replace("%s2", TaskModifyTime).replace("%s3", TaskModifyAction)
			.replace("%s4", TaskTitle).replace("%s5", TaskContent)
			.replace("%s6", TaskExpireTime).replace("%s7", TaskDestX)
			.replace("%s8", TaskDestY).replace("%s9", TaskReaptAction)
			+ " %s0 integer,%s1 text,%s2 text,%s3 text)"
					.replace("%s0", TaskImportLevel).replace("%s1", TaskStatus)
					.replace("%s2", TaskMentionAction)
					.replace("$s3", TaskUserName);

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CreateTask);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public synchronized ArrayList<Task> getAllTasks(String userName) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		db.beginTransaction(); // 开始事务
		try {
			String[] selectionArgs = new String[] { userName };
			Cursor res = db.query(TaskTable, null,
					"%s0 = ?".replace("%s0", TaskUserName), selectionArgs,
					null, null, null);
			while (res.moveToNext()) {
				long createTime = res.getLong(res
						.getColumnIndex(TaskCreateTime));
				long modifyTime = res.getLong(res
						.getColumnIndex(TaskModifyTime));
				ModifyAction modifyAction = ModifyAction.valueOf(res
						.getString(res.getColumnIndex(TaskModifyAction)));
				String title = res.getString(res.getColumnIndex(TaskTitle));
				String content = res.getString(res.getColumnIndex(TaskContent));
				long expireTime = res.getLong(res
						.getColumnIndex(TaskExpireTime));
				double destX = res.getDouble(res.getColumnIndex(TaskDestX));
				double destY = res.getDouble(res.getColumnIndex(TaskDestY));

				ArrayList<RepeatAction> reaptActions = new ArrayList<Task.RepeatAction>();
				String repeatActionString = res.getString(res
						.getColumnIndex(TaskReaptAction));
				String[] repeatActionArrs = repeatActionString.split(",");
				for (String tmp : repeatActionArrs) {
					reaptActions.add(RepeatAction.valueOf(tmp));
				}
				ImportLevel importLevel = ImportLevel.valueOf(res.getString(res
						.getColumnIndex(TaskImportLevel)));
				Status status = Status.valueOf(res.getString(res
						.getColumnIndex(TaskStatus)));
				MentionAction mentionAction = MentionAction.valueOf(res
						.getString(res.getColumnIndex(TaskMentionAction)));
				Task task = new Task(createTime, modifyTime, modifyAction,
						title, content, expireTime, destX, destY, reaptActions,
						importLevel, status, mentionAction);
				tasks.add(task);
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch (Exception e) {
			Log.d(TAG, e.toString());
			return null;
		} finally {
			db.endTransaction(); // 结束事务
		}
		return tasks;
	}
}
