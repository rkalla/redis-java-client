package com.thebuzzmedia.redis.command;

import com.thebuzzmedia.redis.buffer.IArraySource;

public interface ICommand {
	public IArraySource<byte[]> getByteSource();
}