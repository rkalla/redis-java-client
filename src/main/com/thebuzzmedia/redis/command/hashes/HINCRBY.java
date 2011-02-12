package com.thebuzzmedia.redis.command.hashes;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class HINCRBY extends AbstractCommand {
	public static final String NAME = "HINCRBY";

	public HINCRBY(String key, String field, int increment) {
		super(NAME, key, field, Integer.toString(increment));
	}
}