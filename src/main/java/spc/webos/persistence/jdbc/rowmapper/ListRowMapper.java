package spc.webos.persistence.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.support.JdbcUtils;

import spc.webos.persistence.SQLItem;
import spc.webos.persistence.jdbc.rowtype.RowList;
import spc.webos.util.SpringUtil;

public class ListRowMapper extends AbstractRowMapper
{
	ResultSetMetaData rsmd;
	String[] columnName;

	public ResultSetMetaData getResultSetMetaData()
	{
		return rsmd;
	}

	public ListRowMapper(String charsetDB)
	{
		this.charsetDB = charsetDB;
	}

	public String[] getColumnName()
	{
		return columnName;
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
		int columnCount = columnName.length;
		List listOfColValues = new RowList(columnName, columnCount);
		SQLItem item = SQLItem.getCurrentItem();
		for (int i = 1; i <= columnCount; i++)
		{
			String key = columnName[i - 1];
			Object obj = JdbcUtils.getResultSetValue(rs, i);
			if (obj == null)
			{
				listOfColValues.add(null); // modify by sturdypine.chen 070917
				continue;
			}
			if (isConverter(key))
			{ // 字段需要转换 modify by spc 090601
				listOfColValues.add(converter(key, obj));
				continue;
			}
			// System.out.println("value: "+value.getClass().getName()+",
			// "+value);
			// update by sturdypine.chen 2007-04-18 使用数据库中数据原类型，不改为string对象
			// String value = value.toString().trim();
			try
			{
				if ((obj instanceof String)) obj = charsetDB == null ? obj.toString().trim()
						: new String(obj.toString().getBytes(charsetDB));
			}
			catch (Exception e)
			{
				throw new RuntimeException(
						"Error in coverting to Charset(" + charsetDB + "), value=" + obj, e);
			}
			if (item != null && SpringUtil.APPCXT != null && item.dict != null
					&& obj instanceof String)
			{ // 用内存数据字典转换
				// System.out.println(item.dict);
				String v = null;
				String d = (String) item.dict.get(key);
				if (d != null) v = SpringUtil.getMessage(d, (String) obj, null, null);
				listOfColValues.add(v != null ? v : obj);
			}
			else listOfColValues.add(obj);
		}
		return listOfColValues;
	}

	String charsetDB;
}
