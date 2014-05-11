package com.whr.taskmanager.comparator;

import java.util.Comparator;

import com.whr.taskmanager.bean.Task;

public class LevelSortComparator implements Comparator<Task> {

	@Override
	public int compare(Task lhs, Task rhs) {
		int res = rhs.getImportLevel().compareTo(lhs.getImportLevel());
		if (res == 0) {
			return (int) (rhs.getExpireTime() - lhs.getExpireTime());
		}
		return rhs.getImportLevel().compareTo(lhs.getImportLevel());
	}
}
