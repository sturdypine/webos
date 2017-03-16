package spc.webos.service.job.impl;

import java.util.HashMap;
import java.util.Map;

import spc.webos.bpl.mule.MuleEngine;
import spc.webos.exception.AppException;
import spc.webos.service.job.MasterSlaveJobService;
import spc.webos.util.StringX;

public class MuleJobServiceImpl extends LeaderLatchMSJobServiceImpl
{
	protected String key; // 存放启动参数配置信息的key值
	protected Map<String, Object> params = new HashMap<>(); // 启动流程的默认参数
	protected String process; // 流程名
	protected MuleEngine mule = MuleEngine.getInstance();

	@Override
	public void init() throws Exception
	{
		if (StringX.nullity(name)) name = process;
		super.init();
	}

	@Override
	public void execute() throws AppException
	{
		((MasterSlaveJobService) self).execute(false, config.getProperty(key, params));
	}

	@Override
	public void execute(boolean force, Map<String, Object> params) throws AppException
	{
		log.info("mule: force:{}, job:{}, params:{}", force, name, params);
		try
		{
			mule.call(process, params);
		}
		catch (Exception e)
		{
			log.warn("fail to call:" + process, e);
		}
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public void setParams(Map<String, Object> params)
	{
		this.params = params;
	}

	public void setProcess(String process)
	{
		this.process = process;
	}

	public void setMule(MuleEngine mule)
	{
		this.mule = mule;
	}
}
