package spc.webos.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;

import net.sf.cglib.beans.BeanMap;

public class POJOUtil
{
	public static Map<String, Object> pojo2map(Object pojo, Map<String, Object> m)
	{
		if (m == null) m = new HashMap<>();
		if (pojo == null) return m;
		BeanMap bm = BeanMap.create(pojo);
		for (Object key : bm.keySet())
		{
			if ("class".equals(key)) continue;
			Object v = bm.get(key);
			if (v != null) m.put(key.toString(), v);
		}
		return m;
	}

	public static Map<String, Object> pojo2map(Object pojo, Map<String, Object> m,
			String[] properties)
	{
		if (pojo == null) return m;
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (String p : properties)
			if (wrapper.isReadableProperty(p))
			{
				Object v = wrapper.getPropertyValue(p);
				if (v != null) m.put(p, v);
			}
		return m;
	}

	public static Map<String, Object> pojo2map(Object pojo, Map<String, Object> m,
			String[][] properties)
	{
		if (pojo == null) return m;
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (String[] p : properties)
			if (wrapper.isReadableProperty(p[0]))
			{
				Object v = wrapper.getPropertyValue(p[0]);
				if (v != null) m.put((p.length == 1 || StringX.nullity(p[1])) ? p[0] : p[1], v);
			}
		return m;
	}

	public static Map<String, Object> pojo2map(Object pojo, Map<String, Object> m, String[] sp,
			String[] tp)
	{
		if (pojo == null) return m;
		if (sp == null) return pojo2map(pojo, m);
		if (tp == null) return pojo2map(pojo, m, sp);
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (int i = 0; i < sp.length; i++)
			if (wrapper.isReadableProperty(sp[i]))
			{
				Object v = wrapper.getPropertyValue(sp[i]);
				if (v != null) m.put(StringX.nullity(tp[i]) ? sp[i] : tp[i], v);
			}
		return m;
	}

	public static Map<String, String> pojo2strmap(Object pojo)
	{
		Map<String, String> m = new HashMap<String, String>();
		if (pojo == null) return m;
		BeanMap bm = BeanMap.create(pojo);
		for (Object key : bm.keySet())
		{
			String k = key.toString();
			if (k.equals("class")) continue;
			Object value = bm.get(key);
			if (value == null) continue;
			m.put(k, value.toString());
		}
		return m;
	}

	public static Map<String, Object> map2map(Map<String, Object> m, String[] properties)
	{
		if (properties == null) return new HashMap<>(m);
		Map<String, Object> map = new HashMap<>();
		for (String p : properties)
		{
			Object v = m.get(p);
			if (v != null) map.put(p, v);
		}
		return map;
	}

	public static Map<String, Object> map2map(Map<String, Object> m, String[][] properties)
	{
		if (properties == null) return new HashMap<>(m);
		Map<String, Object> map = new HashMap<>();
		for (String[] p : properties)
		{
			Object v = m.get(p[0]);
			if (v == null) continue;
			map.put(p.length > 1 && !StringX.nullity(p[1]) ? p[1] : p[0], v);
		}
		return map;
	}

	public static Map<String, String> strmap(Map<Object, Object> m)
	{
		Map<String, String> hash = new HashMap<String, String>();
		for (Object key : m.keySet())
			hash.put(key.toString(), StringX.null2emptystr(m.get(key)));
		return hash;
	}

	public static <T> T setPropertyValue(T pojo, String[] properties, Object[] value)
	{
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (int i = 0; i < properties.length; i++)
			wrapper.setPropertyValue(properties[i], value[i]);
		return pojo;
	}

	public static <T> T setPropertyValue(T pojo, String p, Object v)
	{
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		wrapper.setPropertyValue(p, v);
		return pojo;
	}

	public static <T> T map2pojo(Map<String, Object> map, T pojo)
	{
		return map2pojo(map, pojo, false);
	}

	public static <T> T map2pojo(Map<String, Object> map, T pojo, boolean ignoreCase)
	{
		if (map == null || map.isEmpty()) return pojo;
		Class<?> c = pojo.getClass();
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (String key : map.keySet())
		{
			String p = ignoreCase ? getProperty(c, key) : key;
			if (!StringX.nullity(p) && wrapper.isWritableProperty(p))
				wrapper.setPropertyValue(p, map.get(key));
		}
		return pojo;
	}

	public static <T> T map2pojo(Map<String, Object> map, T pojo, String[] properties)
	{
		return map2pojo(map, pojo, properties, false);
	}

	public static <T> T map2pojo(Map<String, Object> map, T pojo, String[] properties,
			boolean ignoreCase)
	{
		if (map == null || map.isEmpty()) return pojo;
		Class<?> c = pojo.getClass();
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (String key : properties)
		{
			String rp = key;
			String wp = key;
			int idx = key.indexOf('-');
			if (idx > 0)
			{
				rp = key.substring(0, idx);
				wp = key.substring(idx + 1);
			}
			if (ignoreCase) wp = getProperty(c, wp);
			if (!StringX.nullity(wp) && wrapper.isWritableProperty(wp))
				wrapper.setPropertyValue(wp, map.get(rp));
		}
		return pojo;
	}

	public static <T> T map2pojo(Map<String, Object> map, T pojo, String[][] properties)
	{
		return map2pojo(map, pojo, properties, false);
	}

	public static <T> T map2pojo(Map<String, Object> map, T pojo, String[][] properties,
			boolean ignoreCase)
	{
		if (map == null || map.isEmpty()) return pojo;
		if (properties == null) return map2pojo(map, pojo);
		Class<?> c = pojo.getClass();
		BeanWrapperImpl wrapper = new BeanWrapperImpl();
		wrapper.setWrappedInstance(pojo);
		for (String[] key : properties)
		{
			String k = (key.length == 1 || StringX.nullity(key[1])) ? key[0] : key[1];
			String p = ignoreCase ? getProperty(c, k) : k;
			if (!StringX.nullity(p) && wrapper.isWritableProperty(p))
				wrapper.setPropertyValue(key[0], map.get(p));
		}
		return pojo;
	}

	public static <T> T pojo2pojo(Object source, T pojo, String[] properties)
	{
		return pojo2pojo(source, pojo, properties, false);
	}

	public static <T> T pojo2pojo(Object source, T pojo, String[] properties, boolean ignoreCase)
	{
		BeanWrapperImpl sw = new BeanWrapperImpl();
		sw.setWrappedInstance(source);
		BeanWrapperImpl pw = new BeanWrapperImpl();
		pw.setWrappedInstance(pojo);
		Class<?> c = pojo.getClass();
		for (String p : properties)
		{
			String rp = p;
			String wp = p;
			int idx = p.indexOf('-');
			if (idx > 0)
			{
				rp = p.substring(0, idx);
				wp = p.substring(idx + 1);
			}
			if (ignoreCase) wp = getProperty(c, wp);
			pw.setPropertyValue(wp, sw.getPropertyValue(rp));
		}
		return pojo;
	}

	public static <T> T pojo2pojo(Object source, T pojo, String[][] properties)
	{
		return pojo2pojo(source, pojo, properties, false);
	}

	public static <T> T pojo2pojo(Object source, T pojo, String[][] properties, boolean ignoreCase)
	{
		BeanWrapperImpl sw = new BeanWrapperImpl();
		sw.setWrappedInstance(source);
		BeanWrapperImpl pw = new BeanWrapperImpl();
		pw.setWrappedInstance(pojo);
		Class<?> c = pojo.getClass();
		for (String[] p : properties)
		{
			String wp = (p.length == 1 || StringX.nullity(p[1])) ? p[0] : p[1];
			wp = ignoreCase ? getProperty(c, wp) : wp;
			pw.setPropertyValue(wp, sw.getPropertyValue(p[0]));
		}
		return pojo;
	}

	static Logger log = LoggerFactory.getLogger(POJOUtil.class);
	final static Map<Class<?>, Map<String, String>> CLASS_FIELD = new ConcurrentHashMap<Class<?>, Map<String, String>>();

	/**
	 * 忽略大小写的情况下从class中获取准确的Bean属性名
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	public static String getProperty(Class<?> clazz, String fieldName)
	{
		Map<String, String> field = getClazzField(clazz);
		if (field == null) return null;
		return field.get(fieldName.toLowerCase());
	}

	public static Map<String, String> getClazzField(Class<?> clazz)
	{
		// synchronized (CLASS_FIELD)
		Map<String, String> field = CLASS_FIELD.get(clazz);
		if (field != null) return field;
		field = new HashMap<String, String>();
		Class<?> nclass = clazz;
		while (!nclass.equals(Object.class))
		{
			Field[] fields = nclass.getDeclaredFields();
			for (int i = 0; fields != null && i < fields.length; i++)
			{
				if (Modifier.isStatic(fields[i].getModifiers())) continue;
				String fname = fields[i].getName();
				field.put(fname.toLowerCase(), fname);
			}
			nclass = nclass.getSuperclass();
		}
		CLASS_FIELD.put(clazz, field);
		log.info("load class:{}, fields:{}", clazz.getName(), field.keySet());
		return field;
	}
}
