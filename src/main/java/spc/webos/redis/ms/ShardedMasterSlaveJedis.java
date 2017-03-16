package spc.webos.redis.ms;

import java.io.Closeable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;
import redis.clients.util.Sharded;

public class ShardedMasterSlaveJedis extends
		Sharded<MasterSlaveJedis, MasterSlaveJedisShardInfo> implements
		JedisCommands, BinaryJedisCommands, Closeable {

	protected final Set<MasterSlaveHostAndPort> connectionDesc;
	
	protected Pool<ShardedMasterSlaveJedis> dataSource = null;

	protected final List<MasterSlaveJedisShardInfo> shards;
	
	public ShardedMasterSlaveJedis(List<MasterSlaveJedisShardInfo> shards) {
		super(shards);
		this.shards = shards;
		this.connectionDesc = new LinkedHashSet<MasterSlaveHostAndPort>();
		for(MasterSlaveJedisShardInfo shard : shards){
			Set<HostAndPort> slaves = new LinkedHashSet<HostAndPort>();
			for(JedisShardInfo slaveShard : shard.getSlaveShards()){
				slaves.add(new HostAndPort(slaveShard.getHost(), slaveShard.getPort()));
			}
			this.connectionDesc.add(new MasterSlaveHostAndPort(shard.getMasterName(), new HostAndPort(shard.getMasterShard().getHost(), shard.getMasterShard().getPort()), slaves));
		}
	}

	public ShardedMasterSlaveJedis(List<MasterSlaveJedisShardInfo> shards,
			Hashing algo) {
		super(shards, algo);
		this.shards = shards;
		this.connectionDesc = new LinkedHashSet<MasterSlaveHostAndPort>();
		for(MasterSlaveJedisShardInfo shard : shards){
			Set<HostAndPort> slaves = new LinkedHashSet<HostAndPort>();
			for(JedisShardInfo slaveShard : shard.getSlaveShards()){
				slaves.add(new HostAndPort(slaveShard.getHost(), slaveShard.getPort()));
			}
			this.connectionDesc.add(new MasterSlaveHostAndPort(shard.getMasterName(), new HostAndPort(shard.getMasterShard().getHost(), shard.getMasterShard().getPort()), slaves));
		}
	}

	public ShardedMasterSlaveJedis(List<MasterSlaveJedisShardInfo> shards,
			Pattern keyTagPattern) {
		super(shards, keyTagPattern);
		this.shards = shards;
		this.connectionDesc = new LinkedHashSet<MasterSlaveHostAndPort>();
		for(MasterSlaveJedisShardInfo shard : shards){
			Set<HostAndPort> slaves = new LinkedHashSet<HostAndPort>();
			for(JedisShardInfo slaveShard : shard.getSlaveShards()){
				slaves.add(new HostAndPort(slaveShard.getHost(), slaveShard.getPort()));
			}
			this.connectionDesc.add(new MasterSlaveHostAndPort(shard.getMasterName(), new HostAndPort(shard.getMasterShard().getHost(), shard.getMasterShard().getPort()), slaves));
		}
	}

	public ShardedMasterSlaveJedis(List<MasterSlaveJedisShardInfo> shards,
			Hashing algo, Pattern keyTagPattern) {
		super(shards, algo, keyTagPattern);
		this.shards = shards;
		this.connectionDesc = new LinkedHashSet<MasterSlaveHostAndPort>();
		for(MasterSlaveJedisShardInfo shard : shards){
			Set<HostAndPort> slaves = new LinkedHashSet<HostAndPort>();
			for(JedisShardInfo slaveShard : shard.getSlaveShards()){
				slaves.add(new HostAndPort(slaveShard.getHost(), slaveShard.getPort()));
			}
			this.connectionDesc.add(new MasterSlaveHostAndPort(shard.getMasterName(), new HostAndPort(shard.getMasterShard().getHost(), shard.getMasterShard().getPort()), slaves));
		}
	}
	
	public Set<MasterSlaveHostAndPort> getConnectionDesc() {
		return connectionDesc;
	}

	public String set(String key, String value) {
		
		return getShard(key).set(key, value);
	}

	public String set(String key, String value, String nxxx, String expx,
			long time) {
		
		return getShard(key).set(key, value, nxxx, expx, time);
	}

	public String get(String key) {
		
		return getShard(key).get(key);
	}

	public String echo(String string) {
		MasterSlaveJedis j = getShard(string);
		return getShard(string).echo(string);
	}

	public Boolean exists(String key) {
		
		return getShard(key).exists(key);
	}

	public String type(String key) {
		
		return getShard(key).type(key);
	}

	public Long expire(String key, int seconds) {
		
		return getShard(key).expire(key, seconds);
	}

	public Long expireAt(String key, long unixTime) {
		
		return getShard(key).expireAt(key, unixTime);
	}

	public Long ttl(String key) {
		
		return getShard(key).ttl(key);
	}

	public Boolean setbit(String key, long offset, boolean value) {
		
		return getShard(key).setbit(key, offset, value);
	}

	public Boolean setbit(String key, long offset, String value) {
		
		return getShard(key).setbit(key, offset, value);
	}

	public Boolean getbit(String key, long offset) {
		
		return getShard(key).getbit(key, offset);
	}

	public Long setrange(String key, long offset, String value) {
		
		return getShard(key).setrange(key, offset, value);
	}

	public String getrange(String key, long startOffset, long endOffset) {
		
		return getShard(key).getrange(key, startOffset, endOffset);
	}

	public String getSet(String key, String value) {
		
		return getShard(key).getSet(key, value);
	}

	public Long setnx(String key, String value) {
		
		return getShard(key).setnx(key, value);
	}

	public String setex(String key, int seconds, String value) {
		
		return getShard(key).setex(key, seconds, value);
	}

	@Deprecated
	public List<String> blpop(String arg) {
		return getShard(arg).blpop(arg);
	}

	public List<String> blpop(int timeout, String key) {
		
		return getShard(key).blpop(timeout, key);
	}

	@Deprecated
	public List<String> brpop(String arg) {
		return getShard(arg).brpop(arg);
	}

	public List<String> brpop(int timeout, String key) {
		
		return getShard(key).brpop(timeout, key);
	}

	public Long decrBy(String key, long integer) {
		
		return getShard(key).decrBy(key, integer);
	}

	public Long decr(String key) {
		
		return getShard(key).decr(key);
	}

	public Long incrBy(String key, long integer) {
		
		return getShard(key).incrBy(key, integer);
	}

	public Double incrByFloat(String key, double integer) {
		
		return getShard(key).incrByFloat(key, integer);
	}

	public Long incr(String key) {
		
		return getShard(key).incr(key);
	}

	public Long append(String key, String value) {
		
		return getShard(key).append(key, value);
	}

	public String substr(String key, int start, int end) {
		
		return getShard(key).substr(key, start, end);
	}

	public Long hset(String key, String field, String value) {
		
		return getShard(key).hset(key, field, value);
	}

	public String hget(String key, String field) {
		
		return getShard(key).hget(key, field);
	}

	public Long hsetnx(String key, String field, String value) {
		
		return getShard(key).hsetnx(key, field, value);
	}

	public String hmset(String key, Map<String, String> hash) {
		
		return getShard(key).hmset(key, hash);
	}

	public List<String> hmget(String key, String... fields) {
		
		return getShard(key).hmget(key, fields);
	}

	public Long hincrBy(String key, String field, long value) {
		
		return getShard(key).hincrBy(key, field, value);
	}

	public Double hincrByFloat(String key, String field, double value) {
		
		return getShard(key).hincrByFloat(key, field, value);
	}

	public Boolean hexists(String key, String field) {
		
		return getShard(key).hexists(key, field);
	}

	public Long del(String key) {
		
		return getShard(key).del(key);
	}

	public Long hdel(String key, String... fields) {
		
		return getShard(key).hdel(key, fields);
	}

	public Long hlen(String key) {
		
		return getShard(key).hlen(key);
	}

	public Set<String> hkeys(String key) {
		
		return getShard(key).hkeys(key);
	}

	public List<String> hvals(String key) {
		
		return getShard(key).hvals(key);
	}

	public Map<String, String> hgetAll(String key) {
		
		return getShard(key).hgetAll(key);
	}

	public Long rpush(String key, String... strings) {
		
		return getShard(key).rpush(key, strings);
	}

	public Long lpush(String key, String... strings) {
		
		return getShard(key).lpush(key, strings);
	}

	public Long lpushx(String key, String... string) {
		
		return getShard(key).lpushx(key, string);
	}

	public Long strlen(final String key) {
		
		return getShard(key).strlen(key);
	}

	public Long move(String key, int dbIndex) {
		
		return getShard(key).move(key, dbIndex);
	}

	public Long rpushx(String key, String... string) {
		
		return getShard(key).rpushx(key, string);
	}

	public Long persist(final String key) {
		
		return getShard(key).persist(key);
	}

	public Long llen(String key) {
		
		return getShard(key).llen(key);
	}

	public List<String> lrange(String key, long start, long end) {
		
		return getShard(key).lrange(key, start, end);
	}

	public String ltrim(String key, long start, long end) {
		
		return getShard(key).ltrim(key, start, end);
	}

	public String lindex(String key, long index) {
		
		return getShard(key).lindex(key, index);
	}

	public String lset(String key, long index, String value) {
		
		return getShard(key).lset(key, index, value);
	}

	public Long lrem(String key, long count, String value) {
		
		return getShard(key).lrem(key, count, value);
	}

	public String lpop(String key) {
		
		return getShard(key).lpop(key);
	}

	public String rpop(String key) {
		
		return getShard(key).rpop(key);
	}

	public Long sadd(String key, String... members) {
		
		return getShard(key).sadd(key, members);
	}

	public Set<String> smembers(String key) {
		
		return getShard(key).smembers(key);
	}

	public Long srem(String key, String... members) {
		
		return getShard(key).srem(key, members);
	}

	public String spop(String key) {
		
		return getShard(key).spop(key);
	}

	public Long scard(String key) {
		
		return getShard(key).scard(key);
	}

	public Boolean sismember(String key, String member) {
		
		return getShard(key).sismember(key, member);
	}

	public String srandmember(String key) {
		
		return getShard(key).srandmember(key);
	}

	public List<String> srandmember(String key, int count) {
		return getShard(key).srandmember(key, count);
	}

	public Long zadd(String key, double score, String member) {
		return getShard(key).zadd(key, score, member);
	}

	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return getShard(key).zadd(key, scoreMembers);
	}

	public Set<String> zrange(String key, long start, long end) {
		return getShard(key).zrange(key, start, end);
	}

	public Long zrem(String key, String... members) {
		return getShard(key).zrem(key, members);
	}

	public Double zincrby(String key, double score, String member) {
		return getShard(key).zincrby(key, score, member);
	}

	public Long zrank(String key, String member) {
		return getShard(key).zrank(key, member);
	}

	public Long zrevrank(String key, String member) {
		return getShard(key).zrevrank(key, member);
	}

	public Set<String> zrevrange(String key, long start, long end) {
		return getShard(key).zrevrange(key, start, end);
	}

	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return getShard(key).zrangeWithScores(key, start, end);
	}

	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return getShard(key).zrevrangeWithScores(key, start, end);
	}

	public Long zcard(String key) {
		return getShard(key).zcard(key);
	}

	public Double zscore(String key, String member) {
		return getShard(key).zscore(key, member);
	}

	public List<String> sort(String key) {
		return getShard(key).sort(key);
	}

	public List<String> sort(String key, SortingParams sortingParameters) {
		return getShard(key).sort(key, sortingParameters);
	}

	public Long zcount(String key, double min, double max) {
		return getShard(key).zcount(key, min, max);
	}

	public Long zcount(String key, String min, String max) {
		return getShard(key).zcount(key, min, max);
	}

	public Set<String> zrangeByScore(String key, double min, double max) {
		return getShard(key).zrangeByScore(key, min, max);
	}

	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return getShard(key).zrevrangeByScore(key, max, min);
	}

	public Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count) {
		return getShard(key).zrangeByScore(key, min, max, offset, count);
	}

	public Set<String> zrevrangeByScore(String key, double max, double min,
			int offset, int count) {
		return getShard(key).zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return getShard(key).zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, double min,
			double max, int offset, int count) {
		return getShard(key).zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min, int offset, int count) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Set<String> zrangeByScore(String key, String min, String max) {
		return getShard(key).zrangeByScore(key, min, max);
	}

	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return getShard(key).zrevrangeByScore(key, max, min);
	}

	public Set<String> zrangeByScore(String key, String min, String max,
			int offset, int count) {
		return getShard(key).zrangeByScore(key, min, max, offset, count);
	}

	public Set<String> zrevrangeByScore(String key, String max, String min,
			int offset, int count) {
		return getShard(key).zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return getShard(key).zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<Tuple> zrangeByScoreWithScores(String key, String min,
			String max, int offset, int count) {
		return getShard(key).zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min, int offset, int count) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Long zremrangeByRank(String key, long start, long end) {
		return getShard(key).zremrangeByRank(key, start, end);
	}

	public Long zremrangeByScore(String key, double start, double end) {
		return getShard(key).zremrangeByScore(key, start, end);
	}

	public Long zremrangeByScore(String key, String start, String end) {
		return getShard(key).zremrangeByScore(key, start, end);
	}

	public Long zlexcount(final String key, final String min, final String max) {
		return getShard(key).zlexcount(key, min, max);
	}

	public Set<String> zrangeByLex(final String key, final String min,
			final String max) {
		return getShard(key).zrangeByLex(key, min, max);
	}

	public Set<String> zrangeByLex(final String key, final String min,
			final String max, final int offset, final int count) {
		return getShard(key).zrangeByLex(key, min, max, offset, count);
	}

	public Long zremrangeByLex(final String key, final String min,
			final String max) {
		return getShard(key).zremrangeByLex(key, min, max);
	}

	public Long linsert(String key, LIST_POSITION where, String pivot,
			String value) {
		return getShard(key).linsert(key, where, pivot, value);
	}

	public Long bitcount(final String key) {
		return getShard(key).bitcount(key);
	}

	public Long bitcount(final String key, long start, long end) {
		return getShard(key).bitcount(key, start, end);
	}

	@Deprecated
	/**
	 * This method is deprecated due to bug (scan cursor should be unsigned long)
	 * And will be removed on next major release
	 * @see https://github.com/xetorthio/jedis/issues/531 
	 */
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		return getShard(key).hscan(key, cursor);
	}

	@Deprecated
	/**
	 * This method is deprecated due to bug (scan cursor should be unsigned long)
	 * And will be removed on next major release
	 * @see https://github.com/xetorthio/jedis/issues/531 
	 */
	public ScanResult<String> sscan(String key, int cursor) {
		return getShard(key).sscan(key, cursor);
	}

	@Deprecated
	/**
	 * This method is deprecated due to bug (scan cursor should be unsigned long)
	 * And will be removed on next major release
	 * @see https://github.com/xetorthio/jedis/issues/531 
	 */
	public ScanResult<Tuple> zscan(String key, int cursor) {
		return getShard(key).zscan(key, cursor);
	}

	public ScanResult<Entry<String, String>> hscan(String key,
			final String cursor) {
		return getShard(key).hscan(key, cursor);
	}

	public ScanResult<String> sscan(String key, final String cursor) {
		return getShard(key).sscan(key, cursor);
	}

	public ScanResult<Tuple> zscan(String key, final String cursor) {
		return getShard(key).zscan(key, cursor);
	}

	public Long pfadd(String key, String... elements) {
		return getShard(key).pfadd(key, elements);
	}

	public long pfcount(String key) {
		return getShard(key).pfcount(key);
	}

	public String set(byte[] key, byte[] value) {
		return getShard(key).set(key, value);
	}

	public byte[] get(byte[] key) {
		return getShard(key).get(key);
	}

	public Boolean exists(byte[] key) {
		return getShard(key).exists(key);
	}

	public String type(byte[] key) {
		return getShard(key).type(key);
	}

	public Long expire(byte[] key, int seconds) {
		return getShard(key).expire(key, seconds);
	}

	public Long expireAt(byte[] key, long unixTime) {
		return getShard(key).expireAt(key, unixTime);
	}

	public Long ttl(byte[] key) {
		return getShard(key).ttl(key);
	}

	public byte[] getSet(byte[] key, byte[] value) {
		return getShard(key).getSet(key, value);
	}

	public Long setnx(byte[] key, byte[] value) {
		return getShard(key).setnx(key, value);
	}

	public String setex(byte[] key, int seconds, byte[] value) {
		return getShard(key).setex(key, seconds, value);
	}

	public Long decrBy(byte[] key, long integer) {
		return getShard(key).decrBy(key, integer);
	}

	public Long decr(byte[] key) {
		return getShard(key).decr(key);
	}

	public Long del(byte[] key) {
		return getShard(key).del(key);
	}

	public Long incrBy(byte[] key, long integer) {
		return getShard(key).incrBy(key, integer);
	}

	public Double incrByFloat(byte[] key, double integer) {
		return getShard(key).incrByFloat(key, integer);
	}

	public Long incr(byte[] key) {
		return getShard(key).incr(key);
	}

	public Long append(byte[] key, byte[] value) {
		return getShard(key).append(key, value);
	}

	public byte[] substr(byte[] key, int start, int end) {
		return getShard(key).substr(key, start, end);
	}

	public Long hset(byte[] key, byte[] field, byte[] value) {
		return getShard(key).hset(key, field, value);
	}

	public byte[] hget(byte[] key, byte[] field) {
		return getShard(key).hget(key, field);
	}

	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return getShard(key).hsetnx(key, field, value);
	}

	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return getShard(key).hmset(key, hash);
	}

	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return getShard(key).hmget(key, fields);
	}

	public Long hincrBy(byte[] key, byte[] field, long value) {
		return getShard(key).hincrBy(key, field, value);
	}

	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return getShard(key).hincrByFloat(key, field, value);
	}

	public Boolean hexists(byte[] key, byte[] field) {
		return getShard(key).hexists(key, field);
	}

	public Long hdel(byte[] key, byte[]... fields) {
		return getShard(key).hdel(key, fields);
	}

	public Long hlen(byte[] key) {
		return getShard(key).hlen(key);
	}

	public Set<byte[]> hkeys(byte[] key) {
		return getShard(key).hkeys(key);
	}

	public Collection<byte[]> hvals(byte[] key) {
		return getShard(key).hvals(key);
	}

	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return getShard(key).hgetAll(key);
	}

	public Long rpush(byte[] key, byte[]... strings) {
		return getShard(key).rpush(key, strings);
	}

	public Long lpush(byte[] key, byte[]... strings) {
		return getShard(key).lpush(key, strings);
	}

	public Long strlen(final byte[] key) {
		return getShard(key).strlen(key);
	}

	public Long lpushx(byte[] key, byte[]... string) {
		return getShard(key).lpushx(key, string);
	}

	public Long persist(final byte[] key) {
		return getShard(key).persist(key);
	}

	public Long rpushx(byte[] key, byte[]... string) {
		return getShard(key).rpushx(key, string);
	}

	public Long llen(byte[] key) {
		return getShard(key).llen(key);
	}

	public List<byte[]> lrange(byte[] key, long start, long end) {
		return getShard(key).lrange(key, start, end);
	}

	public String ltrim(byte[] key, long start, long end) {
		return getShard(key).ltrim(key, start, end);
	}

	public byte[] lindex(byte[] key, long index) {
		return getShard(key).lindex(key, index);
	}

	public String lset(byte[] key, long index, byte[] value) {
		return getShard(key).lset(key, index, value);
	}

	public Long lrem(byte[] key, long count, byte[] value) {
		return getShard(key).lrem(key, count, value);
	}

	public byte[] lpop(byte[] key) {
		return getShard(key).lpop(key);
	}

	public byte[] rpop(byte[] key) {
		return getShard(key).rpop(key);
	}

	public Long sadd(byte[] key, byte[]... members) {
		return getShard(key).sadd(key, members);
	}

	public Set<byte[]> smembers(byte[] key) {
		return getShard(key).smembers(key);
	}

	public Long srem(byte[] key, byte[]... members) {
		return getShard(key).srem(key, members);
	}

	public byte[] spop(byte[] key) {
		return getShard(key).spop(key);
	}

	public Long scard(byte[] key) {
		return getShard(key).scard(key);
	}

	public Boolean sismember(byte[] key, byte[] member) {
		return getShard(key).sismember(key, member);
	}

	public byte[] srandmember(byte[] key) {
		return getShard(key).srandmember(key);
	}

	public List<byte[]> srandmember(byte[] key, int count) {
		return getShard(key).srandmember(key, count);
	}

	public Long zadd(byte[] key, double score, byte[] member) {
		return getShard(key).zadd(key, score, member);
	}

	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
		return getShard(key).zadd(key, scoreMembers);
	}

	public Set<byte[]> zrange(byte[] key, long start, long end) {
		return getShard(key).zrange(key, start, end);
	}

	public Long zrem(byte[] key, byte[]... members) {
		return getShard(key).zrem(key, members);
	}

	public Double zincrby(byte[] key, double score, byte[] member) {
		return getShard(key).zincrby(key, score, member);
	}

	public Long zrank(byte[] key, byte[] member) {
		return getShard(key).zrank(key, member);
	}

	public Long zrevrank(byte[] key, byte[] member) {
		return getShard(key).zrevrank(key, member);
	}

	public Set<byte[]> zrevrange(byte[] key, long start, long end) {
		return getShard(key).zrevrange(key, start, end);
	}

	public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
		return getShard(key).zrangeWithScores(key, start, end);
	}

	public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
		return getShard(key).zrevrangeWithScores(key, start, end);
	}

	public Long zcard(byte[] key) {
		return getShard(key).zcard(key);
	}

	public Double zscore(byte[] key, byte[] member) {
		return getShard(key).zscore(key, member);
	}

	public List<byte[]> sort(byte[] key) {
		return getShard(key).sort(key);
	}

	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return getShard(key).sort(key, sortingParameters);
	}

	public Long zcount(byte[] key, double min, double max) {
		return getShard(key).zcount(key, min, max);
	}

	public Long zcount(byte[] key, byte[] min, byte[] max) {
		return getShard(key).zcount(key, min, max);
	}

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return getShard(key).zrangeByScore(key, min, max);
	}

	public Set<byte[]> zrangeByScore(byte[] key, double min, double max,
			int offset, int count) {
		return getShard(key).zrangeByScore(key, min, max, offset, count);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return getShard(key).zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
			double max, int offset, int count) {
		return getShard(key).zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return getShard(key).zrangeByScore(key, min, max);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		return getShard(key).zrangeByScoreWithScores(key, min, max);
	}

	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min,
			byte[] max, int offset, int count) {
		return getShard(key).zrangeByScoreWithScores(key, min, max, offset, count);
	}

	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max,
			int offset, int count) {
		return getShard(key).zrangeByScore(key, min, max, offset, count);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return getShard(key).zrevrangeByScore(key, max, min);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min,
			int offset, int count) {
		return getShard(key).zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min, int offset, int count) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return getShard(key).zrevrangeByScore(key, max, min);
	}

	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min,
			int offset, int count) {
		return getShard(key).zrevrangeByScore(key, max, min, offset, count);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min);
	}

	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min, int offset, int count) {
		return getShard(key).zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	public Long zremrangeByRank(byte[] key, long start, long end) {
		return getShard(key).zremrangeByRank(key, start, end);
	}

	public Long zremrangeByScore(byte[] key, double start, double end) {
		return getShard(key).zremrangeByScore(key, start, end);
	}

	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		return getShard(key).zremrangeByScore(key, start, end);
	}

	public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
		return getShard(key).zlexcount(key, min, max);
	}

	public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min,
			final byte[] max) {
		return getShard(key).zrangeByLex(key, min, max);
	}

	public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min,
			final byte[] max, final int offset, final int count) {
		return getShard(key).zrangeByLex(key, min, max, offset, count);
	}

	public Long zremrangeByLex(final byte[] key, final byte[] min,
			final byte[] max) {
		return getShard(key).zremrangeByLex(key, min, max);
	}

	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value) {
		return getShard(key).linsert(key, where, pivot, value);
	}

	public Long objectRefcount(byte[] key) {
		return getShard(key).objectRefcount(key);
	}

	public byte[] objectEncoding(byte[] key) {
		return getShard(key).objectEncoding(key);
	}

	public Long objectIdletime(byte[] key) {
		return getShard(key).objectIdletime(key);
	}

	public Boolean setbit(byte[] key, long offset, boolean value) {
		return getShard(key).setbit(key, offset, value);
	}

	public Boolean setbit(byte[] key, long offset, byte[] value) {
		return getShard(key).setbit(key, offset, value);
	}

	public Boolean getbit(byte[] key, long offset) {
		return getShard(key).getbit(key, offset);
	}

	public Long setrange(byte[] key, long offset, byte[] value) {
		return getShard(key).setrange(key, offset, value);
	}

	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		return getShard(key).getrange(key, startOffset, endOffset);
	}

	public Long move(byte[] key, int dbIndex) {
		return getShard(key).move(key, dbIndex);
	}

	public byte[] echo(byte[] arg) {
		return getShard(arg).echo(arg);
	}

	@Deprecated
	public List<byte[]> brpop(byte[] arg) {
		return getShard(arg).brpop(arg);
	}

	@Deprecated
	public List<byte[]> blpop(byte[] arg) {
		return getShard(arg).blpop(arg);
	}

	public Long bitcount(byte[] key) {
		return getShard(key).bitcount(key);
	}

	public Long bitcount(byte[] key, long start, long end) {
		return getShard(key).bitcount(key, start, end);
	}

	public Long pfadd(final byte[] key, final byte[]... elements) {
		return getShard(key).pfadd(key, elements);
	}

	public long pfcount(final byte[] key) {
		return getShard(key).pfcount(key);
	}

	public void close() {
		if (dataSource != null) {
			boolean broken = false;

			for (MasterSlaveJedis jedis : getAllShards()) {
				if (jedis.getClient().isBroken()) {
					broken = true;
					break;
				}
			}

			if (broken) {
				dataSource.returnBrokenResource(this);
			} else {
				dataSource.returnResource(this);
			}

		} else {
			disconnect();
		}
	}

	public void setDataSource(Pool<ShardedMasterSlaveJedis> shardedJedisPool) {
		this.dataSource = shardedJedisPool;
	}

	public void resetState() {
		for (MasterSlaveJedis jedis : getAllShards()) {
			jedis.resetState();
		}
	}

	public void disconnect() {
		for (MasterSlaveJedis jedis : getAllShards()) {
			jedis.disconnect();
		}
	}

	public boolean ping() {
		boolean b = true;
		try {
			for (MasterSlaveJedis jedis : getAllShards()) {
				b = b && "PONG".equals(jedis.ping());
				if(!b){
					break;
				}
			}
		} catch (Exception e) {
			b = false;
		}
		return b;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ShardedMasterSlaveJedis@" + Integer.toHexString(hashCode()) + " ");
		sb.append("[");
		for(int i = 0, len = shards.size(); i < len; i++){
			sb.append(shards.get(i));
			if(i != len - 1){
				sb.append(", ");
			}
		}
		sb.append("]}");
		return sb.toString();
	}
	// add...

	@Override
	public String set(String key, String value, String nxxx)
	{
		return getShard(key).set(key, value, nxxx);
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp)
	{
		return getShard(key).pexpireAt(key, millisecondsTimestamp);
	}

	@Override
	public Long pttl(String key)
	{
		return getShard(key).pttl(key);
	}

	@Override
	public String psetex(String key, long milliseconds, String value)
	{
		return getShard(key).psetex(key, milliseconds, value);
	}

	@Override
	public Set<String> spop(String key, long count)
	{
		return getShard(key).spop(key, count);
	}

	@Override
	public Long zadd(String key, double score, String member, ZAddParams params)
	{
		return getShard(key).zadd(key, score, member, params);
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params)
	{
		return getShard(key).zadd(key, scoreMembers, params);
	}

	@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params)
	{
		return getShard(key).zincrby(key, score, member, params);
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min)
	{
		return getShard(key).zrevrangeByLex(key, max, min);
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count)
	{
		return getShard(key).zrevrangeByLex(key, max, min, offset, count);
	}

	@Override
	public Long bitpos(String key, boolean value)
	{
		return getShard(key).bitpos(key, value);
	}

	@Override
	public Long bitpos(String key, boolean value, BitPosParams params)
	{
		return getShard(key).bitpos(key, value, params);
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params)
	{
		return getShard(key).hscan(key, cursor, params);
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params)
	{
		return getShard(key).sscan(key, cursor, params);
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params)
	{
		return getShard(key).zscan(key, cursor, params);
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member)
	{
		return getShard(key).geoadd(key, longitude, latitude, member);
	}

	@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap)
	{
		return getShard(key).geoadd(key, memberCoordinateMap);
	}

	@Override
	public Double geodist(String key, String member1, String member2)
	{
		return getShard(key).geodist(key, member1, member2);
	}

	@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit)
	{
		return getShard(key).geodist(key, member1, member2, unit);
	}

	@Override
	public List<String> geohash(String key, String... members)
	{
		return getShard(key).geohash(key, members);
	}

	@Override
	public List<GeoCoordinate> geopos(String key, String... members)
	{
		return getShard(key).geopos(key, members);
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude,
			double radius, GeoUnit unit)
	{
		return getShard(key).georadius(key, longitude, latitude, radius, unit);
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude,
			double radius, GeoUnit unit, GeoRadiusParam param)
	{
		return getShard(key).georadius(key, longitude, latitude, radius, unit, param);
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius,
			GeoUnit unit)
	{
		return getShard(key).georadiusByMember(key, member, radius, unit);
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius,
			GeoUnit unit, GeoRadiusParam param)
	{
		return getShard(key).georadiusByMember(key, member, radius, unit, param);
	}

	@Override
	public List<Long> bitfield(String key, String... arguments)
	{
		return getShard(key).bitfield(key, arguments);
	}

	@Override
	public String set(byte[] key, byte[] value, byte[] nxxx)
	{
		return getShard(key).set(key, value, nxxx);
	}

	@Override
	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time)
	{
		return getShard(key).set(key, value, nxxx, expx, time);
	}

	@Override
	public Long pexpire(String key, long milliseconds)
	{
		return getShard(key).pexpire(key, milliseconds);
	}

	@Override
	public Long pexpire(byte[] key, long milliseconds)
	{
		return getShard(key).pexpire(key, milliseconds);
	}

	@Override
	public Long pexpireAt(byte[] key, long millisecondsTimestamp)
	{
		return getShard(key).pexpireAt(key, millisecondsTimestamp);
	}

	@Override
	public Set<byte[]> spop(byte[] key, long count)
	{
		return getShard(key).spop(key, count);
	}

	@Override
	public Long zadd(byte[] key, double score, byte[] member, ZAddParams params)
	{
		return getShard(key).zadd(key, score, member, params);
	}

	@Override
	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params)
	{
		return getShard(key).zadd(key, scoreMembers, params);
	}

	@Override
	public Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params)
	{
		return getShard(key).zincrby(key, score, member, params);
	}

	@Override
	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min)
	{
		return getShard(key).zrevrangeByLex(key, max, min);
	}

	@Override
	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count)
	{
		return getShard(key).zrevrangeByLex(key, max, min, offset, count);
	}

	@Override
	public Long geoadd(byte[] key, double longitude, double latitude, byte[] member)
	{
		return getShard(key).geoadd(key, longitude, latitude, member);
	}

	@Override
	public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap)
	{
		return getShard(key).geoadd(key, memberCoordinateMap);
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2)
	{
		return getShard(key).geodist(key, member1, member2);
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit)
	{
		return getShard(key).geodist(key, member1, member2, unit);
	}

	@Override
	public List<byte[]> geohash(byte[] key, byte[]... members)
	{
		return getShard(key).geohash(key, members);
	}

	@Override
	public List<GeoCoordinate> geopos(byte[] key, byte[]... members)
	{
		return getShard(key).geopos(key, members);
	}

	@Override
	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude,
			double radius, GeoUnit unit)
	{
		return getShard(key).georadius(key, longitude, latitude, radius, unit);
	}

	@Override
	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude,
			double radius, GeoUnit unit, GeoRadiusParam param)
	{
		return getShard(key).georadius(key, longitude, latitude, radius, unit, param);
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius,
			GeoUnit unit)
	{
		return getShard(key).georadiusByMember(key, member, radius, unit);
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius,
			GeoUnit unit, GeoRadiusParam param)
	{
		return getShard(key).georadiusByMember(key, member, radius, unit, param);
	}

	@Override
	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor)
	{
		return getShard(key).hscan(key, cursor);
	}

	@Override
	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params)
	{
		return getShard(key).hscan(key, cursor, params);
	}

	@Override
	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor)
	{
		return getShard(key).sscan(key, cursor);
	}

	@Override
	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params)
	{
		return getShard(key).sscan(key, cursor, params);
	}

	@Override
	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor)
	{
		return getShard(key).zscan(key, cursor);
	}

	@Override
	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params)
	{
		return getShard(key).zscan(key, cursor, params);
	}

	@Override
	public List<byte[]> bitfield(byte[] key, byte[]... arguments)
	{
		return getShard(key).bitfield(key, arguments);
	}
}
