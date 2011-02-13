package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class SET extends AbstractCommand {
	public static final String NAME = "SET";

	public SET(CharSequence key, CharSequence value)
			throws IllegalArgumentException {
		this.command = createBinarySafeRequest(3, NAME, key, value);
	}
}