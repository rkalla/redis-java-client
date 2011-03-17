package com.thebuzzmedia.redis.buffer;

import java.nio.ByteBuffer;

public class DynamicByteArray implements IDynamicArray<byte[], ByteBuffer>,
		IArraySource<byte[]> {

	private int length;
	private byte[] array;

	public DynamicByteArray() {
		length = 0;
		array = new byte[16];
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[length=" + length + ", array="
				+ new String(array, 0, length) + "]";
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public byte[] getArray() {
		return array;
	}

	@Override
	public void append(byte[] data) {
		if (data == null || data.length == 0)
			return;

		append(0, data.length, data);
	}

	@Override
	public void append(int index, int length, byte[] data)
			throws IllegalArgumentException {
		if (data == null || length == 0)
			return;
		if (index < 0 || length < 0 || (index + length) > data.length)
			throw new IllegalArgumentException("index [" + index
					+ "] and length [" + length
					+ "] must be >= 0 and (index+length) [" + (index + length)
					+ "] must be <= data.length [" + data.length + "]");

		int insertIndex = this.length;

		ensureCapacity(this.length + length);
		System.arraycopy(data, index, array, insertIndex, length);

		this.length += length;
	}

	@Override
	public void append(ByteBuffer buffer) {
		if (buffer == null || buffer.remaining() == 0)
			return;

		int byteCount = buffer.remaining();
		int insertIndex = length;

		ensureCapacity(length + byteCount);
		buffer.get(array, insertIndex, byteCount);

		length += byteCount;
	}

	@Override
	public void append(IDynamicArray<byte[], ByteBuffer> dynamicArray) {
		if (dynamicArray == null || dynamicArray.getLength() == 0)
			return;

		append(0, dynamicArray.getLength(), dynamicArray.getArray());
	}

	@Override
	public void ensureCapacity(int capacity) {
		if (capacity < array.length)
			return;

		// Growth logic copied from java.util.ArrayList
		int newCapacity = (array.length * 3) / 2 + 1;

		if (newCapacity < capacity)
			newCapacity = capacity;

		byte[] newArray = new byte[newCapacity];
		System.arraycopy(array, 0, newArray, 0, array.length);
		array = newArray;
	}
}