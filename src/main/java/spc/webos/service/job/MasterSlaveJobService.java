package spc.webos.service.job;

import java.util.Map;

import spc.webos.advice.log.LogTrace;

public interface MasterSlaveJobService extends JobService
{
	@LogTrace
	void leader() throws Exception;

	@LogTrace
	boolean leader(boolean force, boolean concurrent, Map<String, Object> params) throws Exception;
}
