package com.thebuzzmedia.redis.command.keys;

import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.command.AbstractCommand;

public class SORT extends AbstractCommand {
	private static final byte[] COMMAND = "SORT".getBytes();
	private static final byte[] BY = "BY".getBytes();
	private static final byte[] LIMIT = "LIMIT".getBytes();
	private static final byte[] GET = "GET".getBytes();
	private static final byte[] ALPHA = "ALPHA".getBytes();
	private static final byte[] STORE = "STORE".getBytes();

	public static enum SortDirection {
		ASC, DESC;
	}

	public SORT(CharSequence key, CharSequence byPattern, int limitOffset,
			int limitCount, List<CharSequence> getPatternList,
			SortDirection direction, boolean alphaSort, CharSequence destination)
			throws IllegalArgumentException {
		append(COMMAND);
		append(key);

		// Add the BY pattern clause if exists
		if (byPattern != null && byPattern.length() > 0) {
			append(BY);
			append(byPattern);
		}

		// Add LIMIT if valid offset and count are provided
		if (limitOffset != Constants.UNDEFINED
				&& limitCount != Constants.UNDEFINED) {
			append(LIMIT);
			append(Integer.toString(limitOffset));
			append(Integer.toString(limitCount));
		}

		// Add any GET patterns
		if (getPatternList != null && !getPatternList.isEmpty()) {
			for (int i = 0, size = getPatternList.size(); i < size; i++) {
				append(GET);
				append(getPatternList.get(i));
			}
		}

		// Add ASC/DESC if specified
		if (direction != null)
			append(direction.name());

		// Add ALPHA if specified
		if (alphaSort)
			append(ALPHA);

		// Add STORE destination if specified
		if (destination != null && destination.length() > 0) {
			append(STORE);
			append(destination);
		}
	}
}