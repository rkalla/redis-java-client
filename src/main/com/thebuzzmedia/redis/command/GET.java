package com.thebuzzmedia.redis.command;

import com.thebuzzmedia.redis.Constants;

public class GET implements ICommand {
	public static final String NAME = "GET";

	private char[] fullCommand;

	public GET(String key) throws IllegalArgumentException {
		if (key == null || key.length() == 0)
			throw new IllegalArgumentException("key cannot be null or empty");

		fullCommand = buildFullCommand(key);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public char[] getFullCommand() {
		return fullCommand;
	}

	protected char[] buildFullCommand(String key) {
		StringBuilder builder = new StringBuilder(NAME.length() + key.length() + 3);
		char[] result = new char[builder.capacity()];

		builder.append(NAME).append(' ').append(key)
				.append(Constants.CRLF_CHARS);
		builder.getChars(0, builder.length(), result, 0);

		return result;
	}
}