package com.thebuzzmedia.redis.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArrayUtilsTest {
	private String EMPTY = "";
	private String WHITESPACE = "  ";

	private String N_0 = "0";
	private String TEXT = "$ext:";

	private String T_0 = TEXT + N_0 + TEXT;

	private String N_1 = "1";
	private String N_27 = "27";
	private String N_417 = "417";
	private String N_7483 = "7483";
	private String N_71381 = "71381";
	private String N_98317 = "98317";
	private String N_1234567 = "1234567";

	private String T_1 = TEXT + N_1 + TEXT;
	private String T_71381 = TEXT + N_71381 + TEXT;

	private String N_NEG_1 = '-' + N_1;
	private String N_NEG_27 = '-' + N_27;
	private String N_NEG_417 = '-' + N_417;
	private String N_NEG_7483 = '-' + N_7483;
	private String N_NEG_71381 = '-' + N_71381;
	private String N_NEG_98317 = '-' + N_98317;
	private String N_NEG_1234567 = '-' + N_1234567;

	private String T_NEG_1 = TEXT + N_NEG_1 + TEXT;
	private String T_NEG_71381 = TEXT + N_NEG_71381 + TEXT;

	@Test
	public void testParseIntegerNull() {
		try {
			ArrayUtils.parseInteger(0, 0, null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(0, 1, null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(1, 0, null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(1, 2, null);
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testParseIntegerEmpty() {
		try {
			ArrayUtils.parseInteger(0, 0, EMPTY.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(0, 1, EMPTY.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(1, 0, EMPTY.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(1, 2, EMPTY.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testParseIntegerWhitespace() {
		try {
			ArrayUtils.parseInteger(0, 0, WHITESPACE.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(0, 1, WHITESPACE.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (NumberFormatException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(1, 0, WHITESPACE.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		try {
			ArrayUtils.parseInteger(1, WHITESPACE.length() - 1,
					WHITESPACE.getBytes());
			assertTrue(false); // Shouldn't get here, FAIL
		} catch (NumberFormatException e) {
			assertNotNull(e);
		}
	}

	@Test
	public void testParseIntegerZero() {
		assertEquals(0, ArrayUtils.parseInteger(0, 1, N_0.getBytes()));
	}

	@Test
	public void testParseIntegerZeroExtraText() {
		assertEquals(0,
				ArrayUtils.parseInteger(TEXT.length(), 1, T_0.getBytes()));
	}

	@Test
	public void testParseIntegerPositives() {
		assertEquals(1, ArrayUtils.parseInteger(0, 1, N_1.getBytes()));
		assertEquals(27, ArrayUtils.parseInteger(0, 2, N_27.getBytes()));
		assertEquals(417, ArrayUtils.parseInteger(0, 3, N_417.getBytes()));
		assertEquals(7483, ArrayUtils.parseInteger(0, 4, N_7483.getBytes()));
		assertEquals(71381, ArrayUtils.parseInteger(0, 5, N_71381.getBytes()));
		assertEquals(98317, ArrayUtils.parseInteger(0, 5, N_98317.getBytes()));
		assertEquals(1234567,
				ArrayUtils.parseInteger(0, 7, N_1234567.getBytes()));
	}

	@Test
	public void testParseIntegerPositivesExtraText() {
		assertEquals(1,
				ArrayUtils.parseInteger(TEXT.length(), 1, T_1.getBytes()));
		assertEquals(71381,
				ArrayUtils.parseInteger(TEXT.length(), 5, T_71381.getBytes()));
	}

	@Test
	public void testParseIntegerNegatives() {
		assertEquals(-1, ArrayUtils.parseInteger(0, 2, N_NEG_1.getBytes()));
		assertEquals(-27, ArrayUtils.parseInteger(0, 3, N_NEG_27.getBytes()));
		assertEquals(-417, ArrayUtils.parseInteger(0, 4, N_NEG_417.getBytes()));
		assertEquals(-7483,
				ArrayUtils.parseInteger(0, 5, N_NEG_7483.getBytes()));
		assertEquals(-71381,
				ArrayUtils.parseInteger(0, 6, N_NEG_71381.getBytes()));
		assertEquals(-98317,
				ArrayUtils.parseInteger(0, 6, N_NEG_98317.getBytes()));
		assertEquals(-1234567,
				ArrayUtils.parseInteger(0, 8, N_NEG_1234567.getBytes()));
	}

	@Test
	public void testParseIntegerNegativesExtraText() {
		assertEquals(-1,
				ArrayUtils.parseInteger(TEXT.length(), 2, T_NEG_1.getBytes()));
		assertEquals(
				-71381,
				ArrayUtils.parseInteger(TEXT.length(), 6,
						T_NEG_71381.getBytes()));
	}
}