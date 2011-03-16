package com.thebuzzmedia.redis.reply;

import java.util.ArrayList;
import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public class MultiBulkReply implements IReply<List<BulkReply>> {
	public static final byte MIN_BYTE_LENGTH = 4;

	private byte type = Constants.UNDEFINED;
	private List<BulkReply> value;

	public MultiBulkReply(IMarker marker) throws IllegalArgumentException {
		if (marker == null)
			throw new IllegalArgumentException("marker cannot be null");
		if (marker.getReplyType() != Constants.REPLY_TYPE_MULTI_BULK)
			throw new IllegalArgumentException(
					"marker.getReplyType must be equal to Constants.REPLY_TYPE_MULTI_BULK in order to create a MultiBulkReply from this marker.");
		if (marker.getIndex() < 0 || marker.getLength() < MIN_BYTE_LENGTH)
			throw new IllegalArgumentException("marker index ["
					+ marker.getIndex() + "] and length [" + marker.getLength()
					+ "] do not mark the bounds of a valid Multi-Bulk reply.");

		this.type = marker.getReplyType();

		try {
			this.value = parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Multi-Bulk reply [marker="
							+ marker + "]", e);
		}
	}

	@Override
	public String toString() {
		return MultiBulkReply.class.getName() + "[isNil=" + isNil() + ", size="
				+ getSize() + ", childBulkReplies=" + value + "]";
	}

	@Override
	public byte getType() {
		return type;
	}

	@Override
	public int getSize() {
		return (value == null ? Constants.UNDEFINED : value.size());
	}

	@Override
	public List<BulkReply> getValue() {
		return value;
	}

	public boolean isNil() {
		return (value == null);
	}

	protected List<BulkReply> parseMarker(IMarker marker) {
		/*
		 * First we check if this marker represents a nil reply (has no
		 * children). If it has children then it is a valid bulk reply and we
		 * should process it as such.
		 */
		if (!marker.hasChildren())
			return null;
		else {
			List<IMarker> markerList = marker.getChildMarkerList();

			int size = markerList.size();
			List<BulkReply> replyList = new ArrayList<BulkReply>(size);

			/*
			 * Run through all the markers we have for child BulkReplies and
			 * create/add them to this MultiBulk.
			 */
			for (int i = 0; i < size; i++) {
				replyList.add(new BulkReply(markerList.get(i)));
			}

			return replyList;
		}
	}
}