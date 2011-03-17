package com.thebuzzmedia.redis.util;

import com.thebuzzmedia.redis.Constants;

public class ArrayUtils {
	/**
	 * Used to find the first index of CR occurring in a byte-array that is
	 * immediately followed by a LF.
	 * 
	 * @param index
	 *            The index to begin the search at.
	 * @param length
	 *            The number of bytes to search from index.
	 * @param data
	 *            The byte[] to search for the CRLF pairing.
	 * 
	 * @return the first index of CR occurring in a byte-array that is
	 *         immediately followed by a LF.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>data</code> is <code>null</code>, if
	 *             <code>index</code> is &lt; <code>0</code>, if
	 *             <code>length</code> is &lt; <code>0</code>, if
	 *             <code>(index + length)</code> &gt; <code>data.length</code>.
	 */
	public static int indexOfCRLF(int index, int length, byte[] data)
			throws IllegalArgumentException {
		if (data == null)
			throw new IllegalArgumentException("data cannot be null");
		if (index < 0 || length < 0 || (index + length > data.length))
			throw new IllegalArgumentException("index [" + index
					+ "] and length [" + length
					+ "] must be >= 0 and (index + length) ["
					+ (index + length) + "] must be <= data.length ["
					+ data.length + "]");

		int result = Constants.UNDEFINED;

		// Loop until we hit length or find the index of \r
		for (int i = 0; result == Constants.UNDEFINED && i < length; i++) {
			int nextIndex = (index + i + 1);

			/*
			 * If the current index is \r and, length permitting, the next
			 * character is \n, then return the current index.
			 */
			if (data[index + i] == Constants.CR && nextIndex < length
					&& data[nextIndex] == Constants.LF)
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
	 * cleanly (i.e. non-ASCII bytes representing chars '0' through '9'
	 * (starting char '-' is allowed for negatives) are encountered). Care
	 * should be taken to either protect against bad values before hand or
	 * handle the exceptions by the caller.
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
	 *             57 (9) is encountered. If the first char is '-' (for a
	 *             negative) that is fine.
	 */
	public static int parseInteger(int index, int length, byte[] data)
			throws IllegalArgumentException, NumberFormatException {
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data cannot be null or empty");
		if (index < 0 || length <= 0 || (index + length > data.length))
			throw new IllegalArgumentException("index [" + index
					+ "] must be >= 0 and length [" + length
					+ "] must be > 0 and (index + length) [" + (index + length)
					+ "] must be <= data.length [" + data.length + "]");

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
						"A non-numeric character (ASCII code " + (num + 48)
								+ ") was encountered at index " + (index + i)
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
}