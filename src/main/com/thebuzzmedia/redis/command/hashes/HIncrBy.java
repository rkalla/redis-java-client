package com.thebuzzmedia.redis.command.hashes;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class HIncrBy extends AbstractCommand {
	private static final byte[] COMMAND = "HINCRBY".getBytes();

	public HIncrBy(CharSequence key, CharSequence field, int increment)
			throws IllegalArgumentException {
		append(COMMAND);
		append(key);
		append(field);
		append(Integer.toString(increment));
	}
}