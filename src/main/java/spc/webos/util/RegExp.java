package spc.webos.util;

import java.util.regex.Pattern;

public class RegExp
{
	public static boolean match(String input, Pattern p)
	{
		return p.matcher(input).matches();
	}

	public static final Pattern CHARSET_N = Pattern.compile("[0-9]*");
	public static final Pattern CHARSET_A = Pattern.compile("([a-z]|[A-Z]|[0-9])*");
	public static final Pattern CHARSET_X = Pattern.compile(
			"([a-z]|[A-Z]|[0-9]|\\.|,|-|_|\\(|\\)|/|=|'|\\+|:|\\?|!|\"|%|&|\\*|<|>|;|@|#| |\n)*");

	/**
	 * 匹配 hh:mm:ss 格式的时间数据
	 */
	public static final Pattern TIME = Pattern
			.compile("([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9])");
	public static final Pattern TIME_2 = Pattern
			.compile("([0-1][0-9]|[2][0-3])([0-5][0-9])([0-5][0-9])");

	/**
	 * 匹配yyyy-MM-dd的日期格式数据, 并对日期的日是否正确做判断
	 */
	public static final Pattern DATE = Pattern.compile(
			"(([1-2][0,9][0-9][0-9])-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))|(([1-2][0,9][0-9][0-9])-(0[469]|11)-([0][1-9]|[12][0-9]|30))|(([1-2][0,9][0-9][0-9])-02-(0[1-9]|1[0-9]|2[0-8]))|((([02468][048]00)|([13579][26]00)|([0-9][0-9][0][48])|([0-9][0-9][2468][048])|([0-9][0-9][13579][26]))-02-29)");
	public static final Pattern DATE_2 = Pattern.compile(
			"(([1-2][0,9][0-9][0-9])(0[13578]|10|12)(0[1-9]|[12][0-9]|3[01]))|(([1-2][0,9][0-9][0-9])(0[469]|11)([0][1-9]|[12][0-9]|30))|(([1-2][0,9][0-9][0-9])02(0[1-9]|1[0-9]|2[0-8]))|((([02468][048]00)|([13579][26]00)|([0-9][0-9][0][48])|([0-9][0-9][2468][048])|([0-9][0-9][13579][26]))0229)");

	/**
	 * datePattern 和 timePattern的结合, 匹配yyyy-MM-dd hh:mm:ss格式数据
	 */
	public static final Pattern TIMESTAMP = Pattern.compile(
			"(([1-2][0,9][0-9][0-9])-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))|(([1-2][0,9][0-9][0-9])-(0[469]|11)-([0][1-9]|[12][0-9]|30))|(([1-2][0,9][0-9][0-9])-02-(0[1-9]|1[0-9]|2[0-8]))|((([02468][048]00)|([13579][26]00)|([0-9][0-9][0][48])|([0-9][0-9][2468][048])|([0-9][0-9][13579][26]))-02-29) ([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9])");

	public static final Pattern IP = Pattern.compile(
			"([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");

	public static void main(String[] args) throws Exception
	{
		System.out.println(match("084959", TIME_2));
		System.out.println(match("20040230", DATE_2));
		System.out.println(match("12.1.1.0", IP));
		System.out.println(match("1a2.1.1.0", IP));
		// System.out.println(match("2004-02-29 08:49:59", Timestamp_Pattern));
	}
}
