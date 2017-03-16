package spc.webos.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCache<K, V> extends AbstractCache<K, V>
{
	protected Map<K, Item<V>> map = new ConcurrentHashMap<>();
	protected int expire = -1;
	protected long maxCheckInterval = 120000;
	protected long minCheckInterval = 30000;
	protected volatile long lastCheckTm;
	protected int threshold = 200; // 713_20140924 默认值从100提高到200应对前端有200个并发情况

	public MapCache()
	{
	}

	public MapCache(int threshold, int expire)
	{
		this.threshold = threshold;
		this.expire = expire;
	}

	public MapCache(int expire, int threshold, long minCheckInterval, long maxCheckInterval)
	{
		this.expire = expire;
		this.threshold = threshold;
		this.minCheckInterval = minCheckInterval;
		this.maxCheckInterval = maxCheckInterval;
	}

	public Map checkStatus(Map param)
	{
		return null;
	}

	public boolean changeStatus(Map param)
	{
		return false;
	}

	public void refresh() throws Exception
	{
	}

	public boolean containsKey(String key)
	{
		return map.containsKey(key);
	}

	public Collection<K> getKeys()
	{
		return map.keySet();
	}

	public int size()
	{
		return map.size();
	}

	public V get(K key)
	{
		Item<V> item = map.get(key);
		if (item == null) return null;
		long cur = System.currentTimeMillis();
		if (item.isExpire(cur))
		{
			log.info("expired: {}={},{}", key, item.createTm, item.expire);
			map.remove(key);
			return null;
		}
		return item.o;
	}

	public V put(K key, V o)
	{
		evictExpiredElements();
		map.put(key, new Item<>(o, this.expire));
		return o;
	}

	public V put(K key, V o, int expire)
	{
		evictExpiredElements();
		map.put(key, new Item<>(o, expire));
		return o;
	}

	public void removeAll()
	{
		map.clear();
	}

	public V remove(K k)
	{
		V v = get(k);
		map.remove(k);
		return v;
	}

	public void evictExpiredElements()
	{
		long cur = System.currentTimeMillis();
		long interval = cur - lastCheckTm;
		int size = map.size();
		if (size == 0 || ((interval < maxCheckInterval)
				&& (size <= threshold || interval < minCheckInterval)))
			return;
		log.info("evict expired, size:{}, threshold:{}", size, threshold);
		lastCheckTm = cur;
		map.forEach((k, v) -> {
			if (v.isExpire(cur))
			{
				log.info("expired: {}={},{}", k, v.createTm, v.expire);
				map.remove(k);
				try
				{
					if (v.o instanceof AutoCloseable) ((AutoCloseable) (v.o)).close();
				}
				catch (Exception e)
				{
					log.warn("fail to auto close:" + e);
				}
			}
		});
	}

	public void destroy()
	{
		super.destroy();
		map.forEach((k, v) -> {
			try
			{
				if (v.o instanceof AutoCloseable) ((AutoCloseable) (v.o)).close();
			}
			catch (Exception e)
			{
			}
		});
		map.clear();
	}

	public int getThreshold()
	{
		return threshold;
	}

	public void setThreshold(int threshold)
	{
		this.threshold = threshold;
	}

	public boolean needRefresh()
	{
		return false;
	}

	public int getExpire()
	{
		return expire;
	}

	public void setExpire(int expire)
	{
		this.expire = expire;
	}

	public final class Item<T> implements java.io.Serializable
	{
		private static final long serialVersionUID = 1L;
		public T o;
		public long createTm;
		public int expire = -1;

		public Item(T o)
		{
			this.o = o;
			createTm = System.currentTimeMillis();
		}

		public Item(T o, int expire)
		{
			this.o = o;
			createTm = System.currentTimeMillis();
			this.expire = expire;
		}

		protected boolean isExpire(long cur)
		{
			if (expire > 0 && cur - createTm > expire * 1000) return true;
			return false;
		}
	}
}
