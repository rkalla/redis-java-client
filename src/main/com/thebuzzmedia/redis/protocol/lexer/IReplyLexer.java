package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;

import com.thebuzzmedia.redis.MalformedReplyException;

public interface IReplyLexer {
	public static enum State {
		COMPLETE, INCOMPLETE
	}

	public State scan(int index, byte[] data, List<IMarker> markerList)
			throws MalformedReplyException;
}