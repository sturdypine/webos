package spc.webos.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import spc.webos.bean.BeanSelfAware;
import spc.webos.model.ConfigPO;
import spc.webos.persistence.IPersistence;
import spc.webos.service.Status;
import spc.webos.util.JsonUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class AppConfig implements BeanSelfAware, Config
{
	private static AppConfig APP_CFG = new AppConfig();
	@Autowired(required = false)
	protected IPersistence persistence;
	protected Config self = this;
	@Autowired(required = false)
	protected PropertyConfigurer propertyConfigurer;
	protected Map staticCfg = new HashMap();
	protected volatile Map config = new HashMap();
	protected volatile boolean run = true;
	protected Thread daemon;
	private static boolean PRODUCT = true;
	private boolean init;
	protected Logger log = LoggerFactory.getLogger(getClass());
	public static final String DELIM = ".";

	public Map getConfig()
	{
		return config;
	}

	public void setProperty(String key, Object value)
	{
		config.put(key, value);
	}

	public <T> T getProperty(String key, boolean jvm, T defValue)
	{
		Object value = null;
		if (jvm) value = config.get(key + '.' + SpringUtil.APPCODE + '.' + SpringUtil.JVM);
		if (value != null) return value(value, defValue);
		value = config.get(key + '.' + SpringUtil.APPCODE);
		if (value != null) return value(value, defValue);
		return getProperty(key, defValue);
	}

	public <T> T getProperty(String[] keys, T defValue)
	{
		for (String key : keys)
		{
			Object v = config.get(key);
			if (v != null) return value(v, defValue);
		}
		return defValue;
	}

	public <T> T getProperty(String key, T defValue)
	{
		if (StringX.nullity(key)) return defValue;
		return value(config.get(key), defValue);
	}

	protected <T> T value(Object value, T defValue)
	{
		if (value == null) return defValue;
		if (defValue == null || value.getClass() == defValue.getClass()) return (T) value;
		if (defValue instanceof Integer) return (T) new Integer(value.toString());
		else if (defValue instanceof Long) return (T) new Long(value.toString());
		else if (defValue instanceof Boolean) return (T) new Boolean(value.toString());
		else if (defValue instanceof Double) return (T) new Double(value.toString());
		return (T) value;
	}

	protected <T> T getProperty(Map map, String key, T defValue)
	{
		if (map == null || StringX.nullity(key)) return defValue;
		T value = defValue;
		int index = key.indexOf(DELIM);
		if (index < 0)
		{
			value = (T) map.get(key);
			return value == null ? defValue : value;
		}
		String path = key.substring(0, index);
		key = key.substring(index + 1);
		return getProperty((Map) map.get(path), key, defValue);
	}

	@PostConstruct
	public void init() throws Exception
	{
		if (init) return;
		init = true;
		staticCfg = new HashMap();
		config = new HashMap();
		if (propertyConfigurer != null)
		{
			staticCfg = propertyConfigurer.getProperties();
			log.info("loading static properties: {}", staticCfg.size());
		}
		reload();
		if (!run) return;
		log.info("config thread start");
		daemon = new Thread(() -> {
			try
			{
				while (run)
				{
					Thread.sleep(getProperty(spc.webos.constant.Config.app_config_repeatInterval,
							true, 60000));
					self.refresh();
				}
			}
			catch (Exception e)
			{
				log.warn("config thread fail", e);
			}
			log.info("config thread stopped...");
		});
		daemon.setDaemon(true);
		daemon.start();
	}

	@PreDestroy
	public void destroy()
	{
		log.info("stop config refresh...");
		run = false;
		daemon.interrupt();
	}

	/**
	 * 刷新当前数据配置表，然后刷新容器内所有status组件
	 */
	public void refresh()
	{
		reload();
		Map<String, Status> statuses = SpringUtil.APPCXT.getBeansOfType(Status.class);
		log.info("All status:{}, All cfg:{}, static cfg:{}", statuses == null ? 0 : statuses.size(),
				config.size(), staticCfg.size());
		if (statuses == null) return;
		statuses.forEach((n, s) -> {
			try
			{
				if (!s.needRefresh()) return;
				log.info("status refresh:{}", n);
				s.refresh();
			}
			catch (Exception e)
			{
				log.info("Fail to refresh:{}, class:{}", n, s.getClass());
			}
		});
	}

	protected void reload()
	{
		try
		{
			log.debug("reload config, static:{}", (staticCfg == null ? 0 : staticCfg.size()));
			Map cfg = new HashMap(staticCfg);
			try
			{
				cfg.putAll(loadDBCfg());
			}
			catch (Exception e)
			{
				log.info("fail to load ConfigPO:" + e);
			}

			this.config = cfg;
		}
		catch (Exception e)
		{
			log.warn("AppConfig.reload: " + e);
			log.debug("AppConfig.reload", e);
		}
		log.debug("config size:{}", config.size());
		log.debug("config:{}", config);
	}

	public static AppConfig getInstance()
	{
		return APP_CFG;
	}

	protected Map loadDBCfg()
	{
		if (persistence == null) return null;
		ConfigPO vo = new ConfigPO();
		vo.setStatus("1");
		List<ConfigPO> list = persistence.get(vo);
		if (list == null || list.size() == 0)
		{
			log.warn("db config has no data!!!");
			return null;
		}
		log.debug("db cfg items:{}", list.size());
		Map<String, Object> dbcfg = new HashMap<>();
		list.forEach((item) -> {
			dbcfg.put(item.getCode(), value(item));
		});
		log.debug("loadDBCfg: {}", dbcfg);
		return dbcfg;
	}

	protected Object value(ConfigPO item)
	{
		Object value = item.getVal().toString().trim();
		if (StringX.nullity(item.getModel())
				|| ConfigPO.MD_SIMPLE.equalsIgnoreCase(item.getModel()))
			value = processSimple(value.toString(), item);
		else if (ConfigPO.MD_ARRAY.equalsIgnoreCase(item.getModel()))
			value = proccessArray(value.toString(), item);
		else if (ConfigPO.MD_JSON.equalsIgnoreCase(item.getModel()))
			value = processJson(value.toString(), item);
		else
		{
			log.warn("undefined model: " + item.getModel() + ", using default simple model...");
			value = processSimple(value.toString(), item);
		}
		return value;
	}

	public static void setAll(Map m1, Map m2)
	{
		if (m2 == null || m1 == null) return;
		m1.putAll(m2);
		// Iterator keys = m2.keySet().iterator();
		// while (keys.hasNext())
		// {
		// String key = keys.next().toString();
		// Object o = m1.get(key);
		// if (o == null) m1.put(key, m2.get(key));
		// else setAll((Map) m1.get(key), (Map) m2.get(key));
		// }
	}

	public static void setPathInMap(Map m, String path, Object value)
	{
		String[] paths = StringX.split(path, DELIM);
		for (int i = 0; i < paths.length - 1; i++)
		{
			Object t = m.get(paths[i]);
			if (t == null || !(t instanceof Map))
			{
				t = new HashMap();
				m.put(paths[i], t);
			}
			m = (Map) t;
		}
		m.put(paths[paths.length - 1], value);
	}

	protected Object processJson(String value, ConfigPO item)
	{
		if (StringX.nullity(value)) return null;
		return JsonUtil.json2obj(value);
	}

	protected Object proccessArray(String value, ConfigPO item)
	{
		String[] arr = value.split(StringX.COMMA);
		List result = new ArrayList(arr.length);
		// for ( int i = 0; i < arr.length; i++)
		// System.out.println(arr[i]);
		for (int i = 0; i < arr.length; i++)
		{
			if (ConfigPO.TP_STRING.equalsIgnoreCase(item.getDtype())) result.add(arr[i]);
			else if (ConfigPO.TP_INT.equalsIgnoreCase(item.getDtype()))
				result.add(new Integer(arr[i]));
			else if (ConfigPO.TP_LONG.equalsIgnoreCase(item.getDtype()))
				result.add(new Long(arr[i]));
			else if (ConfigPO.TP_BOOL.equalsIgnoreCase(item.getDtype())
					|| ConfigPO.TP_BOOLEAN.equalsIgnoreCase(item.getDtype()))
				result.add(new Boolean(arr[i].equalsIgnoreCase(Boolean.TRUE.toString())));
			else if (ConfigPO.TP_DOUBLE.equalsIgnoreCase(item.getDtype()))
				result.add(new Double(arr[i]));
			else result.add(arr[i]);
		}
		return result;
	}

	protected Object processSimple(String value, ConfigPO item)
	{
		if (ConfigPO.TP_STRING.equalsIgnoreCase(item.getDtype())) return value;
		else if (ConfigPO.TP_BOOL.equalsIgnoreCase(item.getDtype())
				|| ConfigPO.TP_BOOLEAN.equalsIgnoreCase(item.getDtype()))
			return new Boolean(Boolean.TRUE.toString().equalsIgnoreCase(value));
		else if (ConfigPO.TP_INT.equalsIgnoreCase(item.getDtype())) return new Integer(value);
		else if (ConfigPO.TP_LONG.equalsIgnoreCase(item.getDtype())) return new Long(value);
		else if (ConfigPO.TP_DOUBLE.equalsIgnoreCase(item.getDtype())) return new Double(value);
		return value;
	}

	public void setPersistence(IPersistence persistence)
	{
		this.persistence = persistence;
	}

	public static boolean isProductMode()
	{
		return PRODUCT;
	}

	public boolean isProduct()
	{
		return PRODUCT;
	}

	public void setProduct(boolean product)
	{
		PRODUCT = product;
	}

	public Map getStaticCfg()
	{
		return staticCfg;
	}

	public void setInStaticCfg(String key, Object value)
	{
		this.staticCfg.put(key, value);
		this.config.putAll(this.staticCfg);
	}

	public void setStaticCfg(Map staticCfg)
	{
		this.staticCfg.putAll(staticCfg);
	}

	public void setRun(boolean run)
	{
		this.run = run;
	}

	@Override
	public void self(Object proxyBean)
	{
		self = (Config) proxyBean;
	}

	@Override
	public Object self()
	{
		return self;
	}
}
