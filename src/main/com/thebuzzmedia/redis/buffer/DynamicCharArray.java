package com.thebuzzmedia.redis.buffer;

import java.nio.CharBuffer;

public class DynamicCharArray implements IDynamicArray<char[], CharBuffer>,
		IArraySource<char[]> {
	private int length;
	private char[] array;

	public DynamicCharArray() {
		length = 0;
		array = new char[16];
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[length=" + length + ", array="
				+ new String(array) + "]";
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public char[] getArray() {
		return array;
	}

	@Override
	public void append(char[] data) {
		if (data == null || data.length == 0)
			return;

		append(0, data.length, data);
	}

	@Override
	public void append(int index, int length, char[] data)
			throws IllegalArgumentException {
		if (data == null || length == 0)
			return;
		if (index < 0 || length < 0 || (index + length) > data.length)
			throw new IllegalArgumentException("index [" + index
					+ "] and length [" + length
					+ "] must be >= 0 and (index+length) [" + (index + length)
					+ "] must be <= data.length [" + data.length + "]");

		int insertIndex = length;

		ensureCapacity(array.length + length);
		System.arraycopy(data, index, array, insertIndex, length);

		this.length += length;
	}

	@Override
	public void append(CharBuffer buffer) {
		if (buffer == null || buffer.remaining() == 0)
			return;

		int byteCount = buffer.remaining();
		int insertIndex = length;

		ensureCapacity(array.length + byteCount);
		buffer.get(array, insertIndex, byteCount);

		length += byteCount;
	}

	@Override
	public void append(IDynamicArray<char[], CharBuffer> dynamicArray) {
		if (dynamicArray == null || dynamicArray.getLength() == 0)
			return;

		append(0, dynamicArray.getLength(), dynamicArray.getArray());
	}

	@Override
	public void ensureCapacity(int capacity) {
		if (capacity < array.length)
			return;

		/*
		 * Growth logic copied from java.util.ArrayList, it is a good balance
		 * between conservative and aggressive enough to rule out lots of
		 * allocations during appending of small values.
		 * 
		 * Also in the case of appending huge values, it resizes exactly to the
		 * required capacity, so as not to waste a lot of space with overly huge
		 * allocations.
		 */
		int newCapacity = (array.length * 3) / 2 + 1;

		if (newCapacity < capacity)
			newCapacity = capacity;

		char[] newArray = new char[newCapacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
	}
}