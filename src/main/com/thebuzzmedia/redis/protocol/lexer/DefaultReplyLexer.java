package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.reply.IReply;
import com.thebuzzmedia.redis.util.ArrayUtils;

public class DefaultReplyLexer implements IReplyLexer {
	public State scan(int index, int length, byte[] data,
			List<IMarker> markerList) throws MalformedReplyException {
		State state = State.INCOMPLETE;

		// Start scanning from the given index for completed replies to mark
		for (; index < length; index++) {
			byte b = data[index];
			IMarker mark = null;

			/*
			 * On the initial execution of loop we are always able to treat 'b'
			 * as a reply-prefix marker because the lexer is only invoked on
			 * full-token boundaries; always starting +1 position beyond the end
			 * of the last token returned in the markerList.
			 */
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

			/*
			 * Bulk replies are 2-lined; 1 line with the byte-length of the
			 * reply and the 2nd line of the actual bytes.
			 */
			case Constants.REPLY_TYPE_BULK:
				state = State.INCOMPLETE;
				mark = markBulkReply(index, data);
				break;

			/*
			 * Multi-bulk replies are any number of lines; the 1st line is the
			 * count of "Bulk replies" to follow, parsing all remaining data is
			 * just doing markBulkReply over and over.
			 */
			case Constants.REPLY_TYPE_MULTI_BULK:
				state = State.INCOMPLETE;
				mark = markMultiBulkReply(index, data);
				break;
			}

			// If we successfully marked data, process it.
			if (mark != null) {
				markerList.add(mark);

				/*
				 * Update the index to point at the last index that was included
				 * in the mark; after we cycle back up to the for-loop it will
				 * increment the index 1 more, pointing at the first char after
				 * our mark.
				 */
				index += mark.getLength() - 1;

				// Update lexer state to indicate we are not mid-reply
				state = State.COMPLETE;
			}
		}

