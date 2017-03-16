package spc.webos.persistence.jdbc.rowmapper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import spc.webos.persistence.jdbc.IColumnConverter;

public abstract class AbstractRowMapper implements RowMapper
{
	public final static ThreadLocal COLUMN_CONVERTER = new ThreadLocal(); // 查询出来的字段是否需要语义转换
	public final static ThreadLocal COLUMN_CASE = new ThreadLocal(); // 字段是否大小写敏感，需要以特定的大小写放在Map里面
	protected Logger log = LoggerFactory.getLogger(getClass());

	public abstract String[] getColumnName();

	protected boolean isConverter(String column)
	{
		Map converters = (Map) COLUMN_CONVERTER.get();
		if (converters == null) return false;
		String name = (String) converters.get(column.toLowerCase());
		if (name == null) return false;
		IColumnConverter converter = (IColumnConverter) IColumnConverter.COLUMN_CONVERTER.get(name);
		return converter != null;
	}

	protected Object converter(String column, Object value)
	{
		Map converters = (Map) COLUMN_CONVERTER.get();
		if (converters == null) return value;
		String name = (String) converters.get(column.toLowerCase());
		if (name == null) return value;
		IColumnConverter converter = (IColumnConverter) IColumnConverter.COLUMN_CONVERTER.get(name);
		if (converter == null)
		{
			log.warn("can not find converter by name: " + name);
			return value;
		}
		return converter.convert(column, value);
	}
}
