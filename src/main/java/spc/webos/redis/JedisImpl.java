package spc.webos.redis;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import redis.clients.jedis.JedisCommands;
import redis.clients.util.Pool;
import spc.webos.util.FTLUtil;
import spc.webos.util.SpringUtil;

public class JedisImpl implements JedisTemplate
{
	@Value("${app.redis.shard.hosts?}")
	protected String[] shardHosts;
	@Value("${app.redis.start?true}")
	protected boolean start = true;
	@Value("${app.redis.ftl?webos/jedis_shard}")
	protected String ftl = "webos/jedis_shard";
	@Value("${app.redis.poolName?jedisPool}")
	protected String poolName = "jedisPool";

	protected Pool<?> jedisPool;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public boolean isReady()
	{
		return jedisPool != null;
	}

	public void execute(Consumer<JedisCommands> consumer)
	{
		if (jedisPool == null) return;
		try (Closeable jedis = (Closeable) jedisPool.getResource())
		{
			consumer.accept(((JedisCommands) jedis));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@PostConstruct
	public void init() throws Exception
	{
		if (!start)
		{
			log.info("no start redis");
			return;
		}
		if (ftl.endsWith("shard")) initShard();
		else log.warn("unvalid ftl:{}", ftl);
	}

	public void initShard() throws Exception
	{
		if (shardHosts == null || shardHosts.length == 0)
		{
			log.info("no redis shard hosts!!!");
			return;
		}
		log.info("Redis shard hosts:{}", Arrays.toString(shardHosts));
		Map<String, Object> root = new HashMap<>();
		root.put("hosts", shardHosts);
		String beans = FTLUtil.ftl(ftl, root);
		log.debug("redis beans:{}", beans);
		SpringUtil.registerXMLBean(beans, true);
		jedisPool = SpringUtil.APPCXT.getBean(poolName, Pool.class);
	}
}
