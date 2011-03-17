package com.thebuzzmedia.redis.command;

import com.thebuzzmedia.redis.util.IByteArraySource;

public interface ICommand {
	public IByteArraySource getCommandData();
}