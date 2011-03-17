package com.thebuzzmedia.redis.command.ssets;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class ZRevRange extends AbstractCommand {
	private static final byte[] COMMAND = "ZREVRANGE".getBytes();
	private static final byte[] WITH_SCORES = "WITHSCORES".getBytes();

	public ZRevRange(CharSequence key, int startIndex, int stopIndex,
			boolean withScores) throws IllegalArgumentException {
		if (key == null || key.length() == 0)
			throw new IllegalArgumentException("key cannot be null or empty");
		
		append(COMMAND);
		append(key);
		append(Integer.toString(startIndex));
		append(Integer.toString(stopIndex));

		if (withScores)
			append(WITH_SCORES);
	}
}