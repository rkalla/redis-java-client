package com.thebuzzmedia.redis.command;

import com.thebuzzmedia.redis.Constants;

public class SET implements ICommand {
	public static final String NAME = "SET";

	private char[] fullCommand;

	public SET(String key, String value) throws IllegalArgumentException {
		if (key == null || key.length() == 0)
			throw new IllegalArgumentException("key cannot be null or empty");

		fullCommand = buildFullCommand(key, value);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public char[] getFullCommand() {
		return fullCommand;
	}

	protected char[] buildFullCommand(String key, String value) {
		StringBuilder builder = new StringBuilder(NAME.length() + key.length()
				+ (value == null ? 0 : value.length()) + 6);
		char[] result = new char[builder.capacity()];

		builder.append(NAME).append(' ').append(key).append(" \"")
				.append((value == null ? "" : value)).append("\"")
				.append(Constants.CRLF_CHARS);
		builder.getChars(0, builder.length(), result, 0);

		return result;
	}
}