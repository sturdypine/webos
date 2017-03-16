package spc.webos.bpl;

import java.util.Map;

public interface Engine
{
	<T> T call(String process, Map<String, Object> params, T pojo) throws Exception;

	<T> T call(String process, Map<String, Object> params, T pojo, String[] properties)
			throws Exception;

	<T> T call(String process, Map<String, Object> params, T pojo, String[][] properties)
			throws Exception;

	Map<String, Object> call(String process, Map<String, Object> params) throws Exception;
}
