package com.thebuzzmedia.redis;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class Constants {
	public static final int UNDEFINED = -1;

	public static final byte REPLY_TYPE_INTEGER = 58; // :
	public static final byte REPLY_TYPE_SINGLE_LINE = 43; // +
	public static final byte REPLY_TYPE_BULK = 36; // $
	public static final byte REPLY_TYPE_MULTI_BULK = 42; // *
	public static final byte REPLY_TYPE_ERROR = 45; // -

	public static final byte CR = 13; // \r
	public static final byte LF = 10; // \n

	public static final char[] CRLF_CHARS = new char[] { '\r', '\n' };

	public static boolean isValidType(byte type) {
		boolean valid = false;

		switch (type) {
		case REPLY_TYPE_INTEGER:
		case REPLY_TYPE_SINGLE_LINE:
		case REPLY_TYPE_BULK:
		case REPLY_TYPE_MULTI_BULK:
		case REPLY_TYPE_ERROR:
			valid = true;
			break;
		}

		return valid;
	}

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	private static final ThreadLocal<CharsetEncoder> THREAD_ENCODER = new ThreadLocal<CharsetEncoder>() {
		@Override
		protected CharsetEncoder initialValue() {
			return UTF8_CHARSET.newEncoder();
		}
	};

	private static final ThreadLocal<CharsetDecoder> THREAD_DECODER = new ThreadLocal<CharsetDecoder>() {
		@Override
		protected CharsetDecoder initialValue() {
			return UTF8_CHARSET.newDecoder();
		}
	};

	public static CharsetEncoder getEncoder() {
		return THREAD_ENCODER.get();
	}

	public static CharsetDecoder getDecoder() {
		return THREAD_DECODER.get();
	}
}