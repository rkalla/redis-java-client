package com.thebuzzmedia.redis.command.hashes;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class HINCRBY extends AbstractCommand {
	public static final String NAME = "HINCRBY";

	public HINCRBY(CharSequence key, CharSequence field, int increment)
			throws IllegalArgumentException {
		this.command = createBinarySafeRequest(4, NAME, key, field,
				Integer.toString(increment));
	}
}