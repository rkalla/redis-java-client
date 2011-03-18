package com.thebuzzmedia.redis.protocol.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight {@link IMarker} implementation. This class does not implement any
 * logic checks in the constructor to make sure valid values are being passed
 * in, it is assumed the caller will vet the values before trying to create an
 * instance of this type.
 * <p/>
 * This was done for performance reasons as any {@link IReplyLexer}
 * implementations will create many instances of this type and should vet the
 * values ahead of time.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
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
		return DefaultMarker.class.getName()
				+ "[replyType="
				+ replyType
				+ ", index="
				+ index
				+ ", length="
				+ length
				+ ", source="
				+ source
				+ ", sourceLength="
				+ (source == null ? "N/A (null)" : source.length)
				+ ", childMarkerList="
				+ childMarkerList
				+ ", childMarkerCount="
				+ (childMarkerList == null ? "N/A (null)" : childMarkerList
						.size()) + ", sourceContent=" + new String(source)
				+ "]";
	}

	public byte getReplyType() {
		return replyType;
	}

	public int getIndex() {
		return index;
	}

	public int getLength() {
		return length;
	}

	public byte[] getSource() {
		return source;
	}

	public boolean hasChildren() {
		return (childMarkerList == null || childMarkerList.isEmpty() ? false
				: true);
	}

	public synchronized void addChildMarker(IMarker child)
			throws IllegalArgumentException {
		if (child == null)
			throw new IllegalArgumentException("child cannot be null");

		if (childMarkerList == null)
			childMarkerList = new ArrayList<IMarker>();

		length += child.getLength();
		childMarkerList.add(child);
	}

	public List<IMarker> getChildMarkerList() {
		return childMarkerList;
	}
}