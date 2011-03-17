package com.thebuzzmedia.redis.command.sets;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class SUnionStore extends AbstractCommand {
	private static final byte[] COMMAND = "SUNIONSTORE".getBytes();

	public SUnionStore(CharSequence destination, CharSequence... keys)
			throws IllegalArgumentException {
		if (destination == null || destination.length() == 0)
			throw new IllegalArgumentException(
					"destination cannot be null or empty");
		if (keys == null || keys.length == 0)
			throw new IllegalArgumentException(
					"keys must contain at least one set key");

		append(COMMAND);
		append(destination);

		for (int i = 0, length = keys.length; i < length; i++)
			append(keys[i]);
	}
}