package spc.webos.persistence.jdbc.datasource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.config.AppConfig;
import spc.webos.util.FTLUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class MasterSlaveDS
{
	protected String[] db;
	protected boolean master;
	protected boolean slave;
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void init() throws Exception
	{
		for (String d : db)
		{
			if (master) master(d);
			if (slave) slave(d);
		}
	}

	public void slave(String db) throws Exception
	{
		String beanJT = db + "SlaveJT";
		try
		{
			if (SpringUtil.getInstance().getBean(beanJT) != null)
			{
				log.info("ms db bean:{} exists", beanJT);
				return;
			}
		}
		catch (Exception e)
		{
		}
		int num = AppConfig.getInstance().getProperty("jdbc." + db + ".slave.num", -1);
		log.info("slave ds:{}, num:{}", db, num);
		if (num < 0) return;
		Map<String, Object> root = new HashMap<>();
		root.put("db", db);
		root.put("beanJT", beanJT);
		String beans = FTLUtil.ftl("webos/slave_ds", root);
		log.debug("slave ds:{}, beans:\n{}", db, beans);
		SpringUtil.registerXMLBean(beans, false);
	}

	public void master(String db) throws Exception
	{
		String beanJT = db + "MasterRJT";
		try
		{
			if (SpringUtil.getInstance().getBean(beanJT) != null)
			{
				log.info("ms db bean:{} exists", beanJT);
				return;
			}
		}
		catch (Exception e)
		{
		}
		String username = AppConfig.getInstance()
				.getProperty("jdbc." + db + ".master.read.username", "");
		log.info("master read ds:{}, user:{}", db, username);
		if (StringX.nullity(username)) return;
		Map<String, Object> root = new HashMap<>();
		root.put("db", db);
		root.put("beanJT", beanJT);
		String beans = FTLUtil.ftl("webos/masterread_ds", root);
		log.debug("master read ds:{}", beans);
		SpringUtil.registerXMLBean(beans, false);
	}

	public void setDb(String[] db)
	{
		this.db = db;
	}

	public void setMaster(boolean master)
	{
		this.master = master;
	}

	public void setSlave(boolean slave)
	{
		this.slave = slave;
	}
}
