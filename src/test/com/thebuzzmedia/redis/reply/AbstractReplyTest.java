package com.thebuzzmedia.redis.reply;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.DefaultMarker;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public abstract class AbstractReplyTest {
	public static IMarker createMark(byte type, String text) {
		return new DefaultMarker(type, 0, text.length(), text.getBytes());
	}

	public static IMarker createBulkMark(String text) {
		int index = text.indexOf('\n');
		IMarker parent = new DefaultMarker(Constants.REPLY_TYPE_BULK, 0,
				text.getBytes());

		parent.addChildMarker(new DefaultMarker(Constants.REPLY_TYPE_BULK, 0,
				index + 1, text.getBytes()));
		parent.addChildMarker(new DefaultMarker(Constants.REPLY_TYPE_BULK,
				index + 1, text.length() - (index + 1), text.getBytes()));

		return parent;
	}

	public static IMarker createBulkMarkParseLength(String text) {
		int index = text.indexOf('\n');
		int length = Integer.parseInt(text.substring(1, index - 1));
		IMarker parent = new DefaultMarker(Constants.REPLY_TYPE_BULK, 0,
				text.getBytes());
		parent.addChildMarker(new DefaultMarker(Constants.REPLY_TYPE_BULK, 0,
				index + 1, text.getBytes()));
		parent.addChildMarker(new DefaultMarker(Constants.REPLY_TYPE_BULK,
				index + 1, length + 2, text.getBytes()));
		return parent;
	}

	// "*1\r\n$2\r\nOK\r\n"
	public static IMarker createMultiBulkSingleChild(String text) {
		int index = text.indexOf('\n');
		IMarker parent = new DefaultMarker(Constants.REPLY_TYPE_MULTI_BULK, 0,
				text.getBytes());

		// Bulk count
		parent.addChildMarker(new DefaultMarker(
				Constants.REPLY_TYPE_MULTI_BULK, 0, index + 1, text.getBytes()));

		// Single child Bulk
		parent.addChildMarker(createBulkMarkParseLength(text
				.substring(index + 1)));

		return parent;
	}
}