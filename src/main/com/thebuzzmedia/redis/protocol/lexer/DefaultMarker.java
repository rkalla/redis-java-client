package com.thebuzzmedia.redis.protocol.lexer;

import java.util.ArrayList;
import java.util.List;

public class DefaultMarker implements IMarker {
	private byte replyType;
	private int index;
	private int length;
	private byte[] source;
	private List<IMarker> childMarkerList;

	public DefaultMarker(byte replyType, int index, byte[] source) {
		this(replyType, index, 0, source);
	}

	public DefaultMarker(byte replyType, int index, int length, byte[] source) {
		this.replyType = replyType;
		this.index = index;
		this.length = length;
		this.source = source;
	}

	@Override
	public String toString() {
		return DefaultMarker.class.getName() + "[replyType=" + replyType
				+ ", index=" + index + ", length=" + length + ", source="
				+ source + ", childMarkerList=" + childMarkerList + "]";
	}

	@Override
	public byte getReplyType() {
		return replyType;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public byte[] getSource() {
		return source;
	}

	@Override
	public boolean hasChildren() {
		return (childMarkerList == null || childMarkerList.isEmpty() ? false
				: true);
	}

	@Override
	public synchronized void addChildMarker(IMarker child) {
		if (child == null)
			return;

		if (childMarkerList == null)
			childMarkerList = new ArrayList<IMarker>();

		length += child.getLength();
		childMarkerList.add(child);
	}

	@Override
	public List<IMarker> getChildMarkerList() {
		return childMarkerList;
	}
}