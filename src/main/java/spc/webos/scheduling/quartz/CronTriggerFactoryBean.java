package spc.webos.scheduling.quartz;

import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.config.AppConfig;
import spc.webos.service.Status;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

/**
 * 支持动态调整后台作业运行时间
 * 
 * @author chenjs
 *
 */
public class CronTriggerFactoryBean
		extends org.springframework.scheduling.quartz.CronTriggerFactoryBean implements Status
{
	protected String scheduler;
	protected String confKey;
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public Scheduler getScheduler()
	{
		return SpringUtil.APPCXT.getBean(scheduler, Scheduler.class);
	}

	public void setScheduler(String scheduler)
	{
		this.scheduler = scheduler;
	}

	public void setConfKey(String confKey)
	{
		this.confKey = confKey;
	}

	public String getConfKey()
	{
		return confKey;
	}

	@Override
	public Map checkStatus(Map param)
	{
		return null;
	}

	@Override
	public boolean changeStatus(Map param)
	{
		return false;
	}

	@Override
	public void refresh() throws Exception
	{
		CronTriggerImpl trigger = (CronTriggerImpl) getObject();
		String cron = (String) AppConfig.getInstance().getProperty(confKey, null);
		try
		{
			log.info("rescheduleJob {}, cron from:{}, to:{}", trigger.getKey(),
					trigger.getCronExpression(), cron);
			trigger.setCronExpression(cron);
			getScheduler().rescheduleJob(trigger.getKey(), trigger);
		}
		catch (Exception e)
		{
			log.warn("fail to rescheduleJob:" + trigger.getKey() + ":" + cron, e);
		}
	}

	@Override
	public boolean needRefresh()
	{
		String cron = (String) AppConfig.getInstance().getProperty(confKey, null);
		CronTriggerImpl trigger = (CronTriggerImpl) getObject();
		if (getScheduler() == null || StringX.nullity(cron)
				|| cron.equalsIgnoreCase(trigger.getCronExpression()))
			return false;
		return true;
	}
}