		return state;
	}

	/**
	 * Convenience method used to create an {@link IMarker} representing the
	 * argument values passed in ONLY if the following conditions are met:
	 * <ol>
	 * <li><code>index</code> is &gt;= <code>0</code></li>
	 * <li><code>source</code> is not <code>null</code></li>
	 * <li><code>(index + length)</code> is &lt;= <code>source.length</code></li>
	 * <li><code>replyType</code> is a valid reply type defined in the
	 * {@link Constants} class.</li>
	 * </ol>
	 * If any of those conditions are <code>false</code>, then <code>null</code>
	 * is returned.
	 * <p/>
	 * This helps keep more complex bounds-checking code out of the individual
	 * <code>markXXX</code> methods.
	 * <p/>
	 * This also helps avoid creating invalid {@link DefaultMarker} instances
	 * which later result in {@link IllegalArgumentException}s when trying to
	 * generate valid {@link IReply} instances from them where an IMark was
	 * created with an index and length longer than it's source (at the time of
	 * creation).
	 * 
	 * @param type
	 *            The type of the reply.
	 * @param index
	 *            The index of <code>source</code> where the mark will start.
	 * @param length
	 *            The length of the mark.
	 * @param source
	 *            The <code>byte[]</code> source that the mark was generated
	 *            from.
	 * 
	 * @return an {@link IMarker} representing the arguments passed in or
	 *         <code>null</code> if there was something wrong with any fo the
	 *         arguments as described above.
	 */
	protected IMarker safelyCreateMark(byte type, int index, int length,
			byte[] source) {
		IMarker mark = null;

		if (index >= 0 && source != null && (index + length) <= source.length
				&& Constants.isValidType(type))
			mark = new DefaultMarker(type, index, length, source);

		return mark;
	}

	protected IMarker markSimpleReply(byte type, int index, byte[] data) {
		IMarker mark = null;

		// First, find the terminating CRLF
		int crIndex = ArrayUtils.indexOfCRLF(index, data.length - index, data);

		// Only create a mark if we found the terminating CRLF
		if (crIndex != Constants.UNDEFINED) {
			mark = safelyCreateMark(type, index, (crIndex + 2) - index, data);
		}

		return mark;
	}

	protected IMarker markBulkReply(int index, byte[] data)
			throws MalformedReplyException {
		IMarker mark = null;

		// First, find the length-terminating CRLF
		int crIndex = ArrayUtils.indexOfCRLF(index, data.length - index, data);

		/*
		 * If we found the \r\n after the length argument, we need to parse the
		 * given length so we know how many bytes this reply is.
		 */
		if (crIndex != Constants.UNDEFINED) {
			int length = Constants.UNDEFINED;

			try {
				length = ArrayUtils.parseInteger(index + 1, crIndex
						- (index + 1), data);
			} catch (Exception e) {
				throw new MalformedReplyException(
						"Unable to parse the Bulk reply byte length as an integer, the value '"
								+ new String(data, index + 1, crIndex
										- (index + 1)) + "' is malformed.");
			}

			/*
			 * TODO: Need to check if replies of length 0 return $0\r\n\r\n or
			 * just $0\r\n like the -1 'nil' reply does.
			 */

			/*
			 * If length was parsed as -1, that is Redis's "nil" reply, so there
			 * is no body. Otherwise we mark the payload portion of the
			 * BulkReply.
			 */
			if (length == Constants.UNDEFINED) {
				/*
				 * We mark the bytes that make up the standard NIL reply, in
				 * BulkReply we check for the standard reply pattern and convert
				 * it to a NIL reply when we see it.
				 */
				mark = safelyCreateMark(Constants.REPLY_TYPE_BULK, index,
						crIndex + 2 - index, data);
			} else {
				/*
				 * Mark the payload portion of the reply. It's position starts 2
				 * after the terminating \r\n after the length and runs for the
				 * length reported to us by Redis plus 2 more for the
				 * terminating \r\n.
				 */
				mark = safelyCreateMark(Constants.REPLY_TYPE_BULK, crIndex + 2,
						length + 2, data);
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
				mark = safelyCreateMark(Constants.REPLY_TYPE_MULTI_BULK, index,
						crIndex + 2 - index, data);
			} else {
				/*
				 * Create the parent MultiBuilk reply that we will add child
				 * BulkReply marks to as we find them.
				 * 
				 * As we add child marks the parent's length property will be
				 * expanded to encompass them.
				 */
				mark = safelyCreateMark(Constants.REPLY_TYPE_MULTI_BULK, index,
						0, data);

				/*
				 * Update the index to point at the first byte after the
				 * terminating \r\n for the bulk count.
				 */
				index = crIndex + 2;

				/*
				 * Parse as many BulkReplies as the bulk count told us this
				 * MultiBulk had. If we are unable to parse a BulkReply (get
				 * null) that means the bytes required to represent this reply
				 * completely are not receiving completely from the server, so
				 * return to the connection and wait for more bytes and we'll
				 * try and mark the entire MultiBulk in a future call to the
				 * lexer.
				 */
				for (int i = 0; mark != null && i < bulkCount; i++) {
					IMarker bulkMark = markBulkReply(index, data);

					if (bulkMark == null) {
						/*
						 * This multi-bulk is incomplete. We just tried to parse
						 * a BulkReply and failed, so we need more bytes to
						 * represent the full MultiBulkReply and cannot return a
						 * valid mark for it yet.
						 */
						mark = null;

						/*
						 * TODO: need to re-consider the performance
						 * implications of this as marking a HUGE multi replies,
						 * this would be a performance killer, constantly
						 * remarking over and over.
						 * 
						 * TODO: This is probably totally unnecessary as
						 * partially marked should be fine (as long as we can
						 * remember the state and what we were marking) --
						 * because DefaultMarker's length grows based on the
						 * children it contains, so technically when we return
						 * from here and call back in later, mark.getLength + 1
						 * should point us right at the start of the next Bulk
						 * reply that is part of this multi-bulk.
						 * 
						 * We need a way to return the original mulit-bulk count
						 * though so we can pickup where we left off.
						 */
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