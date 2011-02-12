package com.thebuzzmedia.redis.command.strings;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class SET extends AbstractCommand {
	public static final String NAME = "SET";

	public SET(CharSequence key, CharSequence value) throws IllegalArgumentException {
		super(NAME, key, value);
		
		if(value == null || value.length() == 0)
			throw new IllegalArgumentException("value cannot be null or empty");
	}
}