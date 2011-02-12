package com.thebuzzmedia.redis.reply;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public class SingleLineReplyTest extends AbstractReplyTest {
	static IMarker EMPTY = createMark(Constants.REPLY_TYPE_SINGLE_LINE, "");
	static IMarker WHITESPACE = createMark(Constants.REPLY_TYPE_SINGLE_LINE,
			"   ");
	static IMarker INVALID = createMark(Constants.REPLY_TYPE_SINGLE_LINE, "@");
	static IMarker NO_CONTENT = createMark(Constants.REPLY_TYPE_SINGLE_LINE,
			"+");
	static IMarker INCOMPLETE = createMark(Constants.REPLY_TYPE_SINGLE_LINE,
			"+h");

	static String WORD = "OK";
	static String SPACE = "hello world";
	static String SPACES = "why hello there yourself";
	static String NEWLINE = "that\nis\ninteresting";
	static String CRLF_ESCAPED = "enjoy\\r\\nlife";

	static IMarker T_WORD = createMark(Constants.REPLY_TYPE_SINGLE_LINE, "+"
			+ WORD + "\r\n");
	static IMarker T_SPACE = createMark(Constants.REPLY_TYPE_SINGLE_LINE, "+"
			+ SPACE + "\r\n");
	static IMarker T_SPACES = createMark(Constants.REPLY_TYPE_SINGLE_LINE, "+"
			+ SPACES + "\r\n");
	static IMarker T_NEWLINE = createMark(Constants.REPLY_TYPE_SINGLE_LINE, "+"
			+ NEWLINE + "\r\n");
	static IMarker T_CRLF_ESCAPED = createMark(
			Constants.REPLY_TYPE_SINGLE_LINE, "+" + CRLF_ESCAPED + "\r\n");

	@Test
	public void testNull() {
		try {
			new SingleLineReply(null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testEmpty() {
		try {
			new SingleLineReply(EMPTY);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testWhitespace() {
		try {
			new SingleLineReply(WHITESPACE);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			assertTrue(false); // Shouldn't get here, empty is OK
		}
	}

	@Test
	public void testInvalid() {
		try {
			new SingleLineReply(INVALID);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testNoContent() {
		try {
			new SingleLineReply(NO_CONTENT);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testIncomplete() {
		try {
			new SingleLineReply(INCOMPLETE);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testWord() {
		doTest(T_WORD, WORD);
	}

	@Test
	public void testSpace() {
		doTest(T_SPACE, SPACE);
	}

	@Test
	public void testSpaces() {
		doTest(T_SPACES, SPACES);
	}

	@Test
	public void testNewline() {
		doTest(T_NEWLINE, NEWLINE);
	}

	@Test
	public void testCRLFEscaped() {
		doTest(T_CRLF_ESCAPED, CRLF_ESCAPED);
	}

	void doTest(IMarker mark, String expectedValue) {
		IReply<char[]> r = new SingleLineReply(mark);

		assertEquals(Constants.REPLY_TYPE_SINGLE_LINE, r.getType());
		assertEquals(expectedValue.length(), r.getSize());
		assertArrayEquals(expectedValue.toCharArray(), r.getValue());
	}
}