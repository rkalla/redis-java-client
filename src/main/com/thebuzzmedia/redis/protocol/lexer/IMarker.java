package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;

/*
 * TODO: Rename, maybe IMark, and extend IToken from common-lib
 */
public interface IMarker {
	public byte getReplyType();

	public int getIndex();

	public int getLength();
	
	public byte[] getSource();
	
	public boolean hasChildren();
	
	public void addChildMarker(IMarker child) throws IllegalArgumentException;

	public List<IMarker> getChildMarkerList();
}