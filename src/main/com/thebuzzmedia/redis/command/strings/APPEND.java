package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class APPEND extends AbstractCommand {
	public static final String NAME = "GET";

	public APPEND(String key, String value) {
		super(NAME, key, value);
	}
}