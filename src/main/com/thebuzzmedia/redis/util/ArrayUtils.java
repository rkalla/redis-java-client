package com.thebuzzmedia.redis.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.thebuzzmedia.redis.Constants;

public class ArrayUtils {
	public static final int MAX_ENCODE_BUFFER_SIZE = Integer.getInteger(
			"redis.util.maxEncodeBufferSize", 4096);
	public static final int MAX_DECODE_BUFFER_SIZE = Integer.getInteger(
			"redis.util.maxDecodeBufferSize", 4096);

	/**
	 * Source: http://snippets.dzone.com/posts/show/93
	 * 
	 * Modification made to only return a <code>byte[]</code> whose length
	 * matches the number of digits in the original integer.
	 * 
	 * @param value
	 *            The integer value to convert to a byte array.
	 * 
	 * @return a <code>byte[]</code> containing the numeric value of the
	 *         original integer. The length of the array will match the number
	 *         of digits in the number.
	 */
	public static byte[] intToByteArray(int value) {
		byte a = (byte) (value >>> 24);
		byte b = (byte) (value >>> 16);
		byte c = (byte) (value >>> 8);
		byte d = (byte) value;

		if (a > 0)
			return new byte[] { a, b, c, d };
		else if (b > 0)
			return new byte[] { b, c, d };
		else if (c > 0)
			return new byte[] { c, d };
		else
			return new byte[] { d };
	}

	public static int indexOfCRLF(int index, int length, byte[] data)
			throws IllegalArgumentException {
		if (index < 0)
			throw new IllegalArgumentException("index must be >= 0");
		if (length < 0)
			throw new IllegalArgumentException("length must be >= 0");
		if (data == null)
			throw new IllegalArgumentException("data cannot be null");
		if ((index + length) > data.length)
			throw new IllegalArgumentException(
					"(index + length) must be < data.length");

		int result = Constants.UNDEFINED;

		// Loop until we hit length or find the index of \r
		for (int i = 0; result == Constants.UNDEFINED && i < length; i++) {
			/*
			 * If the current index is \r and, length permitting, the next
			 * character is \n, then return the current index.
			 */
			if (data[index + i] == Constants.CR
					&& (index + i + 1) < data.length
					&& data[index + i + 1] == Constants.LF)
				result = index + i;
		}

		return result;
	}

	/**
	 * Used to parse an integer from a <code>byte[]</code> starting at the given
	 * index and using the given number of bytes. Parsing values from
	 * {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE} is supported.
	 * <p/>
	 * Because this method cannot return an integer value representing a failed
	 * parse attempt (e.g. -1) it will throw an exception if any of the input
	 * values are invalid or if an integer cannot be parsed from the given text
	 * cleanly (i.e. non-ASCII bytes representing chars '0' through '9' are
	 * encountered). Care should be taken to either protect against bad values
	 * before hand or handle the exceptions by the caller.
	 * <p/>
	 * The implementation of this method is highly efficient, using no
	 * object-creation during parsing and only allocating a few primitives for
	 * state-tracking.
	 * 
	 * @param index
	 *            The index to start parsing from.
	 * @param length
	 *            The number of bytes to parse for the integer.
	 * @param data
	 *            The bytes to get the characters from.
	 * 
	 * @return an integer parsed from the bytes.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>data</code> is <code>null</code> or empty, if
	 *             <code>length</code> &lt;= 0, if <code>index</code> is &lt; 0
	 *             or if <code>index + length</code> is &gt;
	 *             <code>data.length</code>.
	 * @throws NumberFormatException
	 *             if an non-numeric char with an ASCII code &lt; 48 (0) or &gt;
	 *             57 (9) is encountered.
	 */
	public static int parseInteger(int index, int length, byte[] data)
			throws IllegalArgumentException, NumberFormatException {
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data cannot be null or empty");
		if (length <= 0)
			throw new IllegalArgumentException("length must be > 0");
		if (index < 0 || (index + length) > data.length)
			throw new IllegalArgumentException("index [" + index
					+ "] must be >= 0 and index + length [" + (index + length)
					+ "] must be <= text.length [" + data.length + "]");

		int val = 0;

		// Check the first byte to see if this is a negative number: '-' is 45
		boolean neg = (data[index] == 45 ? true : false);

		/*
		 * Begin iterating on the bytes, parsing the number. If we are dealing
		 * with a negative number, start on the index + 1 element because the
		 * element at index is '-'.
		 */
		for (int i = (neg ? 1 : 0); i < length; i++) {
			/*
			 * Get the byte representing the digit. Convert it quickly to an int
			 * by subtracting the ASCII value of '0' from it (48); yielding its
			 * true int value.
			 */
			int num = (data[index + i] - 48);

			/*
			 * If the number parsed is less than 0 or greater than 9, then it
			 * wasn't an ASCII character byte representing a number and we have
			 * to throw an exception.
			 * 
			 * We have to throw an exception when an invalid value is found
			 * because we cannot return -1 from this method; that is a valid
			 * value!
			 */
			if (num < 0 || num > 9)
				throw new NumberFormatException(
						"A non-numeric character (ASCII code " + (num + '0')
								+ ") was encountered at position "
								+ (index + i)
								+ " while trying to parse an integer from '"
								+ new String(data, index, length) + "'.");

			/*
			 * Adjust the value of the parsed number by magnitudes of 10 to
			 * ensure its value is adjusted for the right place it was in (e.g.
			 * one's place, ten's place, etc.)
			 */
			for (int j = i; j < length - 1; j++)
				num *= 10;

			// Add the parsed value to the running total.
			val += num;
		}

		// Negate the number of it was a negative or just return the total.
		return (neg ? -val : val);
	}

