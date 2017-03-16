package spc.webos.service;

import java.util.HashMap;
import java.util.Map;

import spc.webos.advice.log.LogTrace;

public interface Status
{
	// 服务状态接口，提供参数选择，这样可以获得组件关心的部分状态信息
	@LogTrace
	Map checkStatus(Map param);

	@LogTrace
	boolean changeStatus(Map param);

	@LogTrace
	void refresh() throws Exception;
	
	boolean needRefresh();

	static Map<String, Status> SERVICES_PROXY = new HashMap<>(); // spring代理后对象

	static Map<String, Status> SERVICES = new HashMap<>(); // 系统所有服务
}
