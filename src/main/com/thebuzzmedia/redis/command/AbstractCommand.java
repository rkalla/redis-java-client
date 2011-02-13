package com.thebuzzmedia.redis.command;

import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.util.ArrayUtils;
import com.thebuzzmedia.redis.util.StrictDynamicByteArray;

public abstract class AbstractCommand implements ICommand {
	protected byte[] command;

	// public AbstractCommand() {
	// // no-op, subclass will do all the work
	// }
	//
	// public AbstractCommand(CharSequence... arguments)
	// throws IllegalArgumentException {
	// if (arguments == null || arguments.length == 0)
	// throw new IllegalArgumentException(
	// "arguments cannot be null or empty. arguments must represent a valid Redis command with any additional command arguments.");
	//
	// this.command = createBinarySafeRequest(arguments);
	// }
	//
	// public AbstractCommand(char[]... arguments) throws
	// IllegalArgumentException {
	// if (arguments == null || arguments.length == 0)
	// throw new IllegalArgumentException(
	// "arguments cannot be null or empty. arguments must represent a valid Redis command with any additional command arguments.");
	//
	// this.command = createBinarySafeRequest(arguments);
	// }
	//
	// public AbstractCommand(byte[]... arguments) throws
	// IllegalArgumentException {
	// if (arguments == null || arguments.length == 0)
	// throw new IllegalArgumentException(
	// "arguments cannot be null or empty. arguments must represent a valid Redis command with any additional command arguments.");
	//
	// this.command = createBinarySafeRequest(arguments);
	// }

	@Override
	public String toString() {
		return this.getClass().getName() + "[command=" + new String(command)
				+ "]";
	}

	@Override
	public byte[] getCommand() {
		return command;
	}

	protected void checkArguments(int requiredArgumentCount,
			byte[]... arguments) throws IllegalArgumentException {
		if (requiredArgumentCount > 0 && arguments == null
				|| arguments.length == 0)
			throw new IllegalArgumentException("Command requires at least "
					+ requiredArgumentCount
					+ " arguments, but none were specified.");

		for (int i = 0; i < requiredArgumentCount; i++) {
			if (arguments[i] == null || arguments[i].length == 0)
				throw new IllegalArgumentException("Command requires at least "
						+ requiredArgumentCount + " arguments, but argument #"
						+ (i + 1) + " was empty or null.");
		}
	}

	protected void checkArguments(int requiredArgumentCount,
			char[]... arguments) throws IllegalArgumentException {
		if (requiredArgumentCount > 0 && arguments == null
				|| arguments.length == 0)
			throw new IllegalArgumentException("Command requires at least "
					+ requiredArgumentCount
					+ " arguments, but none were specified.");

		for (int i = 0; i < requiredArgumentCount; i++) {
			if (arguments[i] == null || arguments[i].length == 0)
				throw new IllegalArgumentException("Command requires at least "
						+ requiredArgumentCount + " arguments, but argument #"
						+ (i + 1) + " was empty or null.");
		}
	}

	protected void checkArguments(int requiredArgumentCount,
			CharSequence... arguments) throws IllegalArgumentException {
		if (requiredArgumentCount > 0 && arguments == null
				|| arguments.length == 0)
			throw new IllegalArgumentException("Command requires at least "
					+ requiredArgumentCount
					+ " arguments, but none were specified.");

		for (int i = 0; i < requiredArgumentCount; i++) {
			if (arguments[i] == null || arguments[i].length() == 0)
				throw new IllegalArgumentException("Command requires at least "
						+ requiredArgumentCount + " arguments, but argument #"
						+ (i + 1) + " was empty or null.");
		}
	}

	protected void checkArguments(int requiredArgumentCount,
			List<CharSequence> arguments) throws IllegalArgumentException {
		if (requiredArgumentCount > 0 && arguments == null
				|| arguments.size() == 0)
			throw new IllegalArgumentException("Command requires at least "
					+ requiredArgumentCount
					+ " arguments, but none were specified.");

		for (int i = 0; i < requiredArgumentCount; i++) {
			if (arguments.get(i) == null || arguments.get(i).length() == 0)
				throw new IllegalArgumentException("Command requires at least "
						+ requiredArgumentCount + " arguments, but argument #"
						+ (i + 1) + " was empty or null.");
		}
	}

	protected byte[] createBinarySafeRequest(int requiredArgumentCount,
			byte[]... arguments) throws IllegalArgumentException {
		// Verify the number of required arguments.
		checkArguments(requiredArgumentCount, arguments);

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

		return result.getArray();
	}

	protected byte[] createBinarySafeRequest(int requiredArgumentCount,
			char[]... arguments) {
		// Verify the number of required arguments.
		checkArguments(requiredArgumentCount, arguments);

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
		return ArrayUtils.encode(builder);
	}

	protected byte[] createBinarySafeRequest(int requiredArgumentCount,
			CharSequence... arguments) {
		// Verify the number of required arguments.
		checkArguments(requiredArgumentCount, arguments);

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
		return ArrayUtils.encode(builder);
	}

	protected byte[] createBinarySafeRequest(int requiredArgumentCount,
			List<CharSequence> arguments) {
		// Verify the number of required arguments.
		checkArguments(requiredArgumentCount, arguments);

		StringBuilder builder = new StringBuilder();

		// Add the argument count header
		builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
				.append(arguments.size()).append(Constants.CRLF_CHARS);

		// Add each of the arguments
		for (CharSequence seq : arguments) {
			// Add the length of argument header
			builder.append((char) Constants.REPLY_TYPE_BULK)
					.append(seq.length()).append(Constants.CRLF_CHARS);

			// Add the argument body
			builder.append(seq).append(Constants.CRLF_CHARS);
		}

		// Encode command
		return ArrayUtils.encode(builder);
	}
}