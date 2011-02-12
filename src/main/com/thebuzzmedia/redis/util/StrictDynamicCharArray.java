package com.thebuzzmedia.redis.util;

import java.nio.CharBuffer;

public class StrictDynamicCharArray {
	private char[] array;

	public StrictDynamicCharArray() {
		array = new char[0];
	}

	public char[] getArray() {
		return array;
	}

	public void append(String text) {
		if (text == null || text.length() == 0)
			return;

		append(0, text.length(), text);
	}

	public void append(int index, int length, String text)
			throws IllegalArgumentException {
		if (index < 0)
			throw new IllegalArgumentException("index must be >= 0");
		if (text != null && (index + length) > text.length())
			throw new IllegalArgumentException("index + length ["
					+ (index + length) + "] must be <= text.length() ["
					+ text.length() + "]");
		if (length == 0 && (text == null || text.length() == 0))
			return;

		int insertIndex = array.length;
		ensureCapacity(array.length + length);
		text.getChars(index, (index + length), array, insertIndex);
	}

	public void append(CharBuffer buffer) {
		if (buffer == null || buffer.remaining() == 0)
			return;

		append(buffer.arrayOffset(), buffer.remaining(), buffer.array());
	}

	public void append(int index, int length, char[] chars)
			throws IllegalArgumentException {
		if (index < 0)
			throw new IllegalArgumentException("index must be >= 0");
		if (chars != null && (index + length) > chars.length)
			throw new IllegalArgumentException("index + length ["
					+ (index + length) + "] must be <= chars.length ["
					+ chars.length + "]");
		if (length <= 0 && chars == null)
			return;

		int insertIndex = array.length;
		ensureCapacity(array.length + length);
		System.arraycopy(chars, index, array, insertIndex, length);
	}

	public void ensureCapacity(int capacity) {
		if (capacity < array.length)
			return;

		char[] newArray = new char[capacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
	}
}