package com.thebuzzmedia.redis.command;

public class ZINTERSTORE {
	public static enum AggregateType {
		NONE, MIN, MAX, SUM
	}

	public ZINTERSTORE(String destination, int keyCount, String[] keys,
			boolean useWeight, double[] weights, AggregateType type) {

	}
}