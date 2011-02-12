package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class GET extends AbstractCommand {
	public static final String NAME = "GET";

	public GET(String key) throws IllegalArgumentException {
		super(NAME, key);
	}
}