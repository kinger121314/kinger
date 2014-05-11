package com.whr.taskmanager.comparator;

import java.util.Comparator;

import com.whr.taskmanager.bean.Task;

public class TimeSortComparator implements Comparator<Task> {

	@Override
	public int compare(Task lhs, Task rhs) {
		int res = (int) (rhs.getExpireTime() - lhs.getExpireTime());
		if (res == 0) {
			return rhs.getImportLevel().compareTo(lhs.getImportLevel());
		}
		return (int) (rhs.getExpireTime() - lhs.getExpireTime());
	}
}
