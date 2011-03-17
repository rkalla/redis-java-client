package com.thebuzzmedia.redis.util;

public interface IByteArraySource {
	public int getLength();
	
	public byte[] getArray();
}