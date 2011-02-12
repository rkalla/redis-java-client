package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.MalformedReplyException;
import com.thebuzzmedia.redis.util.ArrayUtils;

public class DefaultReplyLexer implements IReplyLexer {
	@Override
	public State scan(int index, byte[] data, List<IMarker> markerList)
			throws MalformedReplyException {
		State state = State.INCOMPLETE;

		// Start scanning from the given index for completed replies to mark
		for (; index < data.length; index++) {
			byte b = data[index];
			IMarker mark = null;

			switch (b) {
			/*
			 * Integer, Single Line and Error reply types are all single-line,
			 * CRLF-terminated replies. They all get marked using the same
			 * logic.
			 */
			case Constants.REPLY_TYPE_INTEGER:
			case Constants.REPLY_TYPE_SINGLE_LINE:
			case Constants.REPLY_TYPE_ERROR:
				state = State.INCOMPLETE;
				mark = markSimpleReply(b, index, data);
				break;

			case Constants.REPLY_TYPE_BULK:
				state = State.INCOMPLETE;
				mark = markBulkReply(index, data);
				break;

			case Constants.REPLY_TYPE_MULTI_BULK:
				state = State.INCOMPLETE;
				mark = markMultiBulkReply(index, data);
				break;
			}

			// If we successfully marked data, process it.
			if (mark != null) {
				markerList.add(mark);

				// Update the index to point beyond the marked data
				index += mark.getLength();

				// Update lexer state to indicate we are not mid-reply
				state = State.COMPLETE;
			}
		}

		return state;
	}

	protected IMarker markSimpleReply(byte type, int index, byte[] data) {
		IMarker mark = null;

		// First, find the terminating CRLF
		int crIndex = ArrayUtils.indexOfCRLF(index, data.length - index, data);

		// Only create a mark if we found the terminating CRLF
		if (crIndex != Constants.UNDEFINED)
			mark = new DefaultMarker(type, index, (crIndex + 2) - index, data);

		return mark;
	}

	protected IMarker markBulkReply(int index, byte[] data)
			throws MalformedReplyException {
		IMarker mark = null;

		// First, find the length-terminating CRLF
		int crIndex = ArrayUtils.indexOfCRLF(index, data.length - index, data);

		/*
		 * If we found the \r\n after the length argument, we need to parse the
		 * given length so we know how long this reply is.
		 */
		if (crIndex != Constants.UNDEFINED) {
			int length = Constants.UNDEFINED;

			try {
				length = ArrayUtils.parseInteger(index + 1, crIndex
						- (index + 1), data);
			} catch (Exception e) {
				throw new MalformedReplyException(
						"Unable to parse the Bulk reply length as an integer, the value '"
								+ new String(data, index + 1, crIndex
										- (index + 1)) + "' is malformed.");
			}

			/*
			 * TODO: Need to check if replies of length 0 return $0\r\n\r\n or
			 * just $0\r\n like the -1 'nil' reply does.
			 */

			/*
			 * If length was parsed as -1, that is Redis's "nil" reply, so there
			 * is no body. Otherwise we add the reply length and 4 (for
			 * \n...\r\n) to the current index and subtract our starting index
			 * for the length.
			 */
			if (length == Constants.UNDEFINED) {
				// Represent a nil bulk reply as a no-child mark
				mark = new DefaultMarker(Constants.REPLY_TYPE_BULK, index,
						(crIndex + 2) - index, data);
			} else {
				int payloadStartIndex = crIndex + 2;

				// Create the parent mark for this 2-part bulk reply
				mark = new DefaultMarker(Constants.REPLY_TYPE_BULK, index, data);

				// Add a child marking the length portion
				mark.addChildMarker(new DefaultMarker(
						Constants.REPLY_TYPE_BULK, index, payloadStartIndex
								- index, data));

				// Add a child marking the payload portion
				mark.addChildMarker(new DefaultMarker(
						Constants.REPLY_TYPE_BULK, payloadStartIndex,
						length + 2, data));
			}
		}

		return mark;
	}

	protected IMarker markMultiBulkReply(int index, byte[] data)
			throws MalformedReplyException {
		IMarker mark = null;

		// First, find the bulk count-terminating CRLF
		int crIndex = ArrayUtils.indexOfCRLF(index, data.length - index, data);

		/*
		 * If we found the \r\n after the bulk count argument, we need to parse
		 * the given count so we know how many bulk replies are in here.
		 */
		if (crIndex != Constants.UNDEFINED) {
			int bulkCount = Constants.UNDEFINED;

			try {
				bulkCount = ArrayUtils.parseInteger(index + 1, crIndex
						- (index + 1), data);
			} catch (Exception e) {
				throw new MalformedReplyException(
						"Unable to parse the MultiBulk reply bulk count as an integer, the value '"
								+ new String(data, index + 1, crIndex
										- (index + 1)) + "' is malformed.");
			}

			/*
			 * If the bulkCount is -1 (e.g. BLPOP timed out) or 0 (empty list)
			 * then we only have a single mark covering the empty reply,
			 * otherwise we need to mark the given number of bulk replies.
			 */
			if (bulkCount == Constants.UNDEFINED || bulkCount == 0) {
				// Represent a nil multi-bulk reply as a no-child Mark
				mark = new DefaultMarker(Constants.REPLY_TYPE_MULTI_BULK,
						index, (crIndex + 2) - index, data);
			} else {
				/*
				 * Create the parent MultiBuild reply that we will add child
				 * marks to as we find them.
				 */
				mark = new DefaultMarker(Constants.REPLY_TYPE_MULTI_BULK,
						index, data);

				// Add the first child mark denoting the length argument
				mark.addChildMarker(new DefaultMarker(
						Constants.REPLY_TYPE_MULTI_BULK, index, (crIndex + 2)
								- index, data));

				// Update the index to point at the first byte after the \r\n
				index = crIndex + 2;

				/*
				 * Keep parsing the bulk replies the multi-bulk told us it had
				 * until we get them all or we encounter an early EOF and cancel
				 * marking the entire multi-bulk.
				 */
				for (int i = 0; mark != null && i < bulkCount; i++) {
					IMarker bulkMark = markBulkReply(index, data);

					if (bulkMark == null) {
						/*
						 * This multi-bulk is incomplete, so we cannot return a
						 * valid mark for it.
						 */
						mark = null;
					} else {
						// Add the child mark to the parent
						mark.addChildMarker(bulkMark);

						// Update the index to look at the next bulk reply
						index += bulkMark.getLength();
					}
				}
			}
		}

		return mark;
	}
}