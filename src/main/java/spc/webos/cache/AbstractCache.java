package spc.webos.cache;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCache<K, V> implements Cache<K, V>
{
	String name;
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected int sleepSeconds = 60;
	protected boolean runEvictExpired = false;
	protected Thread daemon;

	public void setName(String name)
	{
		this.name = name;
	}

	public void init() throws Exception
	{
		if (getName() != null) CACHES.put(getName(), this);
		if (!runEvictExpired) return;
		daemon = new Thread(() -> {
			log.info("start daemon to runEvictExpired...");
			while (runEvictExpired)
			{
				try
				{
					Thread.sleep(sleepSeconds * 1000);
				}
				catch (Exception e)
				{
				}
				try
				{
					log.info("evictExpired...");
					evictExpiredElements();
				}
				catch (Exception e)
				{
				}
			}
			log.info("runEvictExpired daemon stop...");
		});
		daemon.setDaemon(true);
		daemon.start();
	}

	@PreDestroy
	public void destroy()
	{
		runEvictExpired = false;
		try
		{
			if (daemon != null) daemon.interrupt();
		}
		catch (Exception e)
		{
		}
		try
		{
			if (daemon != null) daemon.stop();
		}
		catch (Exception e)
		{
		}
	}

	public synchronized V poll(K key, long timeout) throws Exception
	{
		return poll(key, new CacheWaitWithTime(key, timeout));
	}

	public synchronized V poll(K key, WaitWithTime wwt) throws Exception
	{
		wwt.setTarget(this);
		V v = null;
		while (v == null)
		{
			while (wwt.condition())
				wwt.timeWait();
			v = get(key);
			remove(key);
		}
		return v;
	}

	public V poll(K key) throws Exception
	{
		V v = get(key);
		remove(key);
		return v;
	}

	public String getName()
	{
		return name;
	}

	public boolean changeStatus(Map param)
	{
		return false;
	}

	public Map checkStatus(Map param)
	{
		Map status = new HashMap();
		status.put("name", name);
		status.put("clazz", getClass());
		return status;
	}

	public void refresh() throws Exception
	{
	}

	public void evictExpiredElements()
	{
	}
}
