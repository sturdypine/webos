package spc.webos.persistence.jdbc.rowtype;

import java.util.Arrays;
import java.util.HashMap;

import spc.webos.util.JsonUtil;

/**
 * 特殊的Map. 它表面是个Map, 其实是List, 具有Map的方便使用和List节约内存的两个功能, 把整个数据库的结果集,
 * 用一个公用的字段名来重新定位在List中的位置..
 * 
 * @author Hate
 */
public class RowXMap extends HashMap
{
	private static final long serialVersionUID = 1L;

	public String[] getColumnName()
	{
		return columnName;
	}

	public RowXMap(String[] columnName)
	{
		this.columnName = columnName;
		value = new Object[columnName.length];
	}

	public Object put(Object key, Object v)
	{ // 只能抽取数据
		value[getFieldIndex(key.toString().toLowerCase())] = v;
		return v;
	}

	public Object get(Object key)
	{
		int index = getFieldIndex(key.toString().toLowerCase());
		if (index < 0) return null;
		return value[index];
	}

	public StringBuffer toJson()
	{
		StringBuffer row = new StringBuffer();
		row.append(JsonUtil.obj2json(this));
		// row.append('{');
		// for (int i = 0; i < this.fieldNames.length; i++)
		// {
		// Object v = get(fieldNames[i]);
		// if (v == null) continue;
		// if (row.length() > 2) row.append(',');
		// row.append(fieldNames[i]);
		// row.append(":'");
		// String str = v.toString();
		// if (str.indexOf('\'') >= 0) str = str.replace("'", "\\'");
		// if (str.indexOf('\n') >= 0) str = str.replace("\n", "\\n");
		// row.append(str);
		// row.append('\'');
		// }
		// row.append('}');
		return row;
	}

	/**
	 * 把字段名字格式化为标准的排序形式
	 * 
	 * @param fieldNames
	 * @return
	 */
	public static String[] getFormatFieldNames(String[] fieldNames)
	{
		for (int i = 0; i < fieldNames.length; i++)
			fieldNames[i] = fieldNames[i].toLowerCase().trim();
		Arrays.sort(fieldNames);
		return fieldNames;
	}

	Object[] value;
	String[] columnName; // 字段信息

	int getFieldIndex(String fieldName)
	{
		return Arrays.binarySearch(columnName, fieldName);
	}
}
