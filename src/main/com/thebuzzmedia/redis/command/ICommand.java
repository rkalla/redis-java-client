package com.thebuzzmedia.redis.command;

public interface ICommand {
	public String getName();

	public char[] getFullCommand();
}