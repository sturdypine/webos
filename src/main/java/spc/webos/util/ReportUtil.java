package spc.webos.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 报表的工具类
 * 
 * @author spc
 * 
 */
public class ReportUtil
{
	public final static ReportUtil REPORTUTIL = new ReportUtil();

	public static ReportUtil getInstance()
	{
		return REPORTUTIL;
	}

	public static int match(List data, String matches)
	{
		return match(data, (List) JsonUtil.json2obj(matches));
	}

	public static int match(List data, List matches)
	{
		if (data == null) return -1;
		for (int i = 0; i < data.size(); i++)
		{
			Object row = data.get(i);
			int j = 0;
			for (; j < matches.size(); j++)
			{
				List m = (List) matches.get(j);
				if (row instanceof List)
				{
					if (!m.get(1).equals(((List) row).get(Integer.parseInt(m.get(0).toString())))) break;
				}
				else
				{
					if (!m.get(1).equals(((Map) row).get(m.get(0)))) break;
				}
			}
			if (j >= matches.size()) return i;
		}
		return -1;
	}

	public static Object getValue(List data, List matches, String col, Object defaultValue)
	{
		return getValue(data, match(data, matches), defaultValue, col);
	}

	public static Object getValue(List data, String matches, String col, Object defaultValue)
	{
		return getValue(data, match(data, matches), defaultValue, col);
	}

	// 从结果集中匹配指定的两列所在的行
	public static int match(List data, Object c1, String v1, Object c2, String v2)
	{
		if (data == null) return -1;
		for (int i = 0; i < data.size(); i++)
		{
			Object r = data.get(i);
			if (r instanceof Map)
			{
				if (v1.equals(((Map) r).get(c1)) && v2.equals(((Map) r).get(c2))) return i;
			}
			else
			{ // List
				if (v1.equals(((List) r).get(Integer.parseInt(c1.toString())))
						&& v2.equals(((List) r).get(Integer.parseInt(c2.toString())))) return i;
			}
		}
		return -1;
	}

	public static Object getValue(List data, Object c1, String v1, Object c2, String v2,
			Object col, Object defaultValue)
	{
		return getValue(data, match(data, c1, v1, c2, v2), defaultValue, col);
	}

	public static double getMatchSumValue(List data, Object c1, String v1, Object col)
	{
		double sum = 0;
		for (int i = 0; i < data.size(); i++)
		{
			Object r = data.get(i);
			if (r instanceof Map)
			{
				if (v1.equals(((Map) r).get(c1))) sum += (col == null ? 1 : Double
						.parseDouble(StringX.null2emptystr(((Map) r).get(col), "0")));
			}
			else
			{ // List
				if (v1.equals(((List) r).get(Integer.parseInt(c1.toString())))) sum += (col == null ? 1
						: Double.parseDouble(StringX.null2emptystr(
								((List) r).get(Integer.parseInt(col.toString())), "0")));
			}
		}
		return sum;
	}

	public static double getMatchSumValue(List data, List matches, Object col)
	{
		if (data == null) return 0;
		double sum = 0;
		for (int i = 0; i < data.size(); i++)
		{
			Object row = data.get(i);
			int j = 0;
			for (; j < matches.size(); j++)
			{
				List m = (List) matches.get(j);
				if (row instanceof List)
				{
					if (!m.get(1).equals(((List) row).get(Integer.parseInt(m.get(0).toString()))))
					{
						sum += (col == null ? 1 : Double.parseDouble(StringX.null2emptystr(
								((List) row).get(Integer.parseInt(col.toString())), "0")));
					}
				}
				else
				{
					if (!m.get(1).equals(((Map) row).get(m.get(0))))
					{
						sum += (col == null ? 1 : Double.parseDouble(StringX.null2emptystr(
								((Map) row).get(col), "0")));
					}
				}
			}
		}
		return sum;
	}

	public static double getMatchAvgValue(List data, Object c1, String v1, Object col)
	{
		double d = getMatchSumValue(data, c1, v1, null);
		if (d < 1) return 0;
		return getMatchSumValue(data, c1, v1, col) / d;
	}

	public static double getMatctAvgValue(List data, List matches, Object col)
	{
		double d = getMatchSumValue(data, matches, null);
		if (d < 1) return 0;
		return getMatchSumValue(data, matches, col) / d;
	}

	// -------------712_20140808 end -------------

	// 一组针对报表结果需要进行行索引的简约写法
	public static Object getValue(List data, Map rowIndex, String row, Object defaultValue,
			String col)
	{
		if (rowIndex == null || data == null) return defaultValue;
		if (!rowIndex.containsKey(row)) return defaultValue;
		Integer r = (Integer) rowIndex.get(row);
		if (row == null || r == null) return defaultValue;
		return getValue(data, r.intValue(), defaultValue, col);
	}

	public static Object getValue(List data, int row, Object defaultValue, Object col)
	{
		Object value = null;
		if (data == null || row < 0 || row >= data.size()) return defaultValue;
		Object r = data.get(row);
		if (r instanceof List)
		{
			int c = Integer.parseInt(col.toString());
			if (c < 0 || c >= ((List) r).size()) return defaultValue;
			value = ((List) r).get(c);
		}
		else value = ((Map) r).get(col);
		// try
		// { // 如果传入的列col是数字类型,则认为是行结果集为List否则为Map
		// value = ((List) data.get(row)).get(Integer.parseInt(col));
		// }
		// catch (NumberFormatException nfe)
		// {
		// value = ((Map) data.get(row)).get(col);
		// }
		return value == null || (value instanceof String && ((String) value).length() == 0) ? defaultValue
				: value;
	}

	// 两列的和值相除
	public static Object getDivideValue(List data, Map rowIndex, List row1, List row2,
			Object defaultValue, String col)
	{
		Object n1 = getSumValue(data, rowIndex, row1, null, col);
		Object n2 = getSumValue(data, rowIndex, row2, null, col);
		try
		{
			double v2 = Double.parseDouble(n2.toString());
			if (n1 == null || n2 == null || v2 == 0) return defaultValue;
			return new Double(Double.parseDouble(n1.toString()) / v2);
		}
		catch (NumberFormatException nfe)
		{
		}
		return defaultValue;
	}

	public static Double divide(Object v1, Object v2, Object def)
	{
		double f = Double.parseDouble(v1.toString());
		double s = Double.parseDouble(v2.toString());
		if (Math.abs(s) < 0.00000000000000001) return new Double(def.toString());
		return new Double(f / s);
	}

	// 针对索引定位的某行, 对两列相除
	public static Object getDivideValue(List data, Map rowIndex, String row, Object defaultValue,
			String col1, String col2)
	{
		Object n1 = getValue(data, rowIndex, row, null, col1);
		Object n2 = getValue(data, rowIndex, row, null, col2);
		try
		{
			if (n1 == null || n2 == null || Double.parseDouble(n2.toString()) == 0) return defaultValue;
			return new Double(Double.parseDouble(n1.toString()) / Double.parseDouble(n2.toString()));
		}
		catch (NumberFormatException nfe)
		{
		}
		return defaultValue;
	}

	// 获取某一列的合计
	public static Object getColSumValue(List data, Object defaultValue, String col)
	{
		if (data == null || data.size() == 0) return defaultValue;
		double sum = 0;
		for (int i = 0; i < data.size(); i++)
		{
			Object value = getValue(data, i, defaultValue, col);
			sum += value == null ? 0 : Double.parseDouble(value.toString());
		}
		return new Double(sum);
	}

	// 指定的一批单元格合计
	public static Object getSumValue(List data, Map rowIndex, List cells, Object defaultValue)
	{
		if (rowIndex == null || data == null) return defaultValue;
		double sum = 0;
		Integer zero = new Integer(0);
		for (int i = 0; i < cells.size(); i++)
		{
			List cell = (List) cells.get(i);
			sum += Double.parseDouble(getValue(data, rowIndex, (String) cell.get(0), zero,
					(String) cell.get(1)).toString());
		}
		return new Double(sum);
	}

	// 指定一批索引行的某列合计
	public static Object getSumValue(List data, Map rowIndex, List rows, Object defaultValue,
			String col)
	{

		if (rowIndex == null || data == null) return defaultValue;
		double sum = 0;
		Integer zero = new Integer(0);
		for (int i = 0; i < rows.size(); i++)
			sum += Double.parseDouble(getValue(data, rowIndex, (String) rows.get(i), zero, col)
					.toString());
		return new Double(sum);
	}

	// 指定一批索引行的某列合计 同上
	public static Object getSumValue(List data, Map rowIndex, String[] rows, Object defaultValue,
			String col)
	{
		if (rowIndex == null || data == null) return defaultValue;
		double sum = 0;
		Integer zero = new Integer(0);
		for (int i = 0; i < rows.length; i++)
			sum += Double.parseDouble(getValue(data, rowIndex, (String) rows[i], zero, col)
					.toString());
		return new Double(sum);
	}

	// 指定一批索引行的某列均值
	public static Object getAvgValue(List data, Map rowIndex, List rows, Object defaultValue,
			String col)
	{
		if (rowIndex == null || data == null) return defaultValue;
		double sum = 0;
		int count = 0;
		for (int i = 0; i < rows.size(); i++)
		{
			Object value = getValue(data, rowIndex, (String) rows.get(i), null, col);
			if (value == null || (value instanceof String && ((String) value).length() == 0)) continue;
			sum += Double.parseDouble(value.toString());
			count++;
		}
		return count == 0 ? defaultValue : new Double(sum / count);
	}

	// 指定一批索引行的某列均值 同上
	public static Object getAvgValue(List data, Map rowIndex, String[] rows, Object defaultValue,
			String col)
	{
		if (rowIndex == null || data == null) return defaultValue;
		double sum = 0;
		int count = 0;
		for (int i = 0; i < rows.length; i++)
		{
			Object value = getValue(data, rowIndex, (String) rows[i], null, col);
			if (value == null || (value instanceof String && ((String) value).length() == 0)) continue;
			sum += Double.parseDouble(value.toString());
			count++;
		}
		return count == 0 ? defaultValue : new Double(sum / count);
	}

	// 返回符合正则表达式的行
	public static List getRegRow(List data, Map ri, String regRow)
	{
		List rows = new ArrayList();
		Pattern p = Pattern.compile(regRow);
		Iterator keys = ri.keySet().iterator();
		while (keys.hasNext())
		{
			String key = (String) keys.next();
			if (!p.matcher(key).matches()) continue;
			rows.add(key);
		}
		return rows;
	}

	// 指定的索引行上采用正则表达式进行统计, 适合于机构代码有规则,需要进行各级部门进行小计的情况
	// 统计结果为 小计, 总记录数, 均值
	public static Object[] getRegStat(List data, Map ri, String regRow, String col)
	{
		double sum = 0;
		int count = 0;
		Integer zero = new Integer(0);
		Pattern p = Pattern.compile(regRow);
		Iterator keys = ri.keySet().iterator();
		while (keys.hasNext())
		{
			String key = (String) keys.next();
			if (!p.matcher(key).matches()) continue;
			count++;
			sum += Double.parseDouble(getValue(data, ((Integer) ri.get(key)).intValue(), zero, col)
					.toString());
		}
		return new Object[] { new Double(sum), new Integer(count),
				count <= 0 ? new Double(0) : new Double(sum / count) };
	}

	// 对指定的起始行到结束行的指定列进行统计
	public static double getSumValue(List data, String start, String end, String col)
	{
		double sum = 0;
		Integer zero = new Integer(0);
		int s = Integer.parseInt(start);
		int e = Integer.parseInt(end);
		for (int i = s; i <= e; i++)
			sum += Double.parseDouble(getValue(data, i, zero, col).toString());
		return sum;
	}
}
