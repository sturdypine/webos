package spc.webos.cache;

public class CacheWaitWithTime extends WaitWithTime
{
	Object key;

	public CacheWaitWithTime(Object key, long timeout)
	{
		this.key = key;
		this.timeout = timeout;
	}

	public boolean condition()
	{
		try
		{
			return ((Cache) target).get(key) == null;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
