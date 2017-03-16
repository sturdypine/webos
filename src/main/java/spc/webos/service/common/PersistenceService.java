
package spc.webos.service.common;

import java.util.List;
import java.util.Map;

import spc.webos.exception.AppException;

/**
 * 此接口主要是为了支持前端服务调用，由于不能指定class，所以不能对象PO操作
 * 
 * @author chenjs
 *
 */
public interface PersistenceService
{
	PagingResult queryPage(String sid, String ssid, Map<String, Object> params) throws Exception;

	int update(String clazz, Map<String, Object> map) throws AppException;

	int insert(String clazz, Map<String, Object> map) throws AppException;

	int delete(String clazz, Map<String, Object> map) throws AppException;

	Map<String, Object> find(String clazz, Map<String, Object> map) throws AppException;

	List<Map<String, Object>> get(String clazz, Map<String, Object> map) throws AppException;

	Map<String, Object> queryBatch(String[] sqlId, Map<String, Object> params);

	Object query(String sqlId, Map<String, Object> params);

	Object execute(String sqlId, Map<String, Object> params);

	Map<String, Object> execute(String[] sqlId, Map<String, Object> params,
			Map<String, Object> result);
}
