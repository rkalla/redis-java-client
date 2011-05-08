package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;

/*
 * TODO: Consider designing reply lexer to be a stateful 1:1 relationship with
 * Connection, because the lexer needs to remember it's current state when partially
 * parsed through a multi-bulk, more specifically:
 * 
 * 1. current markerList so I can remember where to pick back up
 * 2. the multi-bulk's size, so it knows how many remaining bulk replies it needs
 * to parse.
 * 
 * Since connections are designed to be serial anyway ("execute" does a send/receive)
 * this shouldn't be an issue at all.
 */
public interface IReplyLexer {
	/*
	 * TODO: This needs to be redesign to return more detailed state
	 * information. For example, if a multi-bulk reply was partially parsed,
	 * then this should return something like INCOMPLETE_MULTI_BULK so we knew
	 * the state the parser was in.
	 */
	public static enum State {
		COMPLETE, INCOMPLETE
	}

	/*
	 * TODO: Add IllegalArgumentException clause if the indices don't check out.
	 */
	public State scan(int index, int length, byte[] data,
			List<IMarker> markerList) throws MalformedReplyException;
}