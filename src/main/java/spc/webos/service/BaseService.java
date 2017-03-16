package spc.webos.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import spc.webos.bean.BeanSelfAware;
import spc.webos.config.AppConfig;
import spc.webos.config.Config;
import spc.webos.mq.MQ;
import spc.webos.persistence.IPersistence;
import spc.webos.redis.JedisTemplate;
import spc.webos.service.seq.UUID;
import spc.webos.util.StringX;

public class BaseService implements BeanSelfAware, Status
{
	@Autowired(required = false)
	protected IPersistence persistence;
	@Autowired(required = false)
	protected UUID uuid;
	@Autowired(required = false)
	protected Config config = AppConfig.getInstance();
	protected String name;
	protected String remark;
	protected Status self = this;
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private String lastVersion = StringX.EMPTY_STRING;
	protected String versionKey; //

	@Autowired(required = false)
	protected MQ mq;

	@Autowired(required = false)
	protected JedisTemplate jedis;

	@PostConstruct
	public void init() throws Exception
	{
		if (!StringX.nullity(name))
		{
			if (SERVICES.containsKey(name)) log.warn(name + " Service existed in SERVICES!!!");
			SERVICES.put(name, this);
			SERVICES_PROXY.put(name, self);
		}
		if (!StringX.nullity(versionKey))
		{
			lastVersion = (String) config.getProperty(versionKey, "");
			log.info("lastVersion:{}", lastVersion);
		}
		refresh();
	}

	@PreDestroy // spring context会自动关闭AutoCloseable
	public void destroy()
	{
	}

	public IPersistence getPersistence()
	{
		return persistence;
	}

	public void setPersistence(IPersistence persistence)
	{
		this.persistence = persistence;
	}

	public boolean changeStatus(Map param)
	{
		return false;
	}

	public Map checkStatus(Map param)
	{
		return new HashMap();
	}

	public void refresh() throws Exception
	{
	}

	// 服务刷新时判断服务是否需要刷新数据库
	public boolean needRefresh()
	{
		if (StringX.nullity(versionKey))
		{
			log.debug("dbVerDtKey is null!!!");
			return false;
		}

		String curVersion = config.getProperty(versionKey, StringX.EMPTY_STRING);
		if (StringX.nullity(curVersion) || lastVersion.equalsIgnoreCase(curVersion))
		{
			log.debug("no refresh key:{}, lastVersion:{}={}", versionKey, lastVersion, curVersion);
			return false;
		}
		log.info("refresh key:{}, lastVersion:{} != {}", versionKey, lastVersion, curVersion);
		lastVersion = curVersion;
		return true;
	}

	public void self(Object proxyBean)
	{
		self = (Status) proxyBean;
		// if (StringX.nullity(name) || self == null) return;
		// SERVICES_PROXY.put(name, self);
		// if (!SERVICES.containsKey(name)) SERVICES.put(name, this);
	}

	public Object self()
	{
		return self;
	}

	public String getLastVersion()
	{
		return lastVersion;
	}

	public void setLastVersion(String lastVersion)
	{
		this.lastVersion = lastVersion;
	}

	public String getVersionKey()
	{
		return versionKey;
	}

	public void setVersionKey(String versionKey)
	{
		this.versionKey = versionKey;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setConfig(Config config)
	{
		this.config = config;
	}

	public void setMq(MQ mq)
	{
		this.mq = mq;
	}

	public void setJedis(JedisTemplate jedis)
	{
		this.jedis = jedis;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}
}
