package spc.webos.service.job.impl;

import java.util.List;
import java.util.Map;

import spc.webos.exception.AppException;
import spc.webos.service.job.JobService;

/**
 * 批量线性执行任务，将多个小任务线性执行，防止并发过大
 * 
 * @author chenjs
 *
 */
public class BatchJobServiceImpl extends LeaderLatchMSJobServiceImpl
{
	protected List<JobService> batchJobs;

	@Override
	public void execute() throws AppException
	{
		batchJobs.forEach((job) -> {
			try
			{
				log.info("start job:{}", job.getName());
				job.execute();
			}
			catch (Throwable t)
			{
				log.warn("fail to run job:" + job.getName(), t);
			}
		});
	}

	@Override
	public void execute(boolean force, Map<String, Object> params) throws AppException
	{
		batchJobs.forEach((job) -> {
			try
			{
				log.info("start job:{}", job.getName());
				job.execute(force, params);
			}
			catch (Throwable t)
			{
				log.warn("fail to run job:" + job.getName(), t);
			}
		});
	}

	public void setBatchJobs(List<JobService> batchJobs)
	{
		this.batchJobs = batchJobs;
	}
}
