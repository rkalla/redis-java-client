package com.thebuzzmedia.redis.reply;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;
import com.thebuzzmedia.redis.util.StrictDynamicCharArray;

public class BulkReply implements IReply<char[]> {
	public static final byte MIN_BYTE_LENGTH = 5;

	private byte type = Constants.UNDEFINED;
	private char[] value;

	public BulkReply(IMarker marker) throws IllegalArgumentException {
		if (marker == null)
			throw new IllegalArgumentException("marker cannot be null");
		if (marker.getReplyType() != Constants.REPLY_TYPE_BULK)
			throw new IllegalArgumentException(
					"marker.getReplyType must be equal to Constants.REPLY_TYPE_BULK in order to create a BulkReply from this marker.");
		if (marker.getIndex() < 0 || marker.getLength() < MIN_BYTE_LENGTH)
			throw new IllegalArgumentException("marker index ["
					+ marker.getIndex() + "] and length [" + marker.getLength()
					+ "] do not mark the bounds of a valid Bulk reply.");

		this.type = marker.getReplyType();

		try {
			this.value = parseMarker(marker);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Unable to parse the given marker as a valid Bulk reply [marker="
							+ marker + "]", e);
		}
	}

	@Override
	public String toString() {
		return BulkReply.class.getName() + "[isNil=" + isNil() + ", size="
				+ getSize() + ", value=" + new String(getValue()) + "]";
	}

	@Override
	public byte getType() {
		return type;
	}

	@Override
	public int getSize() {
		return (value == null ? Constants.UNDEFINED : value.length);
	}

	@Override
	public char[] getValue() {
		return value;
	}

	public boolean isNil() {
		return (value == null);
	}

	protected char[] parseMarker(IMarker marker) {
		/*
		 * First we check if this marker represents a nil reply (has no
		 * children). If it has children then it is a valid bulk reply and we
		 * should process it as such.
		 */
		if (!marker.hasChildren())
			return null;
		else {
			// With our lexing logic, the 2nd child represents the payload
			IMarker payloadMarker = marker.getChildMarkerList().get(1);

			// Wrap the portion of bytes that make up this reply's text
			ByteBuffer src = ByteBuffer.wrap(marker.getSource(),
					payloadMarker.getIndex(), payloadMarker.getLength() - 2);

			// Bulk replies can be large, be generous with the CharBuffer size
			CharBuffer dest = CharBuffer.allocate(8192);
			StrictDynamicCharArray result = new StrictDynamicCharArray();

			CharsetDecoder decoder = Constants.getDecoder();
			decoder.reset();

			/*
			 * Keep reading (in CharBuffer sized chunks) and decoding bytes from
			 * our source and into our dest as long as there are bytes to be
			 * read.
			 */
			while (src.hasRemaining()) {
				/*
				 * Decode read bytes; the 'false' tells the decoder that we are
				 * not positively done yet, so if it reaches a char it cannot
				 * decode, it will hold on to it until it gets more bytes.
				 */
				decoder.decode(src, dest, false);

				// Write the decoded chars to our result array.
				dest.flip();
				result.append(dest);

				// Prepare for another read
				dest.clear();

				/*
				 * If the source buffer has no more bytes remaining to be read,
				 * then that means we are done.
				 */
				if (!src.hasRemaining()) {
					/*
					 * Per the CharsetEn/Decoder classes, we must now give the
					 * decoder an opportunity to finalize and flush its decoded
					 * data.
					 */
					decoder.decode(src, dest, true);
					decoder.flush(dest);

					// Add any last chars flushed by the decoder to our result
					if (dest.position() > 0) {
						dest.flip();

						result.append(dest);
					}
				}
			}

			return result.getArray();
		}
	}
}