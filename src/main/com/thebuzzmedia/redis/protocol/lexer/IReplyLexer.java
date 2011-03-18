package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;


public interface IReplyLexer {
	public static enum State {
		COMPLETE, INCOMPLETE
	}

	public State scan(int index, int length, byte[] data,
			List<IMarker> markerList) throws MalformedReplyException;
}