	public static IByteArraySource encode(char[] chars) {
		return (chars == null ? null : encode(CharBuffer.wrap(chars)));
	}

	public static IByteArraySource encode(CharSequence chars) {
		return (chars == null ? null : encode(CharBuffer.wrap(chars)));
	}

	public static ICharArraySource decode(byte[] bytes) {
		IDynamicArray<char[], CharBuffer> result = null;

		if (bytes == null) {
			CharsetDecoder decoder = Constants.getDecoder();

			ByteBuffer src = ByteBuffer.wrap(bytes);
			CharBuffer dest = CharBuffer
					.allocate(src.remaining() < MAX_DECODE_BUFFER_SIZE ? src
							.remaining() : MAX_DECODE_BUFFER_SIZE);
			result = new DynamicCharArray();

			// Reset the decoder
			decoder.reset();

			while (src.hasRemaining()) {
				/*
				 * Decode the first buffer.capacity chars, passing 'false' to
				 * indicate that we aren't sure if we are done with the decode
				 * operation yet.
				 */
				decoder.decode(src, dest, false);

				// Append what we successfully decoded to our tally
				dest.flip();
				result.append(dest);

				// If there is no more to decode, go through finalization
				if (!src.hasRemaining()) {
					dest.clear();

					/*
					 * Per the CharsetDecoder Javadocs, decoders must be given
					 * an opportunity to "finalize" their internal state and
					 * flush out any pending operations once we know we've hit
					 * the end of the chars to decode.
					 */
					decoder.decode(src, dest, true);
					decoder.flush(dest);

					dest.flip();

					// If any finalized bytes were written, append them.
					if (dest.hasRemaining()) {
						result.append(dest);
					}
				}
			}
		}

		return (DynamicCharArray) result;
	}

	private static IByteArraySource encode(CharBuffer in) {
		IDynamicArray<byte[], ByteBuffer> result = null;

		if (in != null) {
			CharsetEncoder encoder = Constants.getEncoder();

			int size = Math.round(encoder.averageBytesPerChar()
					* (float) in.remaining());
			ByteBuffer buffer = ByteBuffer
					.allocate(size < MAX_ENCODE_BUFFER_SIZE ? size
							: MAX_ENCODE_BUFFER_SIZE);
			result = new DynamicByteArray();

			// Reset the encoder
			encoder.reset();

			while (in.hasRemaining()) {
				/*
				 * Encode the first buffer.capacity chars, passing 'false' to
				 * indicate that we aren't sure if we are done with the encode
				 * operation yet.
				 */
				encoder.encode(in, buffer, false);

				// Append what we successfully encoded to our tally
				buffer.flip();
				result.append(buffer);

				// If there is no more to encode, go through finalization
				if (!in.hasRemaining()) {
					buffer.clear();

					/*
					 * Per the CharsetEncoder Javadocs, encoders must be given
					 * an opportunity to "finalize" their internal state and
					 * flush out any pending operations once we know we've hit
					 * the end of the bytes to encode.
					 */
					encoder.encode(in, buffer, true);
					encoder.flush(buffer);

					buffer.flip();

					// If any finalized bytes were written, append them.
					if (buffer.hasRemaining()) {
						result.append(buffer);
					}
				}
			}
		}

		return (DynamicByteArray) result;
	}
}