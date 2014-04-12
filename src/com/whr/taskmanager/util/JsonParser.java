package com.whr.taskmanager.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.whr.taskmanager.bean.Task;

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
}
