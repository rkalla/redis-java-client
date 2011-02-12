package com.thebuzzmedia.redis.protocol.lexer;

import java.util.List;

public interface IMarker {
	public byte getReplyType();

	public int getIndex();

	public int getLength();
	
	public byte[] getSource();
	
	public boolean hasChildren();
	
	public void addChildMarker(IMarker child);

	public List<IMarker> getChildMarkerList();
}