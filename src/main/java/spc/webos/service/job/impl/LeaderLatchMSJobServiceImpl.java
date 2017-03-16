package spc.webos.service.job.impl;

import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import spc.webos.advice.log.LogTrace;
import spc.webos.exception.AppException;
import spc.webos.service.ZKService;
import spc.webos.service.job.MasterSlaveJobService;
import spc.webos.util.StringX;

/**
 * 通过zookeeper来确定主从模式后台任务
 * 
 * @author chenjs
 *
 */
@Service("leaderLatchMSJobService")
public class LeaderLatchMSJobServiceImpl extends ZKService implements MasterSlaveJobService
{
	public LeaderLatchMSJobServiceImpl()
	{
	}

	protected LeaderLatchMSJobServiceImpl(String zkHost, String zkPath)
	{
		this();
		this.zkHost = zkHost;
		this.zkPath = zkPath;
	}

	protected LeaderLatchMSJobServiceImpl(String zkPath)
	{
		this();
		this.zkPath = zkPath;
	}

	@Override
	public void init() throws Exception
	{
		if (StringX.nullity(name)) throw new Exception("LLMSJob name is null!!!");
		if (StringX.nullity(zkPath)) zkPath = "/Job/LL/" + name;
		super.init();
	}

	@LogTrace(location = "lljob.leader")
	public void leader() throws Exception
	{
		int wait = maxWaitMillis > 0 ? new Random().nextInt(maxWaitMillis) : 0;
		log.info("invoke job: {}, maxWait:{}, wait:{}, minInterval:{}", name, maxWaitMillis, wait,
				minInterval);
		if (wait > 0) Thread.sleep(wait);
		super.syn(concurrent, (l) -> {
			((MasterSlaveJobService) self).execute();
		});
	}

	@LogTrace(location = "lljob.leader")
	public boolean leader(boolean force, boolean concurrent, Map<String, Object> params)
			throws Exception
	{
		log.info("invoke job: {}, force:{}", name, force);
		return super.syn(concurrent, (l) -> {
			((MasterSlaveJobService) self).execute(force, params);
		});
	}

	@Override
	protected boolean isExe(Map<String, Object> value)
	{
		value.put("minIntervalSec", minInterval);
		value.put("redo", redo);
		if (minInterval <= 0) return true;
		String status = (String) value.get("status");
		String endMillis = StringX.null2emptystr(value.get("endMillis"));
		if (StringX.nullity(status)
				|| !status.equalsIgnoreCase(spc.webos.exception.Status.STATUS_SUCCESS)
				|| StringX.nullity(endMillis))
			return true;
		long cur = System.currentTimeMillis();
		boolean exe = cur - Long.parseLong(endMillis) > minInterval * 1000;
		log.info("cur:{}, endMillis:{}, minIntervalSec:{}, exe:{}", cur, endMillis, minInterval,
				exe);
		return exe;
	}

	@Override
	public void execute() throws AppException
	{
		((MasterSlaveJobService) self).execute(false, null);
	}

	@Override
	public void execute(boolean force, Map<String, Object> params) throws AppException
	{
		log.info("execute: force:{}, job:{}, params:{}", force, name, params);
	}

	protected boolean redo = true; // 是否支持任务重新执行
	protected int minInterval = 120; // 集群任务执行的最小间隔时间，默认120s
	protected int maxWaitMillis = 3000; // 为了让任务在各机器均衡(考虑到机器时间差异), 触发后等待一定时间

	public void setMinInterval(int minInterval)
	{
		this.minInterval = minInterval;
	}

	public void setMaxWaitMillis(int maxWaitMillis)
	{
		this.maxWaitMillis = maxWaitMillis;
	}

	public boolean isRedo()
	{
		return redo;
	}

	public void setRedo(boolean redo)
	{
		this.redo = redo;
	}
}
