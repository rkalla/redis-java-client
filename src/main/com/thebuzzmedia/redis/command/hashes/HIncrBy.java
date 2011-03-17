package com.thebuzzmedia.redis.command.hashes;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class HIncrBy extends AbstractCommand {
	private static final byte[] COMMAND = "HINCRBY".getBytes();

	public HIncrBy(CharSequence key, CharSequence field, int increment)
			throws IllegalArgumentException {
		if (key == null || key.length() == 0)
			throw new IllegalArgumentException("key cannot be null or empty");
		if (field == null || field.length() == 0)
			throw new IllegalArgumentException("field cannot be null or empty");

		append(COMMAND);
		append(key);
		append(field);
		append(Integer.toString(increment));
	}
}