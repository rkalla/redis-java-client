package com.thebuzzmedia.redis.util;

import java.nio.Buffer;

public interface IDynamicArray<AT, BT extends Buffer> {
	public int getLength();

	public AT getArray();

	public void append(AT data);
	
	public void append(AT data, int index, int length);

	public void append(BT buffer);

	public void append(IDynamicArray<AT, BT> dynamicArray);

	public void ensureCapacity(int capacity);
}