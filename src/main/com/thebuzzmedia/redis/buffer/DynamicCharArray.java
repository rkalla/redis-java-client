package com.thebuzzmedia.redis.buffer;

import java.nio.CharBuffer;

public class DynamicCharArray implements IDynamicArray<char[], CharBuffer> {
	private char[] array;

	public DynamicCharArray() {
		array = new char[16];
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[length=" + array.length
				+ ", array=" + new String(array) + "]";
	}

	public char[] getArray() {
		return array;
	}

	public void append(char[] data) {
		if (data == null || data.length == 0)
			return;

		append(0, data.length, data);
	}

	public void append(int index, int length, char[] data)
			throws IllegalArgumentException {
		if (data == null || length == 0)
			return;
		if (index < 0 || length < 0 || (index + length) > data.length)
			throw new IllegalArgumentException("index [" + index
					+ "] and length [" + length
					+ "] must be >= 0 and (index+length) [" + (index + length)
					+ "] must be <= data.length [" + data.length + "]");

		int insertIndex = array.length;

		ensureCapacity(array.length + length);
		System.arraycopy(data, index, array, insertIndex, length);
	}

	public void append(CharBuffer buffer) {
		if (buffer == null || buffer.remaining() == 0)
			return;

		int byteCount = buffer.remaining();
		int insertIndex = array.length;

		ensureCapacity(array.length + byteCount);
		buffer.get(array, insertIndex, byteCount);
	}

	public void append(IDynamicArray<char[], CharBuffer> dynamicArray) {
		if (dynamicArray == null)
			return;

		char[] data = dynamicArray.getArray();
		append(0, data.length, data);
	}

	public void ensureCapacity(int capacity) {
		if (capacity < array.length)
			return;

		char[] newArray = new char[capacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
	}
}