package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class Append extends AbstractCommand {
	private static final byte[] COMMAND = "APPEND".getBytes();

	public Append(CharSequence key, CharSequence value)
			throws IllegalArgumentException {
		append(COMMAND);
		append(key);
		append(value);
	}
}