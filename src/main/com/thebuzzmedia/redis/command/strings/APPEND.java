package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class APPEND extends AbstractCommand {
	public static final String NAME = "GET";

	public APPEND(CharSequence key, CharSequence value)
			throws IllegalArgumentException {
		this.command = createBinarySafeRequest(3, NAME, key, value);
	}
}