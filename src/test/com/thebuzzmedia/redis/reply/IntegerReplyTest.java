package com.thebuzzmedia.redis.reply;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public class IntegerReplyTest extends AbstractReplyTest {
	static IMarker EMPTY = createMark(Constants.REPLY_TYPE_INTEGER, "");
	static IMarker WHITESPACE = createMark(Constants.REPLY_TYPE_INTEGER, "   ");
	static IMarker INVALID = createMark(Constants.REPLY_TYPE_INTEGER, "@");
	static IMarker NO_CONTENT = createMark(Constants.REPLY_TYPE_INTEGER, ":");
	static IMarker INCOMPLETE = createMark(Constants.REPLY_TYPE_INTEGER, ":1");

	static IMarker N_0 = createMark(Constants.REPLY_TYPE_INTEGER, ":0\r\n");

	static IMarker N_5 = createMark(Constants.REPLY_TYPE_INTEGER, ":5\r\n");
	static IMarker N_42 = createMark(Constants.REPLY_TYPE_INTEGER, ":42\r\n");
	static IMarker N_331 = createMark(Constants.REPLY_TYPE_INTEGER, ":331\r\n");
	static IMarker N_831742 = createMark(Constants.REPLY_TYPE_INTEGER,
			":831742\r\n");

	static IMarker N_NEG_1 = createMark(Constants.REPLY_TYPE_INTEGER, ":-1\r\n");
	static IMarker N_NEG_33 = createMark(Constants.REPLY_TYPE_INTEGER,
			":-33\r\n");
	static IMarker N_NEG_777 = createMark(Constants.REPLY_TYPE_INTEGER,
			":-777\r\n");
	static IMarker N_NEG_71381 = createMark(Constants.REPLY_TYPE_INTEGER,
			":-71381\r\n");

	@Test
	public void testNull() {
		try {
			new IntegerReply(null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testEmpty() {
		try {
			new IntegerReply(EMPTY);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testWhitespace() {
		try {
			new IntegerReply(WHITESPACE);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testInvalid() {
		try {
			new IntegerReply(INVALID);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testNoContent() {
		try {
			new IntegerReply(NO_CONTENT);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testIncomplete() {
		try {
			new IntegerReply(INCOMPLETE);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testZero() {
		doTest(N_0, 0);
	}

	@Test
	public void testPositives() {
		doTest(N_5, 5);
		doTest(N_42, 42);
		doTest(N_331, 331);
		doTest(N_831742, 831742);
	}

	@Test
	public void testNegatives() {
		doTest(N_NEG_1, -1);
		doTest(N_NEG_33, -33);
		doTest(N_NEG_777, -777);
		doTest(N_NEG_71381, -71381);
	}

	void doTest(IMarker mark, long expectedValue) {
		IReply<Integer> r = new IntegerReply(mark);

		assertEquals(Constants.REPLY_TYPE_INTEGER, r.getType());
		assertEquals(expectedValue, r.getSize());
		assertEquals(expectedValue, (long) r.getValue());
	}
}