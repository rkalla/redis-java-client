package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class Get extends AbstractCommand {
	private static final byte[] COMMAND = "GET".getBytes();

	public Get(CharSequence key) throws IllegalArgumentException {
		if (key == null || key.length() == 0)
			throw new IllegalArgumentException("key cannot be null or empty");

		append(COMMAND);
		append(key);
	}
}