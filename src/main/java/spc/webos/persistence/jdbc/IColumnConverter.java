package spc.webos.persistence.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * 持久层容许提取出来的结果再进行用户指定的列转换类来对实际值进行转换
 * 
 * @author spc
 * 
 */
public interface IColumnConverter
{
	// 列名， 列值
	Object convert(String column, Object value);

	public final static Map COLUMN_CONVERTER = new HashMap();
}
