package spc.webos.persistence.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.alibaba.druid.pool.DruidDataSource;

import spc.webos.persistence.SQLItem;
import spc.webos.persistence.jdbc.datasource.DynamicDataSource;
import spc.webos.persistence.jdbc.rowmapper.ClazzRowMapper;
import spc.webos.persistence.jdbc.rowmapper.JsonRowMapper;
import spc.webos.persistence.jdbc.rowmapper.ListRowMapper;
import spc.webos.persistence.jdbc.rowmapper.MapRowMapper;
import spc.webos.persistence.jdbc.rowmapper.XMapRowMapper;

public class XJdbcTemplate extends JdbcTemplate
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	public Connection createConnectionProxy(Connection con)
	{
		return super.createConnectionProxy(con);
	}

	// public Connection getConnection()
	// {
	// Connection con = DataSourceUtils.getConnection(getDataSource());
	// // Create close-suppressing Connection proxy, also preparing
	// // returned Statements.
	// return createConnectionProxy(con);
	// // }
	// // catch (SQLException ex)
	// // {
	// // // Release Connection early, to avoid potential connection pool
	// // // deadlock
	// // // in the case when the exception translator hasn't been initialized
	// // // yet.
	// // DataSourceUtils.releaseConnection(con, getDataSource());
	// // con = null;
	// //
	// // throw getExceptionTranslator().translate("ConnectionCallback",
	// // getSql(action), ex);
	// // }
	// // finally
	// // {
	// // DataSourceUtils.releaseConnection(con, getDataSource());
	// // }
	// }

	/**
	 * 用给定的Sql语句, 和行处理类, 来返回结果
	 * 
	 * @param sql
	 * @param rowClazzName
	 * @return
	 * @throws DataAccessException
	 */
	public List query(String sqlID, String sql, String rowClazzName) throws DataAccessException
	{
		// if (log.isDebugEnabled())
		// logger.debug("XJdbcTemplate.query sql = " + sql + ", rowClazzName = "
		// + rowClazzName);
		log.info("SQL({},{}): {}", sqlID, getDbName(), sql);
		try
		{
			if (charsetSQL != null) sql = new String(sql.getBytes(charsetSQL));
		}
		catch (Exception e)
		{
			throw new RuntimeException(
					"Error in coverting to Charset(" + charsetSQL + "), sql=" + sql, e);
		}
		// long s = 0;
		// if (log.isInfoEnabled()) s = System.currentTimeMillis();
		// List result = null;
		Class rowClazz = null;
		// 采用的是单一行类, 例如Integer, String, Short....
		if ((rowClazz = (Class) innerClazzMap.get(rowClazzName)) != null)
			return super.queryForList(sql, rowClazz); // 内嵌类型
		else return super.query(sql, getRowMapper(rowClazzName));

		// // 采用扩展的Map方式来处理行, 此方式比一般的hashmap节省Key空间
		// if (SQLItem.RESULT_CLASS_XMAP.equalsIgnoreCase(rowClazzName))
		// result = query(sql, new XMapRowMapper(charsetDB));
		// else if (SQLItem.RESULT_CLASS_LIST.equalsIgnoreCase(rowClazzName)) //
		// 采用List方式来处理行
		// result = query(sql, new ListRowMapper(charsetDB));
		// else if (SQLItem.RESULT_CLASS_MAP.equalsIgnoreCase(rowClazzName)) //
		// 采用一般的HashMap来处理行
		// result = query(sql, new MapRowMapper(charsetDB));
		// else if (SQLItem.RESULT_CLASS_JSON.equalsIgnoreCase(rowClazzName)) //
		// 采用一般的HashMap来处理行
		// result = query(sql, new JsonRowMapper(charsetDB));
		// // 采用的是单一行类, 例如Integer, String, Short....
		// else if ((rowClazz = (Class) innerClazzMap.get(rowClazzName)) !=
		// null)
		// result = super.queryForList(sql, rowClazz);
		// else
		// { // 采用的是自定义的行处理类
		// try
		// {
		// rowClazz = Class.forName(rowClazzName, false,
		// Thread.currentThread().getContextClassLoader());
		// }
		// catch (Exception e)
		// {
		// throw new RuntimeException("cannot find class:" + rowClazzName);
		// }
		// result = query(sql, new ClazzRowMapper(rowClazz, charsetDB));
		// }
		// if (log.isDebugEnabled())
		// {
		// String msg = "JdbcTemplate.query cost:" + (System.currentTimeMillis()
		// - s) + ", size:"
		// + (result == null ? 0 : result.size());
		// log.debug(msg);
		// }
		// return result;
	}

	public RowMapper getRowMapper(String rowClazzName)
	{
		// 采用扩展的Map方式来处理行, 此方式比一般的hashmap节省Key空间
		if (SQLItem.RESULT_CLASS_XMAP.equalsIgnoreCase(rowClazzName))
			return new XMapRowMapper(charsetDB);
		if (SQLItem.RESULT_CLASS_LIST.equalsIgnoreCase(rowClazzName)) // 采用List方式来处理行
			return new ListRowMapper(charsetDB);
		if (SQLItem.RESULT_CLASS_MAP.equalsIgnoreCase(rowClazzName)) // 采用一般的HashMap来处理行
			return new MapRowMapper(charsetDB);
		if (SQLItem.RESULT_CLASS_JSON.equalsIgnoreCase(rowClazzName)) // 采用一般的HashMap来处理行
			return new JsonRowMapper(charsetDB);
		// 采用的是单一行类, 例如Integer, String, Short....
		if (innerClazzMap.get(rowClazzName) != null) return null;
		// 采用的是自定义的行处理类
		try
		{
			return new ClazzRowMapper(Class.forName(rowClazzName, false,
					Thread.currentThread().getContextClassLoader()), charsetDB);
		}
		catch (Exception e)
		{
			throw new RuntimeException("cannot find class:" + rowClazzName);
		}
	}

	public Class getInnerClazz(String rowClazzName)
	{
		return innerClazzMap.get(rowClazzName);
	}

	String charsetSQL; // SQL语句的字符集编码
	String charsetDB; // 从数据提取的数据字符集编码
	public final static Map<String, Class> innerClazzMap; // 内嵌类

	static
	{
		innerClazzMap = new HashMap<>();
		innerClazzMap.put("string", String.class);
		innerClazzMap.put("float", Float.class);
		innerClazzMap.put("double", Double.class);
		innerClazzMap.put("int", Integer.class);
		innerClazzMap.put("short", Short.class);
		innerClazzMap.put("boolean", Boolean.class);
		innerClazzMap.put("byte", Byte.class);
		innerClazzMap.put("long", Long.class);
		innerClazzMap.put("number", Number.class);
		innerClazzMap.put("Date", java.sql.Date.class);
		innerClazzMap.put("date", java.util.Date.class);
		innerClazzMap.put("time", java.sql.Time.class);
		innerClazzMap.put("timestamp", java.sql.Timestamp.class);
	}

	public void setCharsetDB(String charsetDB)
	{
		this.charsetDB = charsetDB;
	}

	public void setCharsetSQL(String charsetSQL)
	{
		this.charsetSQL = charsetSQL;
	}

	public String getDbType()
	{
		if (getDataSource() instanceof DynamicDataSource)
			return ((DynamicDataSource) getDataSource()).getCurrentDbType().toUpperCase();
		if (getDataSource() instanceof DruidDataSource)
			return ((DruidDataSource) getDataSource()).getDbType().toUpperCase();
		return null;
	}

	public String getDbName()
	{
		if (getDataSource() instanceof DynamicDataSource)
			return ((DynamicDataSource) getDataSource()).getCurrentDbName();
		if (getDataSource() instanceof DruidDataSource)
			return ((DruidDataSource) getDataSource()).getName();
		return null;
	}
}