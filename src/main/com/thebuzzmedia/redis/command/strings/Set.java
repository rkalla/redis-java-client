package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class Set extends AbstractCommand {
	private static final byte[] COMMAND = "SET".getBytes();

	public Set(CharSequence key, CharSequence value)
			throws IllegalArgumentException {
		append(COMMAND);
		append(key);
		append(value);
	}
}