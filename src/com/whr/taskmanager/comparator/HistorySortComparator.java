package com.whr.taskmanager.comparator;

import java.util.Comparator;

import com.whr.taskmanager.bean.Task;

public class HistorySortComparator implements Comparator<Task> {

	@Override
	public int compare(Task lhs, Task rhs) {
		return (int) (rhs.getModifyTime()-lhs.getModifyTime());
	}

}
