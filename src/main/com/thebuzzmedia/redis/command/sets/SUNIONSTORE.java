package com.thebuzzmedia.redis.command.sets;

import java.util.ArrayList;
import java.util.List;

import com.thebuzzmedia.redis.command.AbstractCommand;

public class SUNIONSTORE extends AbstractCommand {
	public static final String NAME = "SUNIONSTORE";

	public SUNIONSTORE(CharSequence destination, CharSequence... keys)
			throws IllegalArgumentException {
		List<CharSequence> args = new ArrayList<CharSequence>();

		args.add(NAME);
		args.add(destination);

		for (CharSequence key : keys)
			args.add(key);

		this.command = createBinarySafeRequest(3, args);
	}
}