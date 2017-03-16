package spc.webos.service.job;

import java.util.Map;

import spc.webos.advice.log.LogTrace;
import spc.webos.exception.AppException;

public interface JobService
{
	String getName();
	
	@LogTrace
	void execute() throws AppException;

	@LogTrace
	void execute(boolean force, Map<String, Object> params);
}
