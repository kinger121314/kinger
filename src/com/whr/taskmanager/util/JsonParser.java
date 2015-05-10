package com.whr.taskmanager.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.whr.taskmanager.bean.Task;
import com.whr.taskmanager.bean.User;
import com.whr.taskmanager.bean.Task.ImportLevel;
import com.whr.taskmanager.bean.Task.MentionAction;
import com.whr.taskmanager.bean.Task.ModifyAction;
import com.whr.taskmanager.bean.Task.RepeatAction;
import com.whr.taskmanager.bean.Task.Status;

public class JsonParser {
	/*
	 * // { // "Tasks": // [ // { // "CreateTime":1393069214158, //
	 * "ModifyTime":1393069214158, // "ModifyAction":"Create|Update|Delete", //
	 * "Title":"吃饭", // "Content":"21点吃饭", // "ExpireTime":1393069214158, //
	 * "Longitude ":42.03249652949337, // "Latitude ":113.3129895882556, //
	 * "Repeat":"Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday", //
	 * "ImportLevel":1|2|3|4|5, // "Status":"Active|NonActive|Outdate", //
	 * "MentionAction":"Voice|Shake" // } // ] // }
	 */
	public static String getJsonStringFromTasks(ArrayList<Task> arrs) {
		// 如果为空 则返回null
		if (arrs.size() == 0) {
			return null;
		}
		try {
			JSONObject taskJson = new JSONObject();
			JSONArray tasksArr = new JSONArray();
			for (int i = 0; i < arrs.size(); i++) {
				Task task = arrs.get(i);
				JSONObject tmp = new JSONObject();
				tmp.put("CreateTime", task.getCreateTime());
				tmp.put("ModifyTime", task.getModifyTime());
				tmp.put("ModifyAction", task.getModifyAction().toString());
				tmp.put("Title", task.getTitle());
				tmp.put("Content", task.getContent());
				tmp.put("ExpireTime", task.getExpireTime());
				tmp.put("Longitude", task.getLongitude());
				tmp.put("Latitude", task.getLatitude());
				tmp.put("Repeat", task.getReaptAction());
				tmp.put("ImportLevel", task.getImportLevel().toString());
				tmp.put("Status", task.getStatus().toString());
				tmp.put("MentionAction", task.getMentionAction().toString());
				tasksArr.put(tmp);
			}
			taskJson.put("Tasks", tasksArr);
			return taskJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getJsonStringFromTasks(Task tmpTask) {
		// 如果为空 则返回null
		if (tmpTask==null){
			return null;
		}
		try {
			JSONObject taskJson = new JSONObject();
			JSONArray tasksArr = new JSONArray();
			Task task = tmpTask;
			JSONObject tmp = new JSONObject();
			tmp.put("CreateTime", task.getCreateTime());
			tmp.put("ModifyTime", task.getModifyTime());
			tmp.put("ModifyAction", task.getModifyAction().toString());
			tmp.put("Title", task.getTitle());
			tmp.put("Content", task.getContent());
			tmp.put("ExpireTime", task.getExpireTime());
			tmp.put("Longitude", task.getLongitude());
			tmp.put("Latitude", task.getLatitude());
			tmp.put("Repeat", task.getReaptAction());
			tmp.put("ImportLevel", task.getImportLevel().toString());
			tmp.put("Status", task.getStatus().toString());
			tmp.put("MentionAction", task.getMentionAction().toString());
			tasksArr.put(tmp);
			taskJson.put("Tasks", tasksArr);
			return taskJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<Task> getTasksFromJsonString(String jsonString,
			User user) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		try {
			JSONObject json = new JSONObject(jsonString);
			String userName = json.getString("UserName");
			if (user.getUserName().equals(userName)) {
				JSONArray jsonArr = json.getJSONArray("Tasks");
				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject tmp = jsonArr.getJSONObject(i);
					long createTime = tmp.getLong("CreateTime");
					long modifyTime = tmp.getLong("ModifyTime");
					ModifyAction modifyAction = ModifyAction.valueOf(tmp
							.getString("ModifyAction"));
					String title = tmp.getString("Title");
					String content = tmp.getString("Content");
					long expireTime = tmp.getLong("ExpireTime");
					double destX = -1.0;
					double destY = -1.0;
					try {
						destX = tmp.getDouble("Longitude");
						destY = tmp.getDouble("Latitude ");
					} catch (Exception e) {
						e.printStackTrace();
					}
					ArrayList<RepeatAction> reaptActions = new ArrayList<Task.RepeatAction>();
					String repeatActionString = tmp.getString("Repeat");
					try {
						if (!"".equals(repeatActionString)) {
							String[] repeatActionArrs = repeatActionString
									.split("\\|");
							for (String tmpAction : repeatActionArrs) {
								reaptActions.add(RepeatAction
										.valueOf(tmpAction));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					ImportLevel importLevel = ImportLevel
							.valueOf(tmp.getString("ImportLevel"));
					Status status = Status.valueOf(tmp.getString("Status"));

					ArrayList<MentionAction> mentionActions = new ArrayList<MentionAction>();
					String mentionActionString = tmp.getString("MentionAction");
					try {
						if (!"".equals(mentionActionString)) {
							String[] mentionActionArrs = mentionActionString
									.split("\\|");
							for (String tmpAction : mentionActionArrs) {
								mentionActions.add(MentionAction
										.valueOf(tmpAction));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					Task task = new Task(createTime, modifyTime, modifyAction,
							title, content, expireTime, destX, destY,
							reaptActions, importLevel, status, mentionActions);
					tasks.add(task);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tasks;
	}
}
