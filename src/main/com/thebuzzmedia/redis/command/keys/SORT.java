package com.thebuzzmedia.redis.command.keys;

import java.util.ArrayList;
import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.command.AbstractCommand;

public class SORT extends AbstractCommand {
	public static final String NAME = "SORT";

	public static enum SortDirection {
		ASC, DESC;
	}

	public SORT(CharSequence key, CharSequence byPattern, int limitOffset,
			int limitCount, List<CharSequence> getPatternList,
			SortDirection direction, boolean alphaSort, CharSequence destination)
			throws IllegalArgumentException {
		List<CharSequence> args = new ArrayList<CharSequence>();

		args.add(NAME);
		args.add(key);

		// Add the BY pattern clause if exists
		if (byPattern != null && byPattern.length() > 0) {
			args.add("BY");
			args.add(byPattern);
		}

		// Add the limit clause if valid offset and count are provided
		if (limitOffset != Constants.UNDEFINED
				&& limitCount != Constants.UNDEFINED) {
			args.add("LIMIT");
			args.add(Integer.toString(limitOffset));
			args.add(Integer.toString(limitCount));
		}

		// Add any GET patterns
		if (getPatternList != null && !getPatternList.isEmpty()) {
			for (CharSequence getPattern : getPatternList) {
				args.add("GET");
				args.add(getPattern);
			}
		}

		// Add sort direction if available
		if (direction != null)
			args.add(direction.name());

		// Add ALPHA if requested
		if (alphaSort)
			args.add("ALPHA");

		// Add STORE destination if specified
		if (destination != null && destination.length() > 0) {
			args.add("STORE");
			args.add(destination);
		}

		this.command = createBinarySafeRequest(2, args);
	}
}