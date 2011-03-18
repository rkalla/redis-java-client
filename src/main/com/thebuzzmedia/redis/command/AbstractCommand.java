package com.thebuzzmedia.redis.command;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.buffer.DynamicByteArray;
import com.thebuzzmedia.redis.buffer.IDynamicArray;
import com.thebuzzmedia.redis.util.CodingUtils;

public abstract class AbstractCommand implements ICommand {
	private IDynamicArray<byte[], ByteBuffer> commandBuffer;
	private Deque<IDynamicArray<byte[], ByteBuffer>> pendingArguments;

	public AbstractCommand() {
		pendingArguments = new ArrayDeque<IDynamicArray<byte[], ByteBuffer>>(4);
	}

	// TODO: Improve this
	@Override
	public String toString() {
		return this.getClass().getName()
				+ "[command="
				+ (commandBuffer == null ? "[call getCommandData() first to generate the command]"
						: new String(commandBuffer.getArray())) + "]";
	}

	@Override
	public synchronized byte[] getBytes() {
		/*
		 * Because we need to know how many arguments are included as part of
		 * this command before formatting it into a giant Multi-Bulk query for
		 * the server, we delay building the actual command byte[] until it is
		 * requested.
		 */
		if (commandBuffer == null) {
			commandBuffer = new DynamicByteArray();

			// Prepare the MultiBulk-formatted reply
			commandBuffer
					.append(new byte[] { Constants.REPLY_TYPE_MULTI_BULK });
			commandBuffer.append(Integer.toString(pendingArguments.size())
					.getBytes());
			commandBuffer.append(Constants.CRLF_BYTES);

			// Append each of the pending arguments
			for (int i = 0, size = pendingArguments.size(); i < size; i++)
				commandBuffer.append(pendingArguments.pollFirst());

			/*
			 * The pendingArgs are empty from pollFirst, but make it easier on
			 * the GC anyway.
			 */
			pendingArguments = null;
		}

		return commandBuffer.getArray();
	}

	protected void append(CharSequence argument)
			throws IllegalArgumentException {
		if (argument == null || argument.length() == 0)
			return;

		byte[] encodedArgument = CodingUtils.encode(argument);
		append(0, encodedArgument.length, encodedArgument);
	}

	protected void append(byte[] argument) throws IllegalArgumentException {
		if (argument == null || argument.length == 0)
			return;

		append(0, argument.length, argument);
	}

	protected void append(int index, int length, byte[] argument)
			throws IllegalArgumentException {
		if (argument == null || length == 0)
			return;
		if (index < 0 || length < 0 || (index + length) > argument.length)
			throw new IllegalArgumentException("index [" + index
					+ "] and length [" + length
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
		array.append(index, length, argument);
		array.append(Constants.CRLF_BYTES);

		// Add the array to our pending args that will be sent
		pendingArguments.add(array);
	}
}