package com.thebuzzmedia.redis.command;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.util.ArrayUtils;
import com.thebuzzmedia.redis.util.DynamicByteArray;
import com.thebuzzmedia.redis.util.IByteArraySource;
import com.thebuzzmedia.redis.util.IDynamicArray;

public abstract class AbstractCommand implements ICommand {
	private IDynamicArray<byte[], ByteBuffer> command;
	private Deque<IDynamicArray<byte[], ByteBuffer>> pendingArgs;

	public AbstractCommand() {
		pendingArgs = new ArrayDeque<IDynamicArray<byte[], ByteBuffer>>(4);
	}

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
		return this.getClass().getName()
				+ "[command="
				+ (command == null ? "[call getCommandData() first to generate the command]"
						: new String(command.getArray())) + "]";
	}

	@Override
	public synchronized IByteArraySource getCommandData() {
		/*
		 * Because we need to know how many arguments are included as part of
		 * this command before formatting it into a giant Multi-Bulk query for
		 * the server, we delay building the actual command byte[] until it is
		 * requested.
		 */
		if (command == null) {
			command = new DynamicByteArray();

			// Prepare the MultiBulk-formatted reply
			command.append(new byte[] { Constants.REPLY_TYPE_MULTI_BULK });
			// command.append(ArrayUtils.intToByteArray(pendingArgs.size()));
			command.append(Integer.toString(pendingArgs.size()).getBytes());
			command.append(Constants.CRLF_BYTES);

			// Append each of the pending arguments
			for (int i = 0, size = pendingArgs.size(); i < size; i++)
				command.append(pendingArgs.pollFirst());

			// The pendingArgs are empty, make it easier on the GC
			pendingArgs = null;
		}

		return (DynamicByteArray) command;
	}

	// TODO: Remove the throws IAE from here, move the verification up a level.
	protected void append(CharSequence argument)
			throws IllegalArgumentException {
		if (argument == null || argument.length() == 0)
			throw new IllegalArgumentException(
					"argument cannot be null or empty. Trying to append a null or empty argument to the command buffer is typically a mistake.");

		IByteArraySource source = ArrayUtils.encode(argument);
		append(source.getArray(), 0, source.getLength());
	}

	protected void append(byte[] argument) throws IllegalArgumentException {
		if (argument == null || argument.length == 0)
			throw new IllegalArgumentException(
					"argument cannot be null or empty. Trying to append a null or empty argument to the command buffer is typically a mistake.");

		append(argument, 0, argument.length);
	}

	protected void append(byte[] argument, int index, int length)
			throws IllegalArgumentException {
		if (argument == null || argument.length == 0)
			throw new IllegalArgumentException(
					"argument cannot be null or empty. Trying to append a null or empty argument to the command buffer is typically a mistake.");
		if (index < 0 || (index + length) > argument.length)
			throw new IllegalArgumentException("index [" + index
					+ "] must be >= 0 and (index + length) ["
					+ (index + length) + "] must be <= argument.length ["
					+ argument.length + "]");

		IDynamicArray<byte[], ByteBuffer> array = new DynamicByteArray();

		// First, append the Bulk-formatted length header
		array.append(new byte[] { Constants.REPLY_TYPE_BULK });
		// array.append(ArrayUtils.intToByteArray(argument.length));
		array.append(Integer.toString(length).getBytes());
		array.append(Constants.CRLF_BYTES);

		// Second, append the Bulk-formatted data payload
		array.append(argument, index, length);
		array.append(Constants.CRLF_BYTES);

		// Add the array to our pending args that will be sent
		pendingArgs.add(array);
	}

	//
	// protected void checkArguments(int requiredArgumentCount,
	// byte[]... arguments) throws IllegalArgumentException {
	// if (requiredArgumentCount > 0 && arguments == null
	// || arguments.length == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount
	// + " arguments, but none were specified.");
	//
	// for (int i = 0; i < requiredArgumentCount; i++) {
	// if (arguments[i] == null || arguments[i].length == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount + " arguments, but argument #"
	// + (i + 1) + " was empty or null.");
	// }
	// }
	//
	// protected void checkArguments(int requiredArgumentCount,
	// char[]... arguments) throws IllegalArgumentException {
	// if (requiredArgumentCount > 0 && arguments == null
	// || arguments.length == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount
	// + " arguments, but none were specified.");
	//
	// for (int i = 0; i < requiredArgumentCount; i++) {
	// if (arguments[i] == null || arguments[i].length == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount + " arguments, but argument #"
	// + (i + 1) + " was empty or null.");
	// }
	// }
	//
	// protected void checkArguments(int requiredArgumentCount,
	// CharSequence... arguments) throws IllegalArgumentException {
	// if (requiredArgumentCount > 0 && arguments == null
	// || arguments.length == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount
	// + " arguments, but none were specified.");
	//
	// for (int i = 0; i < requiredArgumentCount; i++) {
	// if (arguments[i] == null || arguments[i].length() == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount + " arguments, but argument #"
	// + (i + 1) + " was empty or null.");
	// }
	// }
	//
	// protected void checkArguments(int requiredArgumentCount,
	// List<CharSequence> arguments) throws IllegalArgumentException {
	// if (requiredArgumentCount > 0 && arguments == null
	// || arguments.size() == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount
	// + " arguments, but none were specified.");
	//
	// for (int i = 0; i < requiredArgumentCount; i++) {
	// if (arguments.get(i) == null || arguments.get(i).length() == 0)
	// throw new IllegalArgumentException("Command requires at least "
	// + requiredArgumentCount + " arguments, but argument #"
	// + (i + 1) + " was empty or null.");
	// }
	// }
	//
	// protected byte[] createBinarySafeRequest(int requiredArgumentCount,
	// byte[]... arguments) throws IllegalArgumentException {
	// // Verify the number of required arguments.
	// checkArguments(requiredArgumentCount, arguments);
	//
	// StringBuilder builder = new StringBuilder();
	// StrictDynamicByteArray result = new StrictDynamicByteArray();
	//
	// // Add the argument count header
	// builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
	// .append(arguments.length).append(Constants.CRLF_CHARS);
	// result.append(ArrayUtils.encode(builder));
	//
	// // Add each of the arguments
	// for (byte[] bytes : arguments) {
	// builder.setLength(0);
	//
	// // Add the length of argument header
	// builder.append((char) Constants.REPLY_TYPE_BULK)
	// .append(bytes.length).append(Constants.CRLF_CHARS);
	// result.append(ArrayUtils.encode(builder));
	//
	// // Add the argument body
	// result.append(bytes);
	// result.append(Constants.CRLF_BYTES);
	// }
	//
	// return result.getArray();
	// }
	//
	// protected byte[] createBinarySafeRequest(int requiredArgumentCount,
	// char[]... arguments) {
	// // Verify the number of required arguments.
	// checkArguments(requiredArgumentCount, arguments);
	//
	// StringBuilder builder = new StringBuilder();
	//
	// // Add the argument count header
	// builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
	// .append(arguments.length).append(Constants.CRLF_CHARS);
	//
	// // Add each of the arguments
	// for (char[] chars : arguments) {
	// // Add the length of argument header
	// builder.append((char) Constants.REPLY_TYPE_BULK)
	// .append(chars.length).append(Constants.CRLF_CHARS);
	//
	// // Add the argument body
	// builder.append(chars).append(Constants.CRLF_CHARS);
	// }
	//
	// // Encode command
	// return ArrayUtils.encode(builder);
	// }
	//
	// protected byte[] createBinarySafeRequest(int requiredArgumentCount,
	// CharSequence... arguments) {
	// // Verify the number of required arguments.
	// checkArguments(requiredArgumentCount, arguments);
	//
	// StringBuilder builder = new StringBuilder();
	//
	// // Add the argument count header
	// builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
	// .append(arguments.length).append(Constants.CRLF_CHARS);
	//
	// // Add each of the arguments
	// for (CharSequence seq : arguments) {
	// // Add the length of argument header
	// builder.append((char) Constants.REPLY_TYPE_BULK)
	// .append(seq.length()).append(Constants.CRLF_CHARS);
	//
	// // Add the argument body
	// builder.append(seq).append(Constants.CRLF_CHARS);
	// }
	//
	// // Encode command
	// return ArrayUtils.encode(builder);
	// }
	//
	// protected byte[] createBinarySafeRequest(int requiredArgumentCount,
	// List<CharSequence> arguments) {
	// // Verify the number of required arguments.
	// checkArguments(requiredArgumentCount, arguments);
	//
	// StringBuilder builder = new StringBuilder();
	//
	// // Add the argument count header
	// builder.append((char) Constants.REPLY_TYPE_MULTI_BULK)
	// .append(arguments.size()).append(Constants.CRLF_CHARS);
	//
	// // Add each of the arguments
	// for (CharSequence seq : arguments) {
	// // Add the length of argument header
	// builder.append((char) Constants.REPLY_TYPE_BULK)
	// .append(seq.length()).append(Constants.CRLF_CHARS);
	//
	// // Add the argument body
	// builder.append(seq).append(Constants.CRLF_CHARS);
	// }
	//
	// // Encode command
	// return ArrayUtils.encode(builder);
	// }
}