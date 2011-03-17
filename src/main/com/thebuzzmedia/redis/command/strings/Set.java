package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class Set extends AbstractCommand {
	private static final byte[] COMMAND = "SET".getBytes();

	public Set(CharSequence key, CharSequence value)
			throws IllegalArgumentException {
		if (key == null || key.length() == 0)
			throw new IllegalArgumentException("key cannot be null or empty");
		if (value == null || value.length() == 0)
			throw new IllegalArgumentException("value cannot be null or empty");

		append(COMMAND);
		append(key);
		append(value);
	}
}