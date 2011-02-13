package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class GET extends AbstractCommand {
	public static final String NAME = "GET";

	public GET(CharSequence key) throws IllegalArgumentException {
		this.command = createBinarySafeRequest(2, NAME, key);
	}
}