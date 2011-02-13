package com.thebuzzmedia.redis.reply;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.DefaultMarker;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public class MultiBulkReplyTest extends AbstractReplyTest {
	// static IMarker EMPTY = "";
	// static IMarker WHITESPACE = "   ";
	// static IMarker INVALID = "@";
	// static IMarker NO_COUNT = "*";
	// static IMarker NO_COUNT_CRLF = "*1";
	// static IMarker NO_CONTENT = "*2\r\n";
	// static IMarker COUNT_TOO_BIG = "*4\r\n$3\r\nfoo\r\n";
	// static IMarker INCOMPLETE = "*1\r\n$5\r\nhello";

	static IMarker EMPTY_LIST = createMark(Constants.REPLY_TYPE_MULTI_BULK,
			"*0\r\n");
	static IMarker TIMEOUT = createMark(Constants.REPLY_TYPE_MULTI_BULK,
			"*-1\r\n");

	static String WORD = "OK";
	static String SPACE = "hello world";
	static String SPACES = "why hello there yourself";
	static String NEWLINE = "that\nis\ninteresting";
	static String CRLF_ESCAPED = "enjoy\\r\\nlife";

	static IMarker R_WORD = createMultiBulkSingleChild("*1\r\n$"
			+ WORD.length() + "\r\n" + WORD + "\r\n");
	static IMarker R_SPACE = createMultiBulkSingleChild("*1\r\n$"
			+ SPACE.length() + "\r\n" + SPACE + "\r\n");
	static IMarker R_SPACES = createMultiBulkSingleChild("*1\r\n$"
			+ SPACES.length() + "\r\n" + SPACES + "\r\n");
	static IMarker R_NEWLINE = createMultiBulkSingleChild("*1\r\n$"
			+ NEWLINE.length() + "\r\n" + NEWLINE + "\r\n");
	static IMarker R_CRLF_ESCAPED = createMultiBulkSingleChild("*1\r\n$"
			+ CRLF_ESCAPED.length() + "\r\n" + CRLF_ESCAPED + "\r\n");

	// static IMarker R_FULL = "*5\r\n" + BulkReplyTest.R_WORD
	// + BulkReplyTest.R_SPACE + BulkReplyTest.R_SPACES
	// + BulkReplyTest.R_NEWLINE + BulkReplyTest.R_CRLF_ESCAPED;
	static IMarker R_FULL = null;

	static {
		IMarker parent = new DefaultMarker(Constants.REPLY_TYPE_MULTI_BULK, 0,
				null);

		parent.addChildMarker(createMark(Constants.REPLY_TYPE_MULTI_BULK,
				"*5\r\n"));
		parent.addChildMarker(createBulkMarkParseLength("$" + WORD.length()
				+ "\r\n" + WORD + "\r\n"));
		parent.addChildMarker(createBulkMarkParseLength("$" + SPACE.length()
				+ "\r\n" + SPACE + "\r\n"));
		parent.addChildMarker(createBulkMarkParseLength("$" + SPACES.length()
				+ "\r\n" + SPACES + "\r\n"));
		parent.addChildMarker(createBulkMarkParseLength("$" + NEWLINE.length()
				+ "\r\n" + NEWLINE + "\r\n"));
		parent.addChildMarker(createBulkMarkParseLength("$"
				+ CRLF_ESCAPED.length() + "\r\n" + CRLF_ESCAPED + "\r\n"));

		R_FULL = parent;
	}

	@Test
	public void testNull() {
		try {
			new MultiBulkReply(null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	// @Test
	// public void testEmpty() {
	// try {
	// new MultiBulkReply(EMPTY.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testWhitespace() {
	// try {
	// new MultiBulkReply(WHITESPACE.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testInvalid() {
	// try {
	// new MultiBulkReply(INVALID.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testNoCount() {
	// try {
	// new MultiBulkReply(NO_COUNT.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testNoCountCRLF() {
	// try {
	// new MultiBulkReply(NO_COUNT_CRLF.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testNoContent() {
	// try {
	// new MultiBulkReply(NO_CONTENT.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testCountTooBig() {
	// try {
	// new MultiBulkReply(COUNT_TOO_BIG.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }
	//
	// @Test
	// public void testIncomplete() {
	// try {
	// new MultiBulkReply(INCOMPLETE.toCharArray());
	// assertTrue(false); // Shouldn't get here, FAIL
	// } catch (IllegalArgumentException e) {
	// assertNotNull(e);
	// }
	// }

	@Test
	public void testEmptyList() {
		try {
			MultiBulkReply r = new MultiBulkReply(EMPTY_LIST);
			assertTrue(r.isNil());
		} catch (IllegalArgumentException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testTimeout() {
		try {
			MultiBulkReply r = new MultiBulkReply(TIMEOUT);
			assertTrue(r.isNil());
		} catch (IllegalArgumentException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testWord() {
		doSingleTest(R_WORD, WORD);
	}

	@Test
	public void testSpace() {
		doSingleTest(R_SPACE, SPACE);
	}

	@Test
	public void testSpaces() {
		doSingleTest(R_SPACES, SPACES);
	}

	@Test
	public void testNewline() {
		doSingleTest(R_NEWLINE, NEWLINE);
	}

	@Test
	public void testCRLFEscaped() {
		doSingleTest(R_CRLF_ESCAPED, CRLF_ESCAPED);
	}

	@Test
	public void testFull() {
		IReply<List<BulkReply>> r = new MultiBulkReply(R_FULL);

		assertEquals(Constants.REPLY_TYPE_MULTI_BULK, r.getType());
		assertEquals(5, r.getSize());

		assertEquals(WORD.length(), r.getValue().get(0).getSize());
		assertArrayEquals(WORD.toCharArray(), r.getValue().get(0).getValue());

		assertEquals(SPACE.length(), r.getValue().get(1).getSize());
		assertArrayEquals(SPACE.toCharArray(), r.getValue().get(1).getValue());

		assertEquals(SPACES.length(), r.getValue().get(2).getSize());
		assertArrayEquals(SPACES.toCharArray(), r.getValue().get(2).getValue());

		assertEquals(NEWLINE.length(), r.getValue().get(3).getSize());
		assertArrayEquals(NEWLINE.toCharArray(), r.getValue().get(3).getValue());

		assertEquals(CRLF_ESCAPED.length(), r.getValue().get(4).getSize());
		assertArrayEquals(CRLF_ESCAPED.toCharArray(), r.getValue().get(4)
				.getValue());
	}

	void doSingleTest(IMarker marker, String expectedValue) {
		IReply<List<BulkReply>> r = new MultiBulkReply(marker);

		System.out.println(r);

		assertEquals(Constants.REPLY_TYPE_MULTI_BULK, r.getType());
		assertEquals(1, r.getSize());
		assertArrayEquals(expectedValue.toCharArray(), r.getValue().get(0)
				.getValue());
	}
}