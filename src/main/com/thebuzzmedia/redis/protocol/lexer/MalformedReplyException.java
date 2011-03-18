package com.thebuzzmedia.redis.protocol.lexer;

import java.io.IOException;

public class MalformedReplyException extends IOException {
	private static final long serialVersionUID = 1L;

	public MalformedReplyException(String message) {
		super(message);
	}
}