package spc.webos.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.beans.BulkBean;
import net.sf.cglib.proxy.LazyLoader;

public class ResultLazyLoader implements LazyLoader
{
	IPersistence persistence;
	Object vo; //
	Object[] bulkBeans;
	Map paramMap;
	boolean lazyLoading;

	public ResultLazyLoader(IPersistence persistence, Object[] bulkBeans, Object vo, Map paramMap, boolean lazyLoading)
	{
		this.persistence = persistence;
		this.bulkBeans = bulkBeans;
		this.vo = vo;
		this.paramMap = paramMap;
		this.lazyLoading = lazyLoading;
	}

	public Object loadObject() throws Exception
	{
		if (bulkBeans[0] instanceof String)
		{ // 此属性不是关联其他VO属性， 而是一个普通的sql属性, 配置的是一个sqlId
			// String key = "_FIRST_ROW_ONEY_";
			// paramMap.put(key, new Boolean(false));
			Map map = new HashMap(paramMap); // 由于paramMap是线程不安全的,需要重新定义对象
			map.put("_VO_", vo); // 覆盖原来的属性，
			// System.out.println("map = " + map);
			return persistence.execute((String) bulkBeans[0], map);
		}
		Object[] values = new Object[((BulkBean) bulkBeans[0]).getGetters().length];
		((BulkBean) bulkBeans[0]).getPropertyValues(vo, values);

		Object property = (Object) ((Class) bulkBeans[3]).newInstance();
		((BulkBean) bulkBeans[1]).setPropertyValues(property, values);

		return (Class) bulkBeans[2] != List.class ? (Object) persistence.find(property, lazyLoading)
				: (Object) persistence.get(property, lazyLoading);
	}
}
