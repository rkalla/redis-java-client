package com.thebuzzmedia.redis.reply;

import static org.junit.Assert.*;

import org.junit.Test;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public class BulkReplyTest extends AbstractReplyTest {
	static IMarker EMPTY = createMark(Constants.REPLY_TYPE_BULK, "");
	static IMarker INVALID = createMark(Constants.REPLY_TYPE_BULK, "@");
	static IMarker NO_SIZE = createMark(Constants.REPLY_TYPE_BULK, "$");
	static IMarker NO_CRLF = createMark(Constants.REPLY_TYPE_BULK, "$1");
	static IMarker SIZE_TOO_SHORT = createBulkMarkParseLength("$10\r\nincompl");
	static IMarker SIZE_TOO_LONG = createBulkMarkParseLength("$3\r\nsmall");
	static IMarker INCOMPLETE = createBulkMark("$9\r\nnoendcrlf");

	static String WORD = "OK";
	static String SPACE = "hello world";
	static String SPACES = "why hello there yourself";
	static String NEWLINE = "that\nis\ninteresting";
	static String CRLF_ESCAPED = "enjoy\\r\\nlife";

	static IMarker R_WORD = createBulkMark("$" + WORD.length() + "\r\n" + WORD
			+ "\r\n");
	static IMarker R_SPACE = createBulkMark("$" + SPACE.length() + "\r\n"
			+ SPACE + "\r\n");
	static IMarker R_SPACES = createBulkMark("$" + SPACES.length() + "\r\n"
			+ SPACES + "\r\n");
	static IMarker R_NEWLINE = createBulkMark("$" + NEWLINE.length() + "\r\n"
			+ NEWLINE + "\r\n");
	static IMarker R_CRLF_ESCAPED = createBulkMark("$" + CRLF_ESCAPED.length()
			+ "\r\n" + CRLF_ESCAPED + "\r\n");

	@Test
	public void testNull() {
		try {
			new BulkReply(null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testEmpty() {
		try {
			new BulkReply(EMPTY);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testInvalid() {
		try {
			new BulkReply(INVALID);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testNoSize() {
		try {
			new BulkReply(NO_SIZE);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testNoCRLF() {
		try {
			new BulkReply(NO_CRLF);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testSizeTooShort() {
		try {
			new BulkReply(SIZE_TOO_SHORT);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testSizeTooLong() {
		try {
			BulkReply b = new BulkReply(SIZE_TOO_LONG);
			assertTrue(true);
			assertFalse(b.isNil());
		} catch (IllegalArgumentException e) {
			assertTrue(false); // It's ok, a message should be limited to it's
								// length value
		}
	}

	@Test
	public void testIncomplete() {
		try {
			BulkReply b = new BulkReply(INCOMPLETE);
			// SHOULD complete, because we just use length param from reply and
			// don't check for ending CRLF.
			assertTrue(true);
			assertFalse(b.isNil());
		} catch (IllegalArgumentException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testWord() {
		doTest(R_WORD, WORD);
	}

	@Test
	public void testSpace() {
		doTest(R_SPACE, SPACE);
	}

	@Test
	public void testSpaces() {
		doTest(R_SPACES, SPACES);
	}

	@Test
	public void testNewline() {
		doTest(R_NEWLINE, NEWLINE);
	}

	@Test
	public void testCRLFEscaped() {
		doTest(R_CRLF_ESCAPED, CRLF_ESCAPED);
	}

	void doTest(IMarker mark, String expectedValue) {
		IReply<char[]> r = new BulkReply(mark);

		assertEquals(Constants.REPLY_TYPE_BULK, r.getType());
		assertEquals(expectedValue.length(), r.getSize());
		assertArrayEquals(expectedValue.toCharArray(), r.getValue());
	}
}