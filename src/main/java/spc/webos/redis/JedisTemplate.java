package spc.webos.redis;

import java.util.function.Consumer;

import redis.clients.jedis.JedisCommands;

public interface JedisTemplate
{
	void execute(Consumer<JedisCommands> consumer);

	boolean isReady();
}
