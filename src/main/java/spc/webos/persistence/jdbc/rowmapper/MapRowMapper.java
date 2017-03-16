package spc.webos.persistence.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.support.JdbcUtils;

import spc.webos.persistence.SQLItem;
import spc.webos.persistence.jdbc.rowtype.RowMap;
import spc.webos.util.StringX;
import spc.webos.util.SpringUtil;

public class MapRowMapper extends AbstractRowMapper
{
	ResultSetMetaData rsmd;
	String[] columnName;

	public ResultSetMetaData getResultSetMetaData()
	{
		return rsmd;
	}

	public MapRowMapper(String charsetDB)
	{
		this.charsetDB = charsetDB;
	}

	public String[] getColumnName()
	{
		return columnName;
	}

	protected String getKey(String key)
	{
		Map col = (Map) COLUMN_CASE.get();
		if (col == null) return key;
		return (String) col.get(key);
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		rsmd = rs.getMetaData();
		if (columnName == null)
		{
			columnName = new String[rsmd.getColumnCount()];
			for (int i = 0; i < columnName.length; i++)
				columnName[i] = rsmd.getColumnName(i + 1).toLowerCase();
		}
		int columnCount = rsmd.getColumnCount();
		Map mapOfColValues = COLUMN_CASE.get() != null ? new HashMap(columnCount) : new RowMap(
				columnCount, columnName);
		SQLItem item = SQLItem.getCurrentItem();
		for (int i = 1; i <= columnCount; i++)
		{
			String key = getKey(rsmd.getColumnName(i).toLowerCase());
			if (key == null) continue; // 如果查询配置了字段大小写敏感转换，如果从中查询不到，则不转换
			Object obj = JdbcUtils.getResultSetValue(rs, i);
			if (obj == null)
			{
				mapOfColValues.put(key, StringX.EMPTY_STRING);
				continue;
			}
			if (isConverter(key))
			{ // 字段需要转换 modify by spc 090601
				mapOfColValues.put(key, converter(key, obj));
				continue;
			}
			// update by sturdypine.chen 2007-04-18 使用数据库中数据原类型，不改为string对象
			// String value = value.toString().trim();
			try
			{
				if ((obj instanceof String)) obj = obj.toString().trim();
				if ((obj instanceof String) && charsetDB != null) obj = new String(obj.toString()
						.getBytes(charsetDB));
			}
			catch (Exception e)
			{
				throw new RuntimeException("Error in coverting to Charset(" + charsetDB + "), value="
						+ obj, e);
			}

			if (item != null && SpringUtil.APPCXT != null && item.dict != null && obj instanceof String)
			{ // 用内存数据字典转换
				String v = null;
				String d = (String) item.dict.get(key);
				if (d != null) v = SpringUtil.getMessage(d, (String) obj, null, null);
				mapOfColValues.put(key, v != null ? v : obj);
			}
			else mapOfColValues.put(key, obj);
		}
		// System.out.println(mapOfColValues);
		return mapOfColValues;
	}

	String charsetDB;
}
