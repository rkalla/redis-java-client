package com.thebuzzmedia.redis.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thebuzzmedia.redis.Constants;
import com.thebuzzmedia.redis.command.ICommand;
import com.thebuzzmedia.redis.protocol.lexer.DefaultReplyLexer;
import com.thebuzzmedia.redis.protocol.lexer.IMarker;
import com.thebuzzmedia.redis.protocol.lexer.IReplyLexer;
import com.thebuzzmedia.redis.reply.BulkReply;
import com.thebuzzmedia.redis.reply.ErrorReply;
import com.thebuzzmedia.redis.reply.IReply;
import com.thebuzzmedia.redis.reply.IntegerReply;
import com.thebuzzmedia.redis.reply.MultiBulkReply;
import com.thebuzzmedia.redis.reply.SingleLineReply;
import com.thebuzzmedia.redis.util.StrictDynamicByteArray;

/*
 * TODO: Per the Redis conversation in the Group, this would need to support
 * auto-reconnect parameters and behavior.
 * 
 * Maybe add a AUTO_RECONNECT boolean and an AUTO_RECONNECT_INTERVAL attempt
 * value.
 * 
 * TODO: Execute method will need to be changed to attempt a reconnect if the
 * server isn't responding or before an IOException is thrown if AUTO_RECONNECT
 * is enabled.
 * 
 * TODO: Should test this by never connecting and see if it works and also
 * by connecting, then shutting down the Redis server and making sure it notices
 * the connection is gone. And also a 3rd way of shutting down Redis server,
 * then bringing it back up all before failing.
 * 
 * TODO: Maybe add a EXECUTE_RETRY_COUNT variable if auto-reconnect is enabled? The
 * number of times a command will try to execute before failing?
 * 
 * TODO: Javadoc for this class should list all the system properties recognized
 * by this class.
 */
public class Connection {
	public static final int DEFAULT_PORT = 6379;

	public static final int CONNECT_TIMEOUT = Integer.getInteger(
			"redis.connection.connectTimeout", 60000);

	public static final int SEND_TIMEOUT = Integer.getInteger(
			"redis.connection.sendTimeout", 5000);

	public static final int REPLY_TIMEOUT = Integer.getInteger(
			"redis.connection.replyTimeout", 60000);

	public static final int RECEIVE_BUFFER_SIZE = Integer.getInteger(
			"redis.connection.receiveBufferSize", 8192);

	@SuppressWarnings("rawtypes")
	public static final List<IReply> EMPTY_REPLY_LIST = Collections.emptyList();

	private static final int CONNECT_CHECK_INTERVAL = Integer.getInteger(
			"redis.connection.connectCheckInterval", 25);

	private static final int SEND_CHECK_INTERVAL = Integer.getInteger(
			"redis.connection.sendCheckInterval", 5);

	private static final int REPLY_CHECK_INTERVAL = Integer.getInteger(
			"redis.connection.replyCheckInterval", 15);

	private static final IReplyLexer REPLY_LEXER = new DefaultReplyLexer();

	static {
		// Check public values
		if (CONNECT_TIMEOUT <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.connectTimeout' value '"
							+ CONNECT_TIMEOUT + "' must be > 0.");
		if (SEND_TIMEOUT <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.sendTimeout' value '"
							+ SEND_TIMEOUT + "' must be > 0.");
		if (REPLY_TIMEOUT <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.replyTimeout' value '"
							+ REPLY_TIMEOUT + "' must be > 0.");
		if (RECEIVE_BUFFER_SIZE <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.receiveBufferSize' value '"
							+ RECEIVE_BUFFER_SIZE + "' must be > 0.");

		// Check private values
		if (CONNECT_CHECK_INTERVAL <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.connectCheckInterval' value '"
							+ CONNECT_CHECK_INTERVAL + "' must be > 0.");
		if (SEND_CHECK_INTERVAL <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.sendCheckInterval' value '"
							+ SEND_CHECK_INTERVAL + "' must be > 0.");
		if (REPLY_CHECK_INTERVAL <= 0)
			throw new RuntimeException(
					"System property 'redis.connection.replyCheckInterval' value '"
							+ REPLY_CHECK_INTERVAL + "' must be > 0.");
	}

	private SocketChannel channel;
	private ByteBuffer receiveBuffer;

	public Connection(String hostname) throws IllegalArgumentException,
			IOException {
		this(hostname, DEFAULT_PORT);
	}

	public Connection(String hostname, int port)
			throws IllegalArgumentException, IOException {
		this(new InetSocketAddress(hostname, port));
	}

	public Connection(SocketAddress address) throws IllegalArgumentException,
			IOException {
		if (address == null)
			throw new IllegalArgumentException("address cannot be null");

		channel = SocketChannel.open();
		channel.configureBlocking(false);

		if (!channel.connect(address)) {
			int timeWaited = 0;

			while (!channel.finishConnect() && timeWaited < CONNECT_TIMEOUT) {
				try {
					Thread.sleep(CONNECT_CHECK_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				timeWaited += CONNECT_CHECK_INTERVAL;
			}

			if (!channel.isConnected())
				throw new IOException("Connection to Redis server timed out ("
						+ CONNECT_TIMEOUT + " ms)");
		}

		/**
		 * Direct allocations should be used sparingly and based on the design
		 * (and risk/reward issues) with direct buffers, they should only be
		 * used for long-lived, performance-sensitive buffers.
		 * 
		 * Our receive buffer is both of those things, living for the entire
		 * duration of the connection (and always being re-used) as well as
		 * being performance-sensitive given that we can get the bytes out of
		 * the network stack and into a direct buffer (by the OS) faster than we
		 * can get it converted by the JVM into bytes managed from inside.
		 */
		receiveBuffer = ByteBuffer.allocateDirect(RECEIVE_BUFFER_SIZE);
	}

	@SuppressWarnings("rawtypes")
	public List<IReply> execute(ICommand... commands) throws IOException {
		if (!channel.isConnected()) {
			// TODO: Add reconnect logic here.

			throw new IOException(
					"Unable to execute the given command(s). This connection was either never successfully connected to a Redis server or the connection was lost.");
		}

		List<IReply> replyList = EMPTY_REPLY_LIST;

		// Only continue if there are commands to send
		if (commands != null && commands.length > 0) {
			replyList = new ArrayList<IReply>(commands.length);

			// Send all commands (pipeline) to Redis.
			for (ICommand command : commands)
				sendCommand(command);

			/*
			 * Wait for all the replies to come back and our lexer to correctly
			 * mark them all up in the returned byte[].
			 */
			List<IMarker> markerList = receiveReply(commands.length);

			/*
			 * Convert all the marks to replies. If we got this far and didn't
			 * get an IOException from the receiveReply call above, then we have
			 * all our replies and all marks were successfully created so there
			 * is no need for error-handling code here, just continue
			 * processing.
			 */
			replyList = new ArrayList<IReply>(markerList.size());

			/*
			 * Marks have a 1:1 relationship with Replies, so convert every
			 * IMark into its corresponding IReply.
			 */
			for (IMarker mark : markerList) {
				switch (mark.getReplyType()) {
				case Constants.REPLY_TYPE_INTEGER:
					replyList.add(new IntegerReply(mark));
					break;
				case Constants.REPLY_TYPE_SINGLE_LINE:
					replyList.add(new SingleLineReply(mark));
					break;
				case Constants.REPLY_TYPE_BULK:
					replyList.add(new BulkReply(mark));
					break;
				case Constants.REPLY_TYPE_MULTI_BULK:
					replyList.add(new MultiBulkReply(mark));
					break;
				case Constants.REPLY_TYPE_ERROR:
					replyList.add(new ErrorReply(mark));
					break;

				/*
				 * We should never get back top-level IMarker instances from the
				 * lexer that is not one of the known types as it should only be
				 * detecting and lexing the 5 Redis return types.
				 */
				default:
					throw new RuntimeException(
							"An IMarker of unknown type ["
									+ mark.getReplyType()
									+ "] was returned by the lexer, this should not happen and cannot be turned into a valid IReply instance.");
				}
			}
		}

		return replyList;
	}

	protected void sendCommand(ICommand command) throws IOException {
		long timeWaited = 0;
		ByteBuffer source = ByteBuffer.wrap(command.getCommand());

		// Keep track of this for a more informative exception message
		int originalByteCount = source.remaining();

		while (source.hasRemaining() && timeWaited <= SEND_TIMEOUT) {
			/*
			 * If we are unable to write any bytes to the underlying network
			 * stream (we filled it up), we should go to sleep for a very brief
			 * interval and try again momentarily.
			 */
			if (channel.write(source) <= 0) {
				try {
					Thread.sleep(SEND_CHECK_INTERVAL);
				} catch (InterruptedException e) {
					// no-op
				}

				timeWaited += SEND_CHECK_INTERVAL;
			}
		}

		// We timed out and still had bytes to write, so throw an exception.
		if (source.hasRemaining())
			throw new IOException("Unable to finish sending command ["
					+ command + "] to Redis before timing out (" + SEND_TIMEOUT
					+ " ms), " + (originalByteCount - source.remaining())
					+ " of " + originalByteCount
					+ " bytes were written to the network.");
	}

	protected List<IMarker> receiveReply(int requiredReplyCount)
			throws IOException {
		int index = 0;
		int timeWaited = 0;
		List<IMarker> markerList = new ArrayList<IMarker>();
		StrictDynamicByteArray data = new StrictDynamicByteArray();

		/*
		 * Loop, trying to read from the channel, until we either time out, or
		 * read enough bytes that we successfully mark all the pending replies
		 * for every command we executed.
		 */
		while (markerList.size() < requiredReplyCount
				&& timeWaited < REPLY_TIMEOUT) {
			// Reset the buffer
			receiveBuffer.clear();

			// Read all available bytes or up to the size of our read buffer
			while (channel.read(receiveBuffer) > 0) {
				// Prepare the buffer to be read from
				receiveBuffer.flip();

				// Append what we read so far to our resultant byte[]
				data.append(receiveBuffer);

				// Reset the read buffer and try and read some more.
				receiveBuffer.clear();
			}

			// Update the index to point beyond the last valid mark, if avail
			if (!markerList.isEmpty()) {
				IMarker mark = markerList.get(markerList.size() - 1);
				index = mark.getIndex() + mark.getLength();
			}

			/*
			 * We fell out of the above while-loop either because we read all
			 * the bytes OR because the underlying OS buffer ran out and needs
			 * to buffer some more, either way, now is the time to lex what we
			 * have and figure out if we are done or not.
			 */
			REPLY_LEXER.scan(index, data.getArray(), markerList);

			/*
			 * We marked all the valid replies in the byte[] we have read so
			 * far. If we don't have enough replies from the server yet, we need
			 * to wait on more data from the server/network.
			 */
			if (markerList.size() < requiredReplyCount) {
				try {
					/*
					 * We sleep here for a very small unit of time as opposed to
					 * spinning right back around to the top of the while loop
					 * to "read" more data in the case where the server is
					 * bogged down and there is no more data from the network
					 * yet; if we didn't sleep, even for a little bit, the
					 * client Thread calling this would peg the CPU waiting on
					 * data from the server and kill the app.
					 */
					Thread.sleep(REPLY_CHECK_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				timeWaited += REPLY_CHECK_INTERVAL;
			}
		}

		// Make sure we didn't time out and we have all our replies
		if (markerList.size() < requiredReplyCount
				&& timeWaited >= REPLY_TIMEOUT)
			throw new IOException(
					"Connection timed out ("
							+ REPLY_TIMEOUT
							+ " ms) waiting for a complete reply from the Redis server for ["
							+ requiredReplyCount
							+ "] issued commands. Only ["
							+ markerList.size()
							+ "] replies were received. Server load may be too high or network connection too slow.");

		return markerList;
	}
}