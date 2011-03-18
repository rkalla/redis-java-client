package com.thebuzzmedia.redis.reply;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;
import com.thebuzzmedia.redis.util.CodingUtils;

public class ErrorReply extends AbstractReply<char[]> {
	private static final byte MIN_BYTE_LENGTH = 4;

	private char[] value;

	public ErrorReply(IMarker marker) throws IllegalArgumentException {
		if (marker == null)
			throw new IllegalArgumentException("marker cannot be null");
		if (marker.getReplyType() != Constants.REPLY_TYPE_ERROR)
			throw new IllegalArgumentException(
					"marker.getReplyType must be equal to Constants.REPLY_TYPE_ERROR in order to create a ErrorReply from this marker.");
		if (marker.getIndex() < 0 || marker.getLength() < MIN_BYTE_LENGTH)
			throw new IllegalArgumentException("marker index ["
					+ marker.getIndex() + "] and length [" + marker.getLength()
					+ "] do not mark the bounds of a valid Error reply.");

		this.type = marker.getReplyType();

		try {
			parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Error reply [marker="
							+ marker + "]", e);
		}
	}

	public int getSize() {
		return value.length;
	}

	public char[] getValue() {
		return value;
	}

	@Override
	protected void parseMarker(IMarker marker) {
		this.value = CodingUtils.decode(marker.getIndex() + 1,
				marker.getLength() - 3, marker.getSource());
	}
}