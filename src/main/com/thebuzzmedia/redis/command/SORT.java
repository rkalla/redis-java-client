package com.thebuzzmedia.redis.command;

public class SORT {
	public static enum SortDirection {
		ASC, DESC;
	}

	public SORT(String key, String byPattern, int limitOffset, int limitCount,
			String[] getPatterns, SortDirection direction,
			boolean sortAsStrings, String destination) {

	}
}