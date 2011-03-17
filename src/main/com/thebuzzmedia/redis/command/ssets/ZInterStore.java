package com.thebuzzmedia.redis.command.ssets;

import java.util.List;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class ZInterStore extends AbstractCommand {
	private static final byte[] COMMAND = "ZINTERSTORE".getBytes();
	private static final byte[] WEIGHTS = "WEIGHTS".getBytes();
	private static final byte[] AGGREGATE = "AGGREGATE".getBytes();

	public static enum AggregateType {
		SUM, MIN, MAX
	}

	public ZInterStore(CharSequence destination, List<CharSequence> setKeyList,
			List<Double> weightList, AggregateType type)
			throws IllegalArgumentException {
		if (destination == null || destination.length() == 0)
			throw new IllegalArgumentException(
					"destination cannot be null or empty");
		if (setKeyList == null || setKeyList.size() == 0)
			throw new IllegalArgumentException(
					"setKeyList must contain 1 or more sorted set key names");
		if (weightList != null && weightList.size() != setKeyList.size())
			throw new IllegalArgumentException(
					"if a weightList is provided (non-null), it must contain a weight multiplier for every sorted set key specified in setKeyList.");

		append(COMMAND);
		append(destination);
		append(Integer.toString(setKeyList.size()));

		// Add sorted set keys
		for (int i = 0, size = setKeyList.size(); i < size; i++)
			append(setKeyList.get(i));

		// Add WEIGHTS if specified
		if (weightList != null && !weightList.isEmpty()) {
			append(WEIGHTS);

			for (int i = 0, size = weightList.size(); i < size; i++)
				append(weightList.get(i).toString());
		}

		// Add AGGREGATE if specified
		if (type != null) {
			append(AGGREGATE);
			append(type.name().getBytes());
		}
	}
}