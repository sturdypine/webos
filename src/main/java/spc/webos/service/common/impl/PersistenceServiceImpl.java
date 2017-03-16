package spc.webos.service.common.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.constant.AppRetCode;
import spc.webos.constant.Web;
import spc.webos.exception.AppException;
import spc.webos.persistence.IPersistence;
import spc.webos.persistence.SQLItem;
import spc.webos.service.BaseService;
import spc.webos.service.common.PagingResult;
import spc.webos.service.common.PersistenceService;
import spc.webos.util.POJOUtil;
import spc.webos.util.StringX;

@Service("persistenceService")
public class PersistenceServiceImpl extends BaseService implements PersistenceService
{
	public PagingResult queryPage(String sid, String ssid, Map<String, Object> params)
			throws Exception
	{
		List<String> batchSQL = new ArrayList<>();
		// 防止前端输入sql id大小写非下划线分隔
		sid = sid.toLowerCase().replace('.', '_');
		String strBatchSQL = StringX.null2emptystr((String) params.get(Web.REQ_KEY_BATCH_SQL),
				(String) params.get(Web.REQ_KEY_BSQL));
		if (!StringX.nullity(strBatchSQL))
			batchSQL = StringX.delimitedList(strBatchSQL, StringX.COMMA);
		if (!StringX.nullity(ssid)) batchSQL.add(ssid.toLowerCase().replace('.', '_'));
		if (!StringX.nullity(sid)) batchSQL.add(sid);
		// 如果没有提供返回类型，则使用map
		if (!params.containsKey(Web.REQ_KEY_SQL_CLASS))
			params.put(IPersistence.RESULT_CLASS_PREFIX + sid, SQLItem.RESULT_CLASS_MAP);
		params.put(IPersistence.SELECT_ONLY, true); // 只能查询，不能修改
		persistence.execute(batchSQL, params, params);

		int start = Integer.parseInt(
				StringX.null2emptystr((String) params.get(Web.REQ_EXTJS_PAGING_START), "0"));
		int limit = Integer.parseInt(
				StringX.null2emptystr((String) params.get(Web.REQ_EXTJS_PAGING_LIMIT), "-1"));

		return new PagingResult(
				!StringX.nullity(ssid) ? Integer.parseInt(params.get(ssid).toString()) : 0, start,
				limit, (List) params.get(sid));
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Integer> update(List<List<Object>> po) throws AppException
	{
		List<Integer> rows = new ArrayList<>();
		po.forEach((p) -> rows.add(persistence
				.update(POJOUtil.map2pojo((Map) p.get(1), po((String) p.get(0)), true))));
		return rows;
	}

	public int update(String clazz, Map<String, Object> map)
	{
		return persistence.update(POJOUtil.map2pojo(map, po(clazz), true));
	}

	public int update(Object po)
	{
		return persistence.update(po);
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Integer> insert(List<List<Object>> po) throws AppException
	{
		List<Integer> rows = new ArrayList<>();
		po.forEach((p) -> rows.add(persistence
				.insert(POJOUtil.map2pojo((Map) p.get(1), po((String) p.get(0)), true))));
		return rows;
	}

	public int insert(String clazz, Map<String, Object> map)
	{
		return persistence.insert(POJOUtil.map2pojo(map, po(clazz), true));
	}

	public int insert(Object po)
	{
		return persistence.insert(po);
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public List<Integer> delete(List<List<Object>> po) throws AppException
	{
		List<Integer> rows = new ArrayList<>();
		po.forEach((p) -> rows.add(persistence
				.delete(POJOUtil.map2pojo((Map) p.get(1), po((String) p.get(0)), true))));
		return rows;
	}

	public int delete(String clazz, Map<String, Object> map)
	{
		return persistence.delete(POJOUtil.map2pojo(map, po(clazz), true));
	}

	public int delete(Object po)
	{
		return persistence.delete(po);
	}

	public List<Map<String, Object>> get(String clazz, Map<String, Object> map)
	{
		List<Object> list = persistence.get(POJOUtil.map2pojo(map, po(clazz), true));
		if (list == null || list.isEmpty()) return null;
		List<Map<String, Object>> result = new ArrayList<>(list.size());
		list.forEach((po) -> result.add(POJOUtil.pojo2map(po, null)));
		return result;
	}

	public Map<String, Object> find(String clazz, Map<String, Object> map)
	{
		return POJOUtil.pojo2map(persistence.find(POJOUtil.map2pojo(map, po(clazz), true)), null);
	}

	public <T> T find(T po)
	{
		return persistence.find(po);
	}

	public <T> List<T> get(T po)
	{
		return persistence.get(po);
	}

	public Object query(String sqlId, Map<String, Object> params)
	{
		if (params == null) params = new HashMap<>();
		params.put(IPersistence.SELECT_ONLY, true);
		return execute(sqlId, params);
	}

	public Map<String, Object> queryBatch(String[] sqlId, Map<String, Object> params)
	{
		Map<String, Object> result = new HashMap<>();
		persistence.execute(sqlId, params, result);
		return result;
	}

	public Object execute(String sqlId, Map<String, Object> params)
	{
		return persistence.execute(sqlId, params);
	}

	public Map<String, Object> execute(String[] sqlId, Map<String, Object> params,
			Map<String, Object> result)
	{
		return persistence.execute(sqlId, params, result);
	}

	protected Object po(String clazz)
	{
		try
		{
			return Class.forName(clazz.indexOf('.') > 0 ? clazz : prefix + clazz + postfix, true,
					Thread.currentThread().getContextClassLoader()).newInstance();
		}
		catch (Exception e)
		{
			throw new AppException(AppRetCode.CMM_BIZ_ERR, e.toString());
		}
	}

	protected String prefix = "spc.webos.model.";
	protected String postfix = "PO";

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public void setPostfix(String postfix)
	{
		this.postfix = postfix;
	}
}
