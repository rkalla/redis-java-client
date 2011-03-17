package com.thebuzzmedia.redis.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.buffer.DynamicByteArray;
import com.thebuzzmedia.redis.buffer.DynamicCharArray;
import com.thebuzzmedia.redis.buffer.IArraySource;
import com.thebuzzmedia.redis.buffer.IDynamicArray;

public class CodingUtils {
	public static final int MAX_ENCODE_BUFFER_SIZE = Integer.getInteger(
			"redis.util.maxEncodeBufferSize", 2048);
	public static final int MAX_DECODE_BUFFER_SIZE = Integer.getInteger(
			"redis.util.maxDecodeBufferSize", 2048);

	public static IArraySource<byte[]> encode(char[] chars) {
		return (chars == null ? null : encode(CharBuffer.wrap(chars)));
	}

	public static IArraySource<byte[]> encode(int index, int length,
			char[] chars) {
		return (chars == null ? null : encode(CharBuffer.wrap(chars, index,
				length)));
	}

	public static IArraySource<byte[]> encode(CharSequence chars) {
		return (chars == null ? null : encode(CharBuffer.wrap(chars)));
	}

	public static IArraySource<byte[]> encode(CharBuffer in) {
		IDynamicArray<byte[], ByteBuffer> result = null;

		if (in != null) {
			CharsetEncoder encoder = Constants.getEncoder();

			int size = Math.round(encoder.averageBytesPerChar()
					* (float) in.remaining());

			/*
			 * To be as memory-sensitive as possible, we attempt to calculate
			 * the exact size of bytes required to encode these chars using the
			 * given encoding and then allocate a buffer that is the smallest
			 * between that required size and the max buffer size.
			 */
			ByteBuffer buffer = ByteBuffer
					.allocate(size < MAX_ENCODE_BUFFER_SIZE ? size
							: MAX_ENCODE_BUFFER_SIZE);
			result = new DynamicByteArray();

			// Reset the encoder
			encoder.reset();

			while (in.hasRemaining()) {
				/*
				 * Encode the first buffer.capacity chars, passing 'false' to
				 * indicate that we aren't sure if we are done with the encode
				 * operation yet.
				 */
				encoder.encode(in, buffer, false);

				// Append what we successfully encoded to our tally
				buffer.flip();
				result.append(buffer);

				// If there is no more to encode, go through finalization
				if (!in.hasRemaining()) {
					buffer.clear();

					/*
					 * Per the CharsetEncoder Javadocs, encoders must be given
					 * an opportunity to "finalize" their internal state and
					 * flush out any pending operations once we know we've hit
					 * the end of the bytes to encode.
					 */
					encoder.encode(in, buffer, true);
					encoder.flush(buffer);

					buffer.flip();

					// If any finalized bytes were written, append them.
					if (buffer.hasRemaining()) {
						result.append(buffer);
					}
				}
			}
		}

		return result;
	}

	public static IArraySource<char[]> decode(byte[] bytes) {
		return (bytes == null ? null : decode(0, bytes.length, bytes));
	}

	public static IArraySource<char[]> decode(int index, int length,
			byte[] bytes) {
		return (bytes == null ? null : decode(ByteBuffer.wrap(bytes, index,
				length)));
	}

	public static IArraySource<char[]> decode(ByteBuffer in) {
		IDynamicArray<char[], CharBuffer> result = null;

		if (in != null) {
			CharsetDecoder decoder = Constants.getDecoder();

			/*
			 * To be as memory-sensitive as possible, we allocate a buffer that
			 * is the smallest between the max required size to hold all the
			 * decoded bytes or the max buffer size.
			 */
			CharBuffer buffer = CharBuffer
					.allocate(in.remaining() < MAX_DECODE_BUFFER_SIZE ? in
							.remaining() : MAX_DECODE_BUFFER_SIZE);
			result = new DynamicCharArray();

			// Reset the decoder
			decoder.reset();

			while (in.hasRemaining()) {
				/*
				 * Decode the first buffer.capacity chars, passing 'false' to
				 * indicate that we aren't sure if we are done with the decode
				 * operation yet.
				 */
				decoder.decode(in, buffer, false);

				// Append what we successfully decoded to our tally
				buffer.flip();
				result.append(buffer);

				// If there is no more to decode, go through finalization
				if (!in.hasRemaining()) {
					buffer.clear();

					/*
					 * Per the CharsetDecoder Javadocs, decoders must be given
					 * an opportunity to "finalize" their internal state and
					 * flush out any pending operations once we know we've hit
					 * the end of the chars to decode.
					 */
					decoder.decode(in, buffer, true);
					decoder.flush(buffer);

					buffer.flip();

					// If any finalized bytes were written, append them.
					if (buffer.hasRemaining()) {
						result.append(buffer);
					}
				}
			}
		}

		return result;
	}
}