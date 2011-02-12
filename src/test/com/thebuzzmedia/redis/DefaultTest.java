package com.thebuzzmedia.redis;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.thebuzzmedia.redis.command.GET;
import com.thebuzzmedia.redis.command.SET;
import com.thebuzzmedia.redis.protocol.Connection;
import com.thebuzzmedia.redis.reply.IReply;

public class DefaultTest {
	@Test
	public void test() throws IllegalArgumentException, IOException {
		Connection c = new Connection("localhost");
		
		SET s = new SET("name", "");
		List<IReply> list = c.execute(s);
		
		for(IReply reply : list)
			System.out.println("SET REPLY: " + new String((char[])reply.getValue()));
		
		GET g = new GET("name2");
		list = c.execute(g);
		
		for(IReply reply : list)
			System.out.println("GET REPLY: " + new String((char[])reply.getValue()));
	}
}