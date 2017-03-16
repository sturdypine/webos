package spc.webos.service.common.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spc.webos.model.DictPO;
import spc.webos.persistence.IPersistence;
import spc.webos.service.BaseService;
import spc.webos.service.common.DictDesc;
import spc.webos.service.common.DictService;
import spc.webos.service.common.OptionMap;

/**
 * 数据字典的Dao...用于系统数据字典服务
 * 
 * @author Hate
 * 
 */
public class DictServiceImpl extends BaseService implements DictService
{
	public void getAllDictDescVO(Map cache) throws Exception
	{
		OptionMap resultMap = getVO(getAllDict(), null, null);
		if (resultMap == null) return;
		Iterator keys = resultMap.keySet().iterator();
		while (keys.hasNext())
		{
			String key = (String) keys.next();
			cache.put(key, (Serializable) resultMap.get(key));
		}
	}

	OptionMap getVO(List<DictPO> dicts, String cd, String dtype)
	{// "ddd.getDDD" Integer.intValue()
		List<DictPO> list = cd == null ? getTopLevelDict(dicts) : getDict(dicts, cd, dtype);
		if (list == null || list.size() == 0)
		{
			log.debug("dict code:{}, dtype:{} is null", cd, dtype);
			return null;
		}

		OptionMap resultMap = new OptionMap();
		String zero = "0";
		list.forEach((dict) -> {
			DictDesc vo = new DictDesc();
			String code = dict.getCode();
			resultMap.put(code, vo);

			String[] item = { code, dict.getName() };
			vo.setItem(item);

			if (zero.equals(dict.getDlevel())) return; // 没有子节点
			vo.setDict(getVO(dicts, dict.getCode(), dict.getDtype()));
		});
		return resultMap;
	}

	protected List<DictPO> getTopLevelDict(List<DictPO> dicts)
	{
		List<DictPO> result = new ArrayList<>();
		dicts.forEach((d) -> {
			if (d.getDlevel().equals("1")) result.add(d);
		});
		return result;
	}

	protected List<DictPO> getDict(List<DictPO> dicts, String parentCd, String dtype)
	{
		List<DictPO> result = new ArrayList<>();
		dicts.forEach((d) -> {
			if (parentCd.equals(d.getParentcode()) && d.getDtype().equals(dtype)) result.add(d);
		});
		return result;
	}

	// 得到全表记录
	protected List<DictPO> getAllDict()
	{
		Map params = new HashMap();
		params.put(IPersistence.SELECT_ATTACH_TAIL_KEY, "order by dorder");
		return persistence.get(new DictPO(), params);
	}

	protected String dictSqlId = "common.getDD";

	public void setDictSqlId(String dictSqlId)
	{
		this.dictSqlId = dictSqlId;
	}
}
