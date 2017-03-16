package spc.webos.web.common;

public class TransientInSession<T> implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	public long lastVisitTimeMillis;
	public T value;
	public int cacheSeconds = -1;

	public TransientInSession(T value)
	{
		this.value = value;
	}

	public TransientInSession(T value, int cacheSeconds)
	{
		this.value = value;
		this.cacheSeconds = cacheSeconds;
		lastVisitTimeMillis = System.currentTimeMillis();
	}

	public T value()
	{
		if (cacheSeconds > 0) lastVisitTimeMillis = System.currentTimeMillis();
		return value;
	}

	public void value(T value)
	{
		if (cacheSeconds > 0) lastVisitTimeMillis = System.currentTimeMillis();
		this.value = value;
	}

	public boolean isExpired(long currentTimeMillis)
	{
		return cacheSeconds >= 0 && currentTimeMillis > lastVisitTimeMillis + cacheSeconds * 1000;
	}
}
