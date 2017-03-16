package spc.webos.service.common;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.util.StringX;

public class DictDesc implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;
	Object[] item; // code,name
	transient Logger log = LoggerFactory.getLogger(getClass());

	// String name;
	// String code;
	// String defaultValue; // 对子项目的默认值
	Map dict; // 所有可选子项目

	public boolean isTree()
	{
		if (dict == null || dict.size() == 0) return false;
		Iterator iter = dict.entrySet().iterator();
		while (iter.hasNext())
		{
			DictDesc dd = (DictDesc) iter.next();
			if (dd.dict != null && dd.dict.size() > 0) return true;
		}
		return false;
	}

	// 采用深度遍历递归方式找到树的子节点
	public DictDesc dfs(String key, List path)
	{
		if (dict == null || dict.size() == 0) return null;
		DictDesc child = (DictDesc) dict.get(key);
		if (child != null) return child;
		Iterator entry = dict.entrySet().iterator();
		while (entry.hasNext())
		{
			DictDesc dd = (DictDesc) entry.next();
			if (path != null) path.add(dd); // 加入到路径
			child = dd.dfs(key, path);
			if (child != null) return child;
			if (path != null) path.remove(path.size() - 1); // 退出当前路径
		}
		return null;
	}

	public String toJsonDict()
	{
		StringBuffer buf = new StringBuffer();
		try
		{
			buf.append('\'');
			buf.append(item[0]);
			buf.append('\'');
			buf.append(':');
			if (!isTree())
			{ // UIGJ:[['111','PhotoShop'],['112','Illstrator'],['113','Flash'],['114','DreamWaver']]
				buf.append('[');
				boolean first = true;
				Iterator keys = dict.keySet().iterator();
				while (keys.hasNext())
				{
					String key = keys.next().toString();
					DictDesc desc = (DictDesc) dict.get(key);
					if (!first) buf.append(',');
					first = false;
					buf.append('[');
					for (int j = 0; j < desc.item.length; j++)
					{
						if (j != 0) buf.append(',');
						buf.append('\'');
						buf.append(StringX.str2utf8((String) desc.item[j]));
						buf.append('\'');
					}
					buf.append(']');
				}
				buf.append(']');
				return buf.toString();
			}
			else buf.append(toJsonTreeDict());
		}
		catch (Exception e)
		{
			log.warn("toJsonDict: " + item[0] + "," + item[1], e);
		}
		return buf.toString();
	}

	String toJsonTreeDict()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("{text:'");
		buf.append(StringX.str2utf8((String) item[1]));
		buf.append("',code:'");
		buf.append(item[0]);
		if (dict == null || dict.size() == 0)
		{
			buf.append("',leaf:true}");
			return buf.toString();
		}
		buf.append("',leaf:false,children:[");
		boolean first = true;
		Iterator keys = dict.keySet().iterator();
		while (keys.hasNext())
		{
			if (!first) buf.append(',');
			first = false;
			String key = keys.next().toString();
			DictDesc desc = (DictDesc) dict.get(key);
			buf.append(desc.toJsonTreeDict());
		}
		buf.append("]}");
		return buf.toString();
	}

	public String toHTML(String value)
	{
		return toHTML(value, null);
	}

	public String toHTML(String value, String format)
	{
		MessageFormat mf = null;
		StringBuffer buf = new StringBuffer();
		// System.out.println(item[1] + ", " + isTree());
		if (isTree())
		{
			Iterator keys = dict.keySet().iterator();
			while (keys.hasNext())
			{
				String key = keys.next().toString();
				DictDesc desc = (DictDesc) dict.get(key);
				if (desc.dict == null || desc.dict.size() == 0)
				{
					buf.append("<option value=\"");
					buf.append(key);
					if (value.equals(key)) buf.append("\" selected>");
					else buf.append("\">");
					if (mf != null) buf.append(mf.format(desc.item));
					else buf.append(desc);
					buf.append("</option>\n");
				}
				else
				{
					buf.append("\n<optgroup label='");
					buf.append(desc.item[1]);
					buf.append("'>\n");
					buf.append(desc.toHTML(value, format));
					buf.append("\n</optgroup>");
				}
			}
			return buf.toString();
		}
		Iterator keys = dict.keySet().iterator();
		while (keys.hasNext())
		{
			String key = keys.next().toString();
			buf.append("<option value=\"");
			buf.append(key);
			if (value.equals(key)) buf.append("\" selected>");
			else buf.append("\">");
			if (mf != null) buf.append(mf.format(((DictDesc) dict.get(key)).item));
			else buf.append(dict.get(key));
			buf.append("</option>\n");
		}
		// System.out.println(buf);
		return buf.toString();
	}

	public String getName()
	{
		return (String) item[1];
	}

	public Map getDict()
	{
		return dict;
	}

	public void setDict(Map dict)
	{
		this.dict = dict;
	}

	public String toString()
	{
		return (String) item[1];
	}

	public Object[] getItem()
	{
		return item;
	}

	public void setItem(Object[] item)
	{
		this.item = item;
	}
}
