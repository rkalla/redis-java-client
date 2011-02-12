package com.thebuzzmedia.redis.util;

import java.nio.ByteBuffer;

public class StrictDynamicByteArray {
	private byte[] array;

	public StrictDynamicByteArray() {
		array = new byte[0];
	}

	public byte[] getArray() {
		return array;
	}

	public void append(ByteBuffer buffer) throws IllegalArgumentException {
		if (buffer == null)
			throw new IllegalArgumentException("buffer cannot be null");

		int insertIndex = array.length;
		ensureCapacity(array.length + buffer.remaining());
		buffer.get(array, insertIndex, buffer.remaining());
	}

	public void append(byte[] bytes) {
		if (bytes == null)
			return;

		append(0, bytes.length, bytes);
	}

	public void append(int index, int length, byte[] bytes)
			throws IllegalArgumentException {
		if (index < 0)
			throw new IllegalArgumentException("index must be >= 0");
		if (bytes == null)
			throw new IllegalArgumentException("bytes cannot be null");
		if ((index + length) > bytes.length)
			throw new IllegalArgumentException("index + length ["
					+ (index + length) + "] must be <= bytes.length ["
					+ bytes.length + "]");
		if (length < 0)
			return;

		int insertIndex = array.length;
		ensureCapacity(array.length + length);
		System.arraycopy(bytes, index, array, insertIndex, length);
	}

	public void ensureCapacity(int capacity) {
		if (capacity < array.length)
			return;

		byte[] newArray = new byte[capacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
	}
}