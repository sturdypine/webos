package spc.webos.bpl;

import java.util.Map;

import spc.webos.service.BaseService;
import spc.webos.util.POJOUtil;

public abstract class AbstractEngine extends BaseService implements Engine
{
	public <T> T call(String process, Map<String, Object> params, T pojo) throws Exception
	{
		return POJOUtil.map2pojo(call(process, params), pojo);
	}

	public <T> T call(String process, Map<String, Object> params, T pojo, String[] properties)
			throws Exception
	{
		return POJOUtil.map2pojo(call(process, params), pojo, properties);
	}

	public <T> T call(String process, Map<String, Object> params, T pojo, String[][] properties)
			throws Exception
	{
		return POJOUtil.map2pojo(call(process, params), pojo, properties);
	}

	public abstract Map<String, Object> call(String process, Map<String, Object> params)
			throws Exception;
}
