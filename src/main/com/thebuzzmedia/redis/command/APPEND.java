package com.thebuzzmedia.redis.command;


public class APPEND implements ICommand{
	private String key;
	private String value;
	
	public APPEND(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getName() {
		return "APPEND";
	}

	@Override
	public char[] getFullCommand() {
		// TODO Auto-generated method stub
		return null;
	}
}