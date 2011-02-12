package com.thebuzzmedia.redis.command;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.util.ArrayUtils;
import com.thebuzzmedia.redis.util.StrictDynamicByteArray;

public abstract class AbstractCommand implements ICommand {
	protected byte[] command;

	public AbstractCommand(CharSequence... arguments)
			throws IllegalArgumentException {
		if (arguments == null || arguments.length == 0)
			throw new IllegalArgumentException(
					"arguments cannot be null or empty. arguments must represent a valid Redis command with any additional command arguments.");

		StringBuilder builder = new StringBuilder();

		// Add the argument count header
		builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
				.append(arguments.length).append(Constants.CRLF_CHARS);

		// Add each of the arguments
		for (CharSequence seq : arguments) {
			// Add the length of argument header
			builder.append((char) Constants.REPLY_TYPE_BULK)
					.append(seq.length()).append(Constants.CRLF_CHARS);

			// Add the argument body
			builder.append(seq).append(Constants.CRLF_CHARS);
		}

		// Encode command
		command = ArrayUtils.encode(builder);
	}

	public AbstractCommand(char[]... arguments) throws IllegalArgumentException {
		if (arguments == null || arguments.length == 0)
			throw new IllegalArgumentException(
					"arguments cannot be null or empty. arguments must represent a valid Redis command with any additional command arguments.");

		StringBuilder builder = new StringBuilder();

		// Add the argument count header
		builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
				.append(arguments.length).append(Constants.CRLF_CHARS);

		// Add each of the arguments
		for (char[] chars : arguments) {
			// Add the length of argument header
			builder.append((char) Constants.REPLY_TYPE_BULK)
					.append(chars.length).append(Constants.CRLF_CHARS);

			// Add the argument body
			builder.append(chars).append(Constants.CRLF_CHARS);
		}

		// Encode command
		command = ArrayUtils.encode(builder);
	}

	public AbstractCommand(byte[]... arguments) throws IllegalArgumentException {
		if (arguments == null || arguments.length == 0)
			throw new IllegalArgumentException(
					"arguments cannot be null or empty. arguments must represent a valid Redis command with any additional command arguments.");

		StringBuilder builder = new StringBuilder();
		StrictDynamicByteArray result = new StrictDynamicByteArray();

		// Add the argument count header
		builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
				.append(arguments.length).append(Constants.CRLF_CHARS);
		result.append(ArrayUtils.encode(builder));

		// Add each of the arguments
		for (byte[] bytes : arguments) {
			builder.setLength(0);

			// Add the length of argument header
			builder.append((char) Constants.REPLY_TYPE_BULK)
					.append(bytes.length).append(Constants.CRLF_CHARS);
			result.append(ArrayUtils.encode(builder));

			// Add the argument body
			result.append(bytes);
			result.append(Constants.CRLF_BYTES);
		}

		this.command = result.getArray();
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[command=" + new String(command)
				+ "]";
	}

	@Override
	public byte[] getCommand() {
		return command;
	}
	
	// TODO: Create protected methods that do all the logic in the constructors
	// so overriding classes can use them when the default constructors aren't
	// good enough.
}