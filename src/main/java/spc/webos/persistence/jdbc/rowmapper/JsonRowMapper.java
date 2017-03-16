package spc.webos.persistence.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.support.JdbcUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import spc.webos.persistence.SQLItem;
import spc.webos.util.SpringUtil;

public class JsonRowMapper extends AbstractRowMapper
{
	ResultSetMetaData rsmd;
	String[] columnName;

	public String[] getColumnName()
	{
		return columnName;
	}

	public ResultSetMetaData getResultSetMetaData()
	{
		return rsmd;
	}

	public JsonRowMapper(String charsetDB)
	{
		this.charsetDB = charsetDB;
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		ObjectMapper m = new ObjectMapper();
		rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		StringBuilder row = new StringBuilder(128);
		row.append('{');
		SQLItem item = SQLItem.getCurrentItem();
		for (int i = 1; i <= columnCount; i++)
		{
			String key = rsmd.getColumnName(i).toLowerCase();
			Object v = JdbcUtils.getResultSetValue(rs, i);
			if (v == null) continue;
			if (isConverter(key)) v = converter(key, v); // 字段需要转换
			try
			{
				if ((v instanceof String)) v = v.toString().trim();
				if ((v instanceof String) && charsetDB != null)
					v = new String(v.toString().getBytes(charsetDB));
			}
			catch (Exception e)
			{
				throw new RuntimeException(
						"Error in coverting to Charset(" + charsetDB + "), value=" + v, e);
			}
			if (row.length() > 2) row.append(',');
			row.append('"');
			row.append(key);
			row.append("\":");
			String str = null;
			if (item != null && SpringUtil.APPCXT != null && item.dict != null)
			{ // 用内存数据字典转换
				String vv = null;
				String d = (String) item.dict.get(key);
				if (d != null) vv = SpringUtil.getMessage(d, (String) v, null, null);
				str = (vv != null ? vv : v.toString());
			}
			else str = v.toString().trim();
			try
			{
				row.append(m.writeValueAsString(str));
			}
			catch (JsonProcessingException e)
			{
				throw new RuntimeException("Error in coverting to Json, value=" + str, e);
			}
		}
		row.append('}');
		return row;
	}

	String charsetDB;
}
