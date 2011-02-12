package com.thebuzzmedia.redis.reply;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;
import com.thebuzzmedia.redis.util.ArrayUtils;

public class IntegerReply implements IReply<Integer> {
	public static final byte MIN_BYTE_LENGTH = 4;

	private byte type = Constants.UNDEFINED;
	private Integer value;

	public IntegerReply(IMarker marker) throws IllegalArgumentException,
			NumberFormatException {
		if (marker == null)
			throw new IllegalArgumentException("marker cannot be null");
		if (marker.getReplyType() != Constants.REPLY_TYPE_INTEGER)
			throw new IllegalArgumentException(
					"marker.getReplyType must be equal to Constants.REPLY_TYPE_INTEGER in order to create an IntegerReply from this marker.");
		if (marker.getIndex() < 0 || marker.getLength() < MIN_BYTE_LENGTH)
			throw new IllegalArgumentException("marker index ["
					+ marker.getIndex() + "] and length [" + marker.getLength()
					+ "] do not mark the bounds of a valid Integer reply.");

		this.type = marker.getReplyType();

		try {
			this.value = parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Integer reply [marker="
							+ marker + "]", e);
		}
	}

	@Override
	public String toString() {
		return IntegerReply.class.getName() + "[value=" + getValue() + "]";
	}

	@Override
	public byte getType() {
		return type;
	}

	@Override
	public int getSize() {
		return getValue().intValue();
	}

	@Override
	public Integer getValue() {
		return value;
	}

	protected Integer parseMarker(IMarker marker) {
		return Integer.valueOf(ArrayUtils.parseInteger(marker.getIndex() + 1,
				marker.getLength() - 3, marker.getSource()));
	}
}