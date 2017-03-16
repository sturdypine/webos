package spc.webos.bpl.jbpm3;

import spc.webos.cache.MapCache;

public class ServiceCallback
{
	MapCache cache = new MapCache();

	public void put(String corId, Object v) throws Exception
	{
		cache.put(corId, v);
	}

	public void setCache(MapCache cache)
	{
		this.cache = cache;
	}
}
