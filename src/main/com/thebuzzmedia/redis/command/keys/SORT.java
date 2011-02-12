package com.thebuzzmedia.redis.command.keys;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class SORT extends AbstractCommand {
	public static final String NAME = "SORT";
	
	public static enum SortDirection {
		ASC, DESC;
	}

	public SORT(String key, String byPattern, int limitOffset, int limitCount,
			String[] getPatterns, SortDirection direction,
			boolean sortAsStrings, String destination) {
		
	}
}