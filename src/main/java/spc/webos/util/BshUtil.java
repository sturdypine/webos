package spc.webos.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.Interpreter;
import spc.webos.model.BshPO;
import spc.webos.persistence.IPersistence;

public class BshUtil
{
	final static Logger log = LoggerFactory.getLogger(BshUtil.class);
	static BshUtil util = new BshUtil();

	public static BshUtil getInstance()
	{
		return util;
	}

	protected String script;

	@PostConstruct
	public void init() throws Exception
	{
		log.info("Bsh init script:{}", script);
		BshUtil.bsh(script, null);
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	// bsh start...
	Map bshCache = new HashMap();

	public void setBshCache(Map cache)
	{
		bshCache = (cache == null ? new HashMap() : cache);
	}

	public Map loadBshInDB(IPersistence persistence) throws Exception
	{
		Map cache = new HashMap();
		List bshs = null;
		try
		{
			bshs = persistence.get(new BshPO());
		}
		catch (Exception e)
		{
			log.info("fail to load Bsh in DB:" + e);
		}
		if (bshs == null) return cache;
		for (int i = 0; i < bshs.size(); i++)
		{
			BshPO vo = (BshPO) bshs.get(i);
			Interpreter inter = new Interpreter();
			inter.eval("Object bsh(java.util.Map params){" + vo.getBsh() + "}");
			cache.put(vo.getId(), inter);
		}
		if (log.isInfoEnabled()) log.info("Bsh in DB: " + cache.keySet());
		setBshCache(cache);
		return cache;
	}

	public static Object exeBsh(String id, Map params) throws Exception
	{
		Interpreter inter = (Interpreter) getInstance().bshCache.get(id);
		if (inter == null) return null;
		synchronized (inter)
		{ // 702_20140121保证线程安全
			inter.set("params", params);
			inter.eval("Object ret = bsh(params);");
			return inter.get("ret");
		}
	}

	public static Interpreter bsh(String script, Map params) throws Exception
	{
		return bsh(new Interpreter(), script, params);
	}

	public static Interpreter bsh(Interpreter inter, String script, Map params) throws Exception
	{
		inter.setClassLoader(Thread.currentThread().getContextClassLoader());
		if (params != null)
		{
			Iterator keys = params.keySet().iterator();
			while (keys.hasNext())
			{
				String key = keys.next().toString();
				inter.set(key, params.get(key));
			}
		}
		if (!StringX.nullity(script)) inter.eval(script);
		return inter;
	}
	// bsh end...
}
