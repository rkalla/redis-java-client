package com.thebuzzmedia.redis.command.ssets;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class ZREVRANGE extends AbstractCommand {
	public static final String NAME = "ZREVRANGE";

	public ZREVRANGE(CharSequence key, int start, int stop, boolean withScores) {
		if (withScores)
			this.command = createBinarySafeRequest(5, NAME, key,
					Integer.toString(start), Integer.toString(stop),
					"WITHSCORES");
		else
			this.command = createBinarySafeRequest(4, NAME, key,
					Integer.toString(start), Integer.toString(stop));
	}
}