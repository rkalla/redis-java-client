package com.thebuzzmedia.redis.reply;

/*
 * TODO: Look into if every error returned by Redis has the same reply format of
 * ERR: <some message>, if it does, consider adding an isError method to the reply.
 */
public interface IReply<T> {
	public byte getType();

	public int getSize();

	/*
	 * TODO: Consider making the base return value ALWAYS byte[] and just
	 * provide additional toXXX reply converters... like toSingleLineReply.
	 * 
	 * Those methods would throw an "InvalidReplyType" exception if they
	 * couldn't be convereted as their constructors take care of this anyway.
	 * 
	 * NOTE: This would require the individual Replies to hold on to their
	 * source IMarkers and their source markers hold on to the original byte[]
	 * array created to house the entire response from the server. This isn't
	 * BAD as long as we aren't creating copies, and as soon as the copies are
	 * created, we are discarding the references to the original byte[] (so we
	 * don't effectively double the memory requirements).
	 */
	public T getValue();
}