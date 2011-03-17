package com.thebuzzmedia.redis.reply;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.buffer.DynamicCharArray;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

/*
 * TODO: This class should be able to represent a byte[] reply as well... maybe
 * the default return type is byte[] and there is a getValueAsChar method or something.
 * 
 * Need to think about it.
 */
public class BulkReply implements IReply<char[]> {
	public static final byte MIN_BYTE_LENGTH = 3;
	public static final int MAX_BUFFER_SIZE = Integer.getInteger(
			"redis.reply.bulkMaxBufferSize", 4096);

	private byte type = Constants.UNDEFINED;
	private char[] value;

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
				+ getSize() + ", value="
				+ (value == null ? "" : new String(getValue())) + "]";
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
		byte[] source = marker.getSource();

		/*
		 * Check for a NIL marker. This is a special case that always has the
		 * same properties and is fast/easy to check for: length of 5 ($-1\r\n)
		 * and the bytes at position 1 and 2 are "-" and "1" respectively.
		 */
		if (marker.getLength() == 5 && source[1] == 45 && source[2] == 49)
			return null;
		else {
			// Wrap the portion of bytes that make up this reply's text
			ByteBuffer src = ByteBuffer.wrap(source, marker.getIndex(),
					marker.getLength() - 2);

			/*
			 * Create an optimally sized destination buffer UP TO our max buffer
			 * size. Decoding bytes to chars will never result in more chars
			 * than bytes, so we can at least use the same sized buffer if it's
			 * smaller than our max buffer size.
			 */
			CharBuffer dest = CharBuffer
					.allocate(src.remaining() < MAX_BUFFER_SIZE ? src
							.remaining() : MAX_BUFFER_SIZE);
			DynamicCharArray result = new DynamicCharArray();
			CharsetDecoder decoder = Constants.getDecoder();

			// Reset the decoder.
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
					 * Per the CharsetEncoder/Decoder class Javadoc, we must now
					 * give the decoder an opportunity to finalize and flush its
					 * decoded data.
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