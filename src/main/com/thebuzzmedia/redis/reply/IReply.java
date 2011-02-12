package com.thebuzzmedia.redis.reply;

public interface IReply<T> {
	public byte getType();

	public int getSize();

	public T getValue();
}