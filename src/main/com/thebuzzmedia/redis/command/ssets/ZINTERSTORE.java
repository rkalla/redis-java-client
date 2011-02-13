package com.thebuzzmedia.redis.command.ssets;

import java.util.ArrayList;
import java.util.List;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class ZINTERSTORE extends AbstractCommand {
	public static final String NAME = "ZINTERSTORE";

	public static enum AggregateType {
		SUM, MIN, MAX
	}

	public ZINTERSTORE(CharSequence destination, List<CharSequence> keyList,
			List<Double> weightList, AggregateType type)
			throws IllegalArgumentException {
		if (keyList == null || keyList.size() == 0)
			throw new IllegalArgumentException(
					"keyList must contain 1 or more sort set key names to be unioned with each other.");
		if (weightList != null && weightList.size() != keyList.size())
			throw new IllegalArgumentException(
					"weightList must contain a weight multiplier for every sorted set key specified in keyList; each list must be the same size.");

		List<CharSequence> args = new ArrayList<CharSequence>();

		args.add(NAME);
		args.add(destination);
		args.add(Integer.toString(keyList.size()));

		// Add sorted set keys to union
		for (CharSequence key : keyList)
			args.add(key);

		// Add WEIGHTS if specified
		if (weightList != null && weightList.size() > 0) {
			args.add("WEIGHTS");

			for (Double weight : weightList)
				args.add(weight.toString());
		}

		// Add AGGREGATE if specified
		if (type != null) {
			args.add("AGGREGATE");
			args.add(type.name());
		}

		this.command = createBinarySafeRequest(3 + keyList.size(), args);
	}
}