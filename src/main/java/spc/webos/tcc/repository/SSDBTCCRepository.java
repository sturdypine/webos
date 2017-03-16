package spc.webos.tcc.repository;

import java.io.Closeable;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import redis.clients.jedis.JedisCommands;
import redis.clients.util.Pool;
import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.tcc.Transaction;

public class SSDBTCCRepository extends AbstractTCCRepository
{
	@Resource
	protected Pool ssdbPool;

	protected String prefix = "tcc:"; // ssdb key prefix
	protected String tccPrefixKey;
	protected String failTccKey; // fail
	protected String allTccKey; // all

	@PostConstruct
	public void init() throws Exception
	{
		tccPrefixKey = prefix + group + ":";
		failTccKey = prefix + group + ":fail";
		allTccKey = prefix + group + ":all";
		super.init();
	}

	protected Closeable getJedis()
	{
		return (Closeable) ssdbPool.getResource();
	}

	public Transaction create(final Transaction transaction) throws Exception
	{
		if (repositoryOnlyErr) return transaction;
		final String key = tccPrefixKey + transaction.xid;

		try (Closeable jedis = getJedis())
		{
			if (((JedisCommands) jedis).exists(key))
				throw new AppException(AppRetCode.TCC_XID_REPEAT, new Object[] { transaction.xid });

			((JedisCommands) jedis).hmset(key, transaction.toMap());
			((JedisCommands) jedis).sadd(allTccKey, transaction.xid);
			return transaction;
		}
	}

	public Transaction updateStatus(final Transaction transaction)
	{
		try (Closeable jedis = getJedis())
		{
			if (transaction.status == Transaction.STATUS_CANCEL_FAIL
					|| transaction.status == Transaction.STATUS_CONFIRM_FAIL)
				((JedisCommands) jedis).sadd(failTccKey, transaction.xid);
			((JedisCommands) jedis).hmset(tccPrefixKey + transaction.xid, transaction.toMap());
		}
		catch (Exception e)
		{
			log.warn("TX fail update:" + transaction.xid + ", status:" + transaction.status, e);
		}
		return transaction;
	}

	public Transaction delete(final Transaction transaction, boolean byFail) throws Exception
	{
		if (repositoryOnlyErr && !byFail) return transaction;
		try (Closeable jedis = getJedis())
		{
			((JedisCommands) jedis).srem(failTccKey, transaction.xid);
			((JedisCommands) jedis).srem(allTccKey, transaction.xid);
			if (sucTccExpireSeconds <= 0)
				((JedisCommands) jedis).del(tccPrefixKey + transaction.xid);
			else
			{
				updateStatus(transaction);
				((JedisCommands) jedis).expire(tccPrefixKey + transaction.xid, sucTccExpireSeconds);
			}
		}
		return transaction;
	}

	public Transaction find(String xid) throws Exception
	{
		try (Closeable jedis = getJedis())
		{
			return new Transaction(((JedisCommands) jedis).hgetAll(tccPrefixKey + xid));
		}
	}

	public Collection<String> findAll(int from, int limit) throws AppException
	{
		try (Closeable jedis = getJedis())
		{
			return ((JedisCommands) jedis).smembers(allTccKey);
		}
		catch (Exception e)
		{
			throw new AppException(AppRetCode.CMM_BIZ_ERR, e);
		}
	}

	public Collection<String> findErr(int from, int limit) throws AppException
	{
		try (Closeable jedis = getJedis())
		{
			return ((JedisCommands) jedis).smembers(failTccKey);
		}
		catch (Exception e)
		{
			throw new AppException(AppRetCode.CMM_BIZ_ERR, e);
		}
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public void setSsdbPool(Pool ssdbPool)
	{
		this.ssdbPool = ssdbPool;
	}
}
