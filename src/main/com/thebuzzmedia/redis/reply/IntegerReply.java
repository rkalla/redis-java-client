package com.thebuzzmedia.redis.reply;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;
import com.thebuzzmedia.redis.util.ArrayUtils;

public class IntegerReply extends AbstractReply<Integer> {
	private static final byte MIN_BYTE_LENGTH = 4;

	private Integer value;

	public IntegerReply(IMarker marker) throws IllegalArgumentException,
			NumberFormatException {
		if (marker == null)
			throw new IllegalArgumentException("marker cannot be null");
		if (marker.getReplyType() != Constants.REPLY_TYPE_INTEGER)
			throw new IllegalArgumentException(
					"marker.getReplyType must be equal to Constants.REPLY_TYPE_INTEGER in order to create an IntegerReply from this marker.");
		if (marker.getIndex() < 0 || marker.getLength() < MIN_BYTE_LENGTH)
			throw new IllegalArgumentException(
					"marker index ["
							+ marker.getIndex()
							+ "] and length ["
							+ marker.getLength()
							+ "] do not mark the bounds of a minimum valid Integer reply.");

		this.type = marker.getReplyType();

		try {
			parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Integer reply [marker="
							+ marker + "]", e);
		}
	}

	public int getSize() {
		return Constants.UNDEFINED;
	}

	public Integer getValue() {
		return value;
	}

	@Override
	protected void parseMarker(IMarker marker) {
		this.value = Integer.valueOf(ArrayUtils.parseInteger(
				marker.getIndex() + 1, marker.getLength() - 3,
				marker.getSource()));
	}
}