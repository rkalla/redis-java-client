package com.thebuzzmedia.redis.buffer;

public interface IArraySource<AT> {
	public int getLength();
	
	public AT getArray();
}