package com.thebuzzmedia.redis.buffer;

import java.nio.Buffer;

public interface IDynamicArray<AT, BT extends Buffer> {
	public AT getArray();

	public void append(AT data);

	public void append(int index, int length, AT data);

	public void append(BT buffer);

	public void append(IDynamicArray<AT, BT> dynamicArray);

	public void ensureCapacity(int capacity);
}