package com.thebuzzmedia.redis.reply;

import java.util.ArrayList;
import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public class MultiBulkReply extends AbstractReply<List<BulkReply>> {
	private static final byte MIN_BYTE_LENGTH = 4;

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
			parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Multi-Bulk reply [marker="
							+ marker + "]", e);
		}
	}

	public int getSize() {
		return (value == null ? Constants.UNDEFINED : value.size());
	}

	public List<BulkReply> getValue() {
		return value;
	}

	@Override
	protected void parseMarker(IMarker marker) {
		/*
		 * First we check if this marker represents a nil reply (has no
		 * children). If it has children then it is a valid bulk reply and we
		 * should process create all the child BulkReplies from the child
		 * markers.
		 */
		if (marker.hasChildren()) {
			List<IMarker> markerList = marker.getChildMarkerList();

			int size = markerList.size();
			value = new ArrayList<BulkReply>(size);

			/*
			 * Run through all the markers we have for child BulkReplies and
			 * create/add them to this MultiBulk.
			 */
			for (int i = 0; i < size; i++) {
				value.add(new BulkReply(markerList.get(i)));
			}
		}
	}
}