package com.thebuzzmedia.redis.reply;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;

public abstract class AbstractReply<T> implements IReply<T> {
	protected byte type;

	public AbstractReply() {
		type = Constants.UNDEFINED;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[type=" + getType() + ", size="
				+ getSize() + ", value=" + getValue() + "]";
	}

	public byte getType() {
		return type;
	}

	protected abstract void parseMarker(IMarker marker);
}