package com.thebuzzmedia.redis.reply;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;
import com.thebuzzmedia.redis.util.CodingUtils;

public class BulkReply extends AbstractReply<byte[]> {
	private static final byte MIN_BYTE_LENGTH = 3;

	private byte[] value;

	public BulkReply(IMarker marker) throws IllegalArgumentException {
		if (marker == null)
			throw new IllegalArgumentException("marker cannot be null");
		if (marker.getReplyType() != Constants.REPLY_TYPE_BULK)
			throw new IllegalArgumentException(
					"marker.getReplyType must be equal to Constants.REPLY_TYPE_BULK in order to create a BulkReply from this marker.");
		if (marker.getIndex() < 0 || marker.getLength() < MIN_BYTE_LENGTH)
			throw new IllegalArgumentException(
					"marker index ["
							+ marker.getIndex()
							+ "] and length ["
							+ marker.getLength()
							+ "] do not mark the bounds of a valid Bulk reply which is always at least "
							+ MIN_BYTE_LENGTH + " bytes long.");

		this.type = marker.getReplyType();

		try {
			parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Bulk reply [marker="
							+ marker + "]", e);
		}
	}

	public int getSize() {
		return (value == null ? Constants.UNDEFINED : value.length);
	}

	public byte[] getValue() {
		return value;
	}

	public char[] getValueAsChars() {
		return (value == null ? null : CodingUtils.decode(value));
	}

	@Override
	protected void parseMarker(IMarker marker) {
		byte[] source = marker.getSource();

		/*
		 * Check for a NIL marker. This is a special case that always has the
		 * same properties and is fast/easy to check for: length of 5 ($-1\r\n)
		 * and the bytes at position 1 and 2 are "-" and "1" respectively.
		 */
		if (marker.getLength() == 5 && source[1] == 45 && source[2] == 49)
			return;
		else {
			value = new byte[marker.getLength() - 2];
			System.arraycopy(source, marker.getIndex(), value, 0, value.length);
		}
	}
}