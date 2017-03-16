package spc.webos.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import spc.webos.constant.Common;
import spc.webos.util.tree.TreeNode;

/**
 * <pre>
 * Usage:
 * 
 *  StringX.left 		(&quot;abc__cdf__fgh&quot;, &quot;__&quot;); 	// return &quot;abc&quot;
 *  StringX.leftback 	(&quot;abc__cdf__fgh&quot;, &quot;__&quot;); 	// return &quot;abc__cdf&quot;
 *  StringX.right 		(&quot;abc__cdf__fgh&quot;, &quot;__&quot;); 	// return &quot;cdf__fgh&quot;
 *  StringX.rightback 	(&quot;abc__cdf__fgh&quot;, &quot;__&quot;); 	// return &quot;fgh&quot;
 *  StringX.between 	(); 
 *  StringX.replace 	(&quot;hello world&quot;, &quot;world&quot;, &quot;web-report&quot;); 				// return &quot;hello web-report&quot;
 *  StringX.replace 	(&quot;hello world&quot;, {&quot;hello&quot;,&quot;world&quot;}, {&quot;hi&quot;, &quot;money&quot;}); 	// return &quot;hi money&quot;
 *  StringX.replaceAll 	();
 *  StringX.replaceFirst(); 						// == replace ()
 *  StringX.replaceBetween ();
 *  StringX.trim ();								// &quot; ab   cde  &quot; --&gt; &quot;abcde&quot;
 *  StringX.split 		(&quot;this--is--good&quot;, &quot;--&quot;);	// return new String[]{&quot;this&quot;, &quot;is&quot;, &quot;good&quot;}
 *  StringX.split		(&quot;1,'ab,cd',2&quot;,&quot;,&quot;);		// {1, 'ab, cd', 2}
 *  StringX.split		(&quot;1,'ab,cd',2&quot;,&quot;,&quot;,&quot;\'&quot;);	// {1, 'ab,cd', 2}
 *  StringX.split2ints  (&quot;3,2,6,4&quot;);				// return {3,2,6,4}
 *  StringX.join ({&quot;aa&quot;,&quot;bb&quot;,&quot;cc&quot;}, &quot;,&quot;, &quot;(&quot;, &quot;)&quot;)	// return &quot;(aa),(bb),(cc)&quot;
 *  StringX.join		({&quot;aa&quot;,&quot;bb&quot;,&quot;cc&quot;}, &quot; | &quot;)	// return &quot;aa | bb | cc&quot;
 *  StringX.join		({100,300,200}, &quot;,&quot;)		// return &quot;100,300,200&quot;
 *  StringX.join		({false,true,false}, &quot;,&quot;)	// return &quot;false,true,false&quot;
 *  StringX.unique		({&quot;aa&quot;, &quot;aa&quot;, &quot;cc&quot;, &quot;aa&quot;})	// return &quot;aa&quot;, &quot;cc&quot;
 *  StringX.set			({&quot;aa&quot;, &quot;aa&quot;, &quot;cc&quot;, &quot;aa&quot;})	// return &quot;aa&quot;, &quot;cc&quot;
 *  StringX.isSet		({&quot;aa&quot;, &quot;aa&quot;, &quot;cc&quot;, &quot;aa&quot;})	// return false
 *  StringX.sort		({&quot;bb&quot;, &quot;aa&quot;, &quot;cc&quot;})		// return &quot;aa&quot;, &quot;bb&quot;, &quot;cc&quot;
 *  StringX.group		({&quot;aa&quot;, &quot;aa&quot;, &quot;cc&quot;, &quot;aa&quot;});	// return {{&quot;aa&quot;,&quot;cc&quot;}, {3,1}}
 *  StringX.include 	(&quot;aa&quot;, {&quot;bb&quot;, &quot;cc&quot;, &quot;aa&quot;})	// return true	
 *  StringX.in 			(&quot;aa&quot;, {&quot;bb&quot;, &quot;cc&quot;, &quot;aa&quot;})	// return true	
 *  StringX.isSubset 	({&quot;aa&quot;, &quot;bb&quot;}, {&quot;bb&quot;, &quot;cc&quot;, &quot;aa&quot;})	// return true		
 *  StringX.indexOf     (&quot;aa&quot;, {&quot;bb&quot;, &quot;cc&quot;, &quot;aa&quot;}); // return 2
 *  StringX.insert		(&quot;aa.bb.xml&quot;, &quot;.&quot;, &quot;_out&quot;);	// return aa_out.bb.xml
 *  StringX.insertBack	(&quot;aa.bb.xml&quot;, &quot;.&quot;, &quot;_out&quot;);	// return aa.bb_out.xml
 *  -------------- following methods is for jsp --------------
 *  StringX.base64encode (&quot;abc&quot;);					// return base64string
 *  StringX.base64decode (&quot;base64string&quot;);			// return &quot;abc&quot;
 *  StringX.md5 (&quot;a&quot;);								// return string of 32 length
 *  StringX.check		(&quot;&quot;, &quot;default&quot;);		   	// return &quot;default&quot;
 *  StringX.nullity		(&quot;&quot;);						// return true;
 *  StringX.isTrue		(&quot;True&quot;);					// return true;
 *  StringX.getRealPath (application);		   		// return &quot;/webfirst/webapp/webreport/&quot;
 *  StringX.getRealPath (application, uri);			// return &quot;/webfirst/webapp/webreport/res/&quot;
 *  StringX.getTemplate (application, request);		// return related template file name.
 *  -------------- following methods is for expression -------
 *  StringX.getExpVars (&quot;A * (B - C/2)&quot;);			// return {&quot;A&quot;,&quot;B&quot;,&quot;C&quot;}
 *  -------------- 
 *  StringX.map2array (Map);						// return String[2][]
 *  StringX.collection2array (c);				// return String[]
 *  StringX.list2array (List);						// return String[]
 *  --------------
 *  StringX.format (&quot;{0}'s age is {1}&quot;, new Object[]{...})	// return &quot;James's age is 100&quot;
 * 
 * 
 * </pre>
 * 
 * @see NumberX, DatetimeX
 * @see TestStringX
 */
public class StringX
{
	// public final static ClassObjectPool TABLE_STRBUF_POOL = new
	// ClassObjectPool(StringBuffer.class,
	// 20);
	public final static String ZEROS = "00000000000000000000000000000000"; // 最多支持32位
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	public static final String EMPTY_STRING = "";
	public static final String COMMA = ",";
	public static final String DIGEST_MD5 = "MD5";
	public final static StringX STRINGX = new StringX();
	public final static char[] TRIM_CHAR = new char[] { ' ', '\t', '\n', '\r' };

	public static String[] last2path(String uri)
	{
		String[] paths = StringX.split(uri, "/");
		return new String[] { paths[paths.length - 2], paths[paths.length - 1] };
	}

	public static Map<String, String> uri2params(String uri, int start)
	{
		return uri2params(StringX.split(uri, "/"), start);
	}

	public static Map<String, String> uri2params(String[] paths, int start)
	{
		Map<String, String> params = new HashMap<>();
		if (paths == null || paths.length < 2) return params;
		for (int i = paths.length - 2; i > start; i -= 2)
			if (!StringX.nullity(paths[i])) params.put(paths[i], paths[i + 1]);
		return params;
	}

	public static String escapeHtml4(String value)
	{
		return StringEscapeUtils.escapeHtml4(value);
	}
	// public static boolean containUnvalidXmlChar(byte[] buf)
	// {
	// if (buf == null) return false;
	// for (byte b : buf)
	// if (isUnvalidXmlByte(b)) return true;
	// return false;
	// }
	//
	// public static boolean isUnvalidXmlByte(byte b)
	// {
	// if ((b & 0x80) > 0) return false; // 如果高位为1, 表示非ASC字符
	// if (!(0x00 < b && b < 0x08 || 0x0b < b && b < 0x0c || 0x0e < b && b <
	// 0x1f)) return true;
	// return false;
	// }

	public static String removeBshAnnotation(String bsh)
	{
		if (StringX.nullity(bsh)) return bsh;
		bsh = StringX.trim(bsh);
		String[] lines = StringX.split(bsh, "\n");
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < lines.length; i++)
		{
			String line = StringX.trim(lines[i]);
			if (line.startsWith("//")) continue;
			int idx = line.indexOf(';');
			if (idx < 0)
			{
				buf.append(line + "\n");
				continue;
			}
			int index = line.indexOf("//", idx);
			if (index > 0) buf.append(line.substring(0, index) + "\n");
			else buf.append(line + "\n");
		}
		return buf.toString();
	}

	public static boolean containUnvalidXmlChar(String xml)
	{
		for (int i = 0; i < xml.length(); i++)
		{
			char c = xml.charAt(i);
			if (isUnvalidXMLChar(c)) return true;
		}
		return false;
	}

	public static String removeUnvalidXmlChar(String xml)
	{
		return removeUnvalidXmlChar(xml, null);
	}

	public static String removeUnvalidXmlChar(String xml, String replace)
	{
		StringBuilder sbud = new StringBuilder(xml.length());
		for (int i = 0; i < xml.length(); i++)
		{
			char c = xml.charAt(i);
			if (!isUnvalidXMLChar(c)) sbud.append(c);
			else if (replace != null) sbud.append(replace);
		}
		return sbud.toString();
	}

	public static boolean isUnvalidXMLChar(char c)
	{
		return ((0x00 < c && c < 0x08) || (0x0b < c && c < 0x0c) || (0x0e < c && c < 0x1f));
	}

	public static boolean equals(Object o1, Object o2)
	{
		if (o1 == o2) return true;
		if (o1 == null || o2 == null) return false;
		return o1.toString().equals(o2.toString());
	}

	/**
	 * <root><a><c>c</></><b>b</></> 变为 <root><a><c>c</c></a><b>b</b></root>
	 * 
	 * @param xml
	 * @return
	 */
	public static String untrimXML(String xml)
	{
		StringBuilder buf = new StringBuilder();
		int tagStart = -1, tagEnd = -1;
		Stack<String> tag = new Stack<String>();
		for (int i = 0; i < xml.length(); i++)
		{
			char ch = xml.charAt(i);
			buf.append(ch);
			if (i > 0 && i < xml.length() - 1 && xml.charAt(i - 1) == '<' && ch == '/'
					&& xml.charAt(i + 1) == '>')
			{
				// System.out.println("peek: " + tag.peek());
				buf.append(tag.pop());
				tagStart = tagEnd = -1;
				continue;
			}
			if (ch == '<') tagStart = i + 1;
			if (ch == '>') tagEnd = i;
			if (tagStart >= 0 && tagEnd > tagStart)
			{
				// System.out.println("tag:" + xml.substring(tagStart, tagEnd));
				tag.push(xml.substring(tagStart, tagEnd));
				tagStart = tagEnd = -1;
			}

		}
		return buf.toString();
	}

	/**
	 * <root><a><c>c</c></a><b>b</b></root> 变为 <root><a><c>c</></><b>b</></>
	 * 
	 * @param xml
	 * @return
	 */
	public static String trimXML(String xml)
	{
		StringBuilder buf = new StringBuilder();
		boolean isEndTag = false;
		for (int i = 0; i < xml.length(); i++)
		{
			char ch = xml.charAt(i);
			if ((i > 0 && xml.charAt(i - 1) == '<') && ch == '/')
			{
				isEndTag = true;
				buf.append(ch);
			}
			else if (ch == '>') isEndTag = false;
			if (!isEndTag) buf.append(ch);
		}
		return buf.toString();
	}

	/*
	 * Verify that no character has a hex value greater than 0xFFFD, or less
	 * than 0x20. Check that the character is not equal to the tab (
	 * "t), the newline ("n), the carriage return ("r), or is an invalid XML
	 * character below the range of 0x20. If any of these characters occur, an
	 * exception is thrown.
	 */
	public static String checkXMLUnicode(String str) throws Exception
	{
		int i = 0;
		for (i = 0; i < str.length(); ++i)
		{
			char ch = str.charAt(i);
			if (ch > 0xFFFD || (ch < 0x20 && ch != '\t' & ch != '\n' & ch != '\r')) break;
		}
		if (i >= str.length()) return str;

		StringBuilder buf = new StringBuilder();
		for (i = 0; i < str.length(); ++i)
		{
			char ch = str.charAt(i);
			if (ch > 0xFFFD || (ch < 0x20 && ch != '\t' & ch != '\n' & ch != '\r')) continue;
			buf.append(ch);
		}
		return buf.toString();
	}

	public static Map<String, Object> xml2map(String xml) throws Exception
	{
		XMLReader parser = getXMLReader();
		MapSaxHandler handler = new MapSaxHandler();
		Map<String, Object> root = new HashMap<>();
		handler.setRoot(root);
		parser.setErrorHandler(handler);
		parser.setContentHandler(handler);
		parser.parse(new InputSource(new ByteArrayInputStream(xml.getBytes())));
		return root;
	}

	public static String map2xml(String name, Map<String, Object> map)
	{
		if (map == null || map.isEmpty()) return "";
		StringBuilder s = new StringBuilder().append('<').append(name).append('>');
		map.forEach((k, v) -> {
			if (v instanceof Map) s.append(map2xml(k, (Map<String, Object>) v));
			else if (v instanceof List) s.append(list2xml(k, (List<Object>) v));
			else s.append("<").append(k).append(">").append(str2xml(v.toString(), true))
					.append("</").append(k).append(">");
		});
		s.append("</").append(name).append(">");
		return s.toString();
	}

	public static String list2xml(String name, List<Object> list)
	{
		if (list == null || list.isEmpty()) return "";
		StringBuilder s = new StringBuilder();
		list.forEach((v) -> {
			if (v instanceof Map) s.append(map2xml(name, (Map<String, Object>) v));
			else if (v instanceof List) s.append(list2xml(name, (List<Object>) v));
			else s.append("<").append(name).append(">").append(str2xml(v.toString(), true))
					.append("</").append(name).append(">");
		});
		return s.toString();
	}

	// byte[]转换为hex字符串
	public static String byte2hex(byte[] b)
	{
		StringBuilder buf = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++)
		{
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) buf.append('0');
			buf.append(hex.toUpperCase());
		}
		return buf.toString();
	}

	// hex字符串转换为byte[]
	public static byte[] hex2byte(String hex)
	{
		byte[] bts = new byte[hex.length() / 2];
		for (int i = 0, j = 0; j < bts.length; j++)
		{
			bts[j] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
			i += 2;
		}
		return bts;
	}

	public static String removeNotInSpecificChar(String str, char min, char max)
	{
		if (StringX.nullity(str)) return str;
		StringBuilder buf = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (ch < min || ch > max) continue;
			buf.append(ch);
		}
		return buf.toString();
	}

	public static boolean isAllSpecificChar(String str, char specificChar)
	{
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) != specificChar) return false;
		return true;
	}

	public static boolean isAllSpecificChar(String str, String specificStr)
	{
		for (int i = 0; i < str.length(); i++)
			if (specificStr.indexOf(str.charAt(i)) < 0) return false;
		return true;
	}

	public static boolean isAllSpecificChar(String str, char min, char max)
	{
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			if (ch < min || ch > max) return false;
		}
		return true;
	}

	public static boolean isAllSpecificChar(String str, char[][] range)
	{
		for (int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			boolean ok = false;
			for (int j = 0; j < range.length; j++)
			{
				if (ch >= range[j][0] && ch <= range[j][1])
				{
					ok = true;
					break;
				}
			}
			if (!ok) return false;
		}
		return true;
	}

	public static boolean contain(String[] keys, String key, boolean ignoreCase)
	{
		if (keys == null) return false;
		for (int i = 0; i < keys.length; i++)
		{
			String k = keys[i].trim();
			if (k.equals(key) || (ignoreCase && k.equalsIgnoreCase(key))) return true;
		}
		return false;
	}

	/**
	 * 根据参数个数自动生成消息格式MessageFormat
	 * 
	 * @param args
	 * @return
	 */
	public static String getMessageFormat(int args)
	{
		if (args == 0) return EMPTY_STRING;
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < args; i++)
		{
			if (i != 0) buf.append(" - ");
			buf.append('{');
			buf.append(i);
			buf.append('}');
		}
		return buf.toString();
	}

	// 把一个map信息变成一个类似url查询的格式
	public static StringBuilder map2queryString(Map map)
	{
		StringBuilder buf = new StringBuilder();
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			Object v = map.get(key);
			if (buf.length() > 0) buf.append('&');
			buf.append(key);
			buf.append('=');
			buf.append(v);
		}
		return buf;
	}

	// 从字符串的末尾找出某字符倒序出现的某次数的位置
	public static int lastIndexOf(String str, int ch, int times)
	{
		for (int i = str.length() - 1; i >= 0; i--)
		{
			if (ch == str.charAt(i))
			{
				times--;
				if (times <= 0) return i;
			}
		}
		return 0;
	}

	public static String printStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static String stackTrace(StackTraceElement[] stes, int depth)
	{
		StringBuilder buf = new StringBuilder();
		if (stes == null && depth == 0) return buf.toString();
		for (int i = 0; i < stes.length && (depth < 0 || i < depth); i++)
		{
			buf.append(stes[i]);
			buf.append('\n');
		}
		return buf.toString();
	}

	// 把一个list中的字符串用墨个前缀和一个分隔符连接成一个字符串
	public static String arrayToDelimitedString(List arr, String prefix, String delim)
	{
		if (arr == null) return StringX.EMPTY_STRING;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.size(); i++)
		{
			if (i > 0) sb.append(delim);
			sb.append(prefix + arr.get(i));
		}
		return sb.toString();
	}

	// 判断一个字符串是否可以是一个合法的程序变量名
	public static boolean isVariableName(String name)
	{
		if (!((name.charAt(0) >= 'a' && name.charAt(0) <= 'z')
				|| (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z')))
			return false;
		for (int i = 1; i < name.length(); i++)
		{
			char ch = name.charAt(i);
			if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')
					|| ch == '_'))
				return false;
		}
		return true;
	}

	public static Date timeMillis2Date(String timeMillis)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(Long.parseLong(timeMillis));
		return c.getTime();
	}

	// 把一个字符串变成一个xml文本字符串，转换&, <, >, 或者加上cdata
	public static String str2xml(String v, boolean cdata)
	{
		if (cdata && ((v.indexOf('<') >= 0 || v.indexOf('>') >= 0 || v.indexOf('&') >= 0)))
			return "<![CDATA[" + v + "]]>";
		if (v.indexOf('&') >= 0) v = StringX.replaceAll(v, "&", "&amp;");
		if (v.indexOf('<') >= 0) v = StringX.replaceAll(v, "<", "&lt;");
		if (v.indexOf('>') >= 0) v = StringX.replaceAll(v, ">", "&gt;");
		return v;
	}

	// 把一个xml文本内容变成真正的字符串，做<, >转义
	public static String xml2str(String v)
	{
		v = StringX.replaceAll(v, "&lt;", "<");
		v = StringX.replaceAll(v, "&gt;", ">");
		v = StringX.replaceAll(v, "&apos;", "'");
		v = StringX.replaceAll(v, "&quot;", "\"");
		v = StringX.replaceAll(v, "&amp;", "&");
		return v;
	}

	// 把一个字符串转化为html能显示的字符串
	public static String toHTML(String text)
	{
		return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
				.replaceAll("\n", "<br>").replaceAll(" ", "&nbsp;")
				.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
	}

	// 发送URL变成xml
	public static String url2xml(String text)
	{
		return text.replaceAll("%3C", "<").replaceAll("%3E", ">").replaceAll("%2F", "/")
				.replaceAll("%20", " ").replaceAll("%09", "\t");
	}

	// 把一个treenode节点变成ext的json格式.由于一般ext的跟节点信息在界面已经提供,所以要除去根节点
	public static String treeNode2ExtJson(TreeNode n, boolean root)
	{
		StringBuilder buf = new StringBuilder(100);
		if (root)
		{
			buf.append("{text:\"");
			buf.append(StringX.str2utf8(n.getText()));
			buf.append("\", children:");
		}
		buf.append('[');
		boolean first = true;
		for (TreeNode node : n.getChildren())
		{
			if (!first) buf.append(',');
			first = false;
			buf.append(StringX.str2utf8(JsonUtil.obj2json(node.toJson())));
		}
		buf.append(']');
		if (root) buf.append('}');
		return buf.toString();
	}

	// 把一个table变成ext grid json
	public static String table2extgrid(Collection table, Integer total, Integer start,
			Integer limit, List column, boolean utf8) throws Exception
	{
		if (table == null || table.size() == 0)
			return "{\"success\":true,\"total\":0,\"start\":" + start + ",\"data\":[]}";
		StringBuilder buf = new StringBuilder("{\"success\":true,\"total\":");
		buf.append(total != null ? total : table.size());
		buf.append(",\"start\":").append(start != null ? start : 0);
		if (limit != null) buf.append(",\"limit\":").append(limit);
		buf.append(",\"data\":[");
		boolean first = true;
		for (Object row : table)
		{
			if (row == null) continue;
			String s = null;
			if (row instanceof StringBuilder || row instanceof String
					|| row instanceof StringBuffer)
				s = row.toString();
			else if (row instanceof List) s = StringX.array2json(column, (List) row);
			// else if (row instanceof IJson) s = ((IJson)
			// row).toJson().toString();
			else s = JsonUtil.obj2json(row);
			if (utf8) s = StringX.str2utf8(s); // 将汉字转换为utf8格式，\\u34\\u56
			if (!first) buf.append(',');
			first = false;
			buf.append(s);
		}
		buf.append("]}");
		return buf.toString();
	}

	// 把一个数组用指定的列名变成json字符串
	public static String array2json(List columnName, List column) throws Exception
	{
		ObjectMapper m = new ObjectMapper();
		StringBuilder buf = new StringBuilder();
		buf.append('{');
		for (int i = 0; i < column.size(); i++)
		{
			if (i != 0) buf.append(',');
			buf.append("\"");
			buf.append(columnName.get(i));
			buf.append("\":");
			buf.append(m.writeValueAsString(StringX.null2emptystr(column.get(i))));
		}
		buf.append('}');
		return buf.toString();
	}

	public static String array2json(Object[] columnName, List column)
	{
		StringBuilder buf = new StringBuilder();
		buf.append('{');
		for (int i = 0; i < column.size(); i++)
		{
			if (i != 0) buf.append(',');
			buf.append(columnName[i]);
			buf.append(":'");
			String str = column.get(i).toString();
			if (str.indexOf('\'') >= 0) str = str.replace("'", "\\'");
			if (str.indexOf('\n') >= 0) str = str.replace("\n", "\\n");
			if (str.indexOf('\r') >= 0) str = str.replace("\r", "");
			buf.append(str);
			buf.append('\'');
		}
		buf.append('}');
		return buf.toString();
	}

	// 把整数变为指定长度的字符串，前面用000补齐
	public static String int2str(String number, int len)
	{
		boolean negative = number.charAt(0) == '-';
		if (negative) number = number.substring(1);
		StringBuilder buf = new StringBuilder(ZEROS);
		buf.append(number);
		return negative ? '-' + buf.substring(buf.length() - len + 1)
				: buf.substring(buf.length() - len);
	}

	// 后补0保持小数位数
	public static String float2str(String number, int decimal)
	{
		// 711_20140729 如果number为空字符串则直接返回
		if (decimal <= 0 || StringX.nullity(number)) return number;
		int index = number.indexOf('.');
		if (index < 0) return number + '.' + ZEROS.substring(ZEROS.length() - decimal);
		String intPart = number.substring(0, index);
		String decimalPart = number.substring(index + 1) + ZEROS;
		return intPart + '.' + decimalPart.substring(0, decimal);
	}

	// 把浮点数用Decimal(18,2)字符串表示，前后用0补齐，18表示总长度（不含小数点）,2表示小数点位数
	public static String float2str(String number, int len, int decimal, boolean withDot)
	{
		if (decimal <= 0) withDot = false; // added by chenjs 2011-08-23
											// 如果浮点类型没有小数点位数设置则没有.
		boolean negative = (number.charAt(0) == '-');
		if (negative) number = number.substring(1);
		char dot = '.';
		int index = number.indexOf(dot);
		String temp = null;
		temp = ZEROS + (index >= 0 ? number.substring(0, index) : number);
		String value = temp.substring(temp.length() - len + decimal + (withDot ? 1 : 0));
		if (withDot) value += dot;
		temp = index >= 0 ? number.substring(index + 1) + ZEROS : ZEROS;
		value += temp.substring(0, decimal);
		return negative ? ('-' + value.substring(1)) : value.toString();
	}

	// 判断一个字符串是否含有乱码
	public static boolean isMessyCode(String str)
	{
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(str);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		for (int i = 0; i < ch.length; i++)
		{
			char c = ch[i];
			if (!Character.isLetterOrDigit(c) && !isCN(c)) count++;
		}
		float result = count / chLength;
		return result > 0.4;
	}

	// 判断一个字符串里面是否包含中文字符
	public static boolean isContainCN(String str)
	{
		if (str == null) return false;
		for (int i = 0; i < str.length(); i++)
			if (isCN(str.charAt(i))) return true;
		return false;
	}

	public static boolean isCN(char c)
	{
		// return c > 255;
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
	}

	// 判断一个字符串转化为BCD码的长度，因为BCD码中中文部分的前后会多一个固定字符
	public static int bcdLength(String str)
	{
		int len = 0;
		boolean cn = false;
		for (int i = 0; i < str.length(); i++)
		{
			if (isCN(str.charAt(i)))
			{
				if (!cn) len++;
				len += 2;
				cn = true;
			}
			else
			{
				if (cn) len++;
				len++;
				cn = false;
			}
		}
		return len;
	}

	public static String bcd2str(byte[] bytes)
	{
		StringBuilder temp = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++)
		{
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1)
				: temp.toString();
	}

	public static byte[] str2bcd(String asc)
	{
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0)
		{
			asc = "0" + asc;
			len = asc.length();
		}
		byte abt[] = new byte[len];
		if (len >= 2) len = len / 2;
		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++)
		{
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) j = abt[2 * p] - '0';
			else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) j = abt[2 * p] - 'a' + 0x0a;
			else j = abt[2 * p] - 'A' + 0x0a;

			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) k = abt[2 * p + 1] - '0';
			else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z'))
				k = abt[2 * p + 1] - 'a' + 0x0a;
			else k = abt[2 * p + 1] - 'A' + 0x0a;

			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	public static String byte2binary(byte b)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 7; i >= 0; --i)
			buf.append(((1 << i) & b) == 0 ? '0' : '1');
		return buf.toString();
	}

	public static String bytes2binary(byte[] bs, String delim)
	{
		return bytes2binary(bs, 0, bs.length, delim);
	}

	public static String bytes2binary(byte[] bs, int offset, int len, String delim)
	{
		StringBuilder buf = new StringBuilder();
		for (int j = offset; j < offset + len; j++)
		{
			if (buf.length() > 0) buf.append(delim);
			byte b = bs[j];
			for (int i = 7; i >= 0; --i)
				buf.append(((1 << i) & b) == 0 ? '0' : '1');
		}
		return buf.toString();
	}

	// 把字节数组打印为原始数字串
	public static String bytes2str(byte[] bytes, int column)
	{
		if (bytes == null || bytes.length == 0) return StringX.EMPTY_STRING;
		StringBuilder buf = new StringBuilder();
		int rows = bytes.length / column;
		for (int i = 0; i <= rows; i++)
		{
			StringBuilder byteBuf = new StringBuilder();
			StringBuilder textBuf = new StringBuilder();
			int rowLen = column < bytes.length - i * column ? column : bytes.length - i * column;
			for (int j = 0; j < rowLen; j++)
			{
				// byte b = bytes[i * column + j];
				int intVal = (int) bytes[i * column + j];
				String hexString = Integer.toHexString(intVal);
				if (hexString.length() == 1) hexString = "0" + hexString;
				else if (hexString.length() > 2)
					hexString = hexString.substring(hexString.length() - 2);
				byteBuf.append(hexString + " ");

				if (intVal <= 127 && intVal >= 32) textBuf.append((char) intVal);
				else textBuf.append('.');
			}
			if (byteBuf.length() < column * 3)
			{
				int spacesLen = column * 3 - byteBuf.length();
				for (int k = 0; k < spacesLen; k++)
					byteBuf.append(' ');
			}
			if (textBuf.length() < column)
			{
				int spacesLen = column - textBuf.length();
				for (int k = 0; k < spacesLen; k++)
					textBuf.append(' ');
			}
			buf.append(byteBuf.toString() + "   " + textBuf.toString());
			if (rows > 1) buf.append(System.getProperty("line.separator"));
		}
		return buf.toString();
	}

	public static String str2utf8(String str)
	{
		if (str == null) return StringX.EMPTY_STRING;
		String tmp;
		StringBuilder sb = new StringBuilder();
		char c;
		int i, j;
		sb.setLength(0);
		for (i = 0; i < str.length(); i++)
		{
			c = str.charAt(i);
			if (c > 255)
			{
				sb.append("\\u");
				j = (c >>> 8);
				tmp = Integer.toHexString(j);
				if (tmp.length() == 1) sb.append('0');
				sb.append(tmp);
				j = (c & 0xFF);
				tmp = Integer.toHexString(j);
				if (tmp.length() == 1) sb.append('0');
				sb.append(tmp);
			}
			else sb.append(c);
		}
		return sb.toString();
	}

	public static String utf82str(String str)
	{
		if (str == null) return null;
		if (str.indexOf("\\u") < 0) return str; // 说明里面不含有\\u8989 utf转码的中文字符
		StringBuilder sb = new StringBuilder();
		char c;
		sb.setLength(0);
		for (int i = 0; i < str.length(); i++)
		{
			c = str.charAt(i);
			if (c == '\\' && i + 1 < str.length() && str.charAt(i + 1) == 'u'
					&& i + 5 < str.length())
			{
				sb.append(utf8Ch2Str(str.substring(i + 2, i + 6)));
				i += 5;
			}
			else sb.append(c);
		}
		return sb.toString();
	}

	static String utf8Ch2Str(String str)
	{
		try
		{
			int c1 = Integer.parseInt(str.substring(0, 2), 16);
			int c2 = Integer.parseInt(str.substring(2, 4), 16);
			return String.valueOf((char) ((c1 << 8) | c2));
		}
		catch (Exception e)
		{
		}
		return str;
	}

	public static List delimitedList(String str, String delimiter)
	{
		List result = new ArrayList();
		if (str == null) { return result; }
		if (delimiter == null)
		{
			result.add(str);
			return result;
		}
		int pos = 0;
		int delPos = 0;
		while ((delPos = str.indexOf(delimiter, pos)) != -1)
		{
			result.add(str.substring(pos, delPos));
			pos = delPos + delimiter.length();
		}
		if (str.length() > 0 && pos <= str.length()) result.add(str.substring(pos));
		return result;
	}

	/**
	 * 把一个字符串数组用给定的隔离字符串隔离为一个字符串
	 * 
	 * @param array
	 * @param delim
	 * @return
	 */
	public static StringBuffer splitArray(String[] array, String delim)
	{
		StringBuffer strBuf = new StringBuffer(50);
		if (array == null || array.length < 1)
		{
			strBuf.append("");
			return strBuf;
		}
		strBuf.append(array[0]);
		for (int i = 1; i < array.length; i++)
		{
			strBuf.append(delim);
			strBuf.append(array[i]);
		}
		return strBuf;
	}

	public static String map2str(Map m, char delim)
	{
		return map2str(m, String.valueOf(delim));
	}

	public static String map2str(Map m, String delim)
	{
		if (m == null || m.size() == 0) return StringX.EMPTY_STRING;
		StringBuilder buf = new StringBuilder(50);
		Iterator keys = m.keySet().iterator();
		while (keys.hasNext())
		{
			if (buf.length() > 0) buf.append(delim);
			Object key = keys.next();
			buf.append(key);
			buf.append('=');
			buf.append(m.get(key));
		}
		return buf.toString();
	}

	public static Map str2map(String queryString, char delim)
	{
		return str2map(queryString, String.valueOf(delim));
	}

	public static Map str2map(String queryString, String delim)
	{
		return str2map(queryString, delim, new HashMap());
	}

	/**
	 * 把url上带的QueryString分解为一个Map对象, 参数名为Key String queryString =
	 * request.getQueryString(); Map paramMap =
	 * XStringUtil.queryStringToMap(queryString);
	 * 
	 * @param queryString
	 * @return
	 */
	public static Map str2map(String queryString, String delim, Map paramMap)
	{
		// System.out.println("queryString = "+queryString);
		if (queryString == null || queryString.length() < 3) { return null; }
		int lastIndex = 0;
		int currentIndex = queryString.indexOf(delim);
		if (currentIndex < 0) currentIndex = queryString.length();
		while (currentIndex > 0)
		{
			String str = queryString.substring(lastIndex, currentIndex);
			// System.out.print("debug = '" + str + "', ");
			int index = str.indexOf('=');
			if (index > 0)
			{
				String paramName = str.substring(0, index);
				String paramValue = str.substring(index + 1);
				if (index > 0 && index < str.length()) paramMap.put(paramName, paramValue);
				// System.out.println(paramName + ", " + paramValue);
			}
			lastIndex = currentIndex + 1;
			currentIndex = queryString.indexOf(delim, lastIndex);
			if (lastIndex < queryString.length() && currentIndex < 0
					&& currentIndex != queryString.length())
				currentIndex = queryString.length();
		}
		return paramMap;
	}

	// 字符串是否以数字开头
	public static boolean startsWithNumber(String str)
	{
		if (nullity(str)) return false;
		if (str.charAt(0) >= '0' && str.charAt(0) <= '9') return true;
		return false;
	}

	// spc end
	/**
	 * @author jamesqiu 2006-11-21 capitalize a string (uppercase first char)
	 */
	public static String capitalize(String src)
	{
		if (src == null || src.length() == 0) return src;
		char[] cs = src.toCharArray();
		cs[0] = Character.toUpperCase(cs[0]);
		return new String(cs);
	}

	/**
	 * @author jamesqiu 2006-11-21 like capitalize but uppercase first char AND
	 *         lowercase other chars
	 * @param more
	 *            set as true/false are both ok
	 */
	public static String capitalize(String src, boolean more)
	{
		if (src == null || src.length() == 0) return src;
		return capitalize(src.toLowerCase());
	}

	/**
	 * Searches a string from left to right and returns the leftmost characters
	 * of the string. left ("111 222 333", " ") --> "111"
	 */
	public static String left(String src, String substr)
	{
		int idx = src.indexOf(substr);
		if (idx == -1) return EMPTY_STRING;
		else return src.substring(0, idx);
	} // left ()

	/**
	 * Searches a string from right to left and returns a substring. leftback (
	 * "111 222 333", " ") --> "111 222" notice: leftback ("a,0,0,0", ",0,0")
	 * --> "a", NOT "a,0"
	 */
	public static String leftback(String src, String substr)
	{
		int idx = src.indexOf(substr);
		if (idx == -1) { return EMPTY_STRING; }
		int len = substr.length();
		int tmp;
		while (true)
		{
			tmp = src.indexOf(substr, idx + len);
			if (tmp == -1) break;
			else idx = tmp;
		} // while
		return src.substring(0, idx);
	} // leftback ()

	/**
	 * Returns the rightmost characters in the string. right ("111 222 333", " "
	 * ) --> "222 333"
	 */
	public static String right(String src, String substr)
	{
		int idx = src.indexOf(substr);
		if (idx == -1) return EMPTY_STRING;
		else return src.substring(idx + substr.length());
	} // right ()

	/**
	 * Returns the rightmost characters in a string. rightback ("111 222 333", "
	 * ") --> "333"
	 */
	public static String rightback(String src, String substr)
	{
		int idx = src.indexOf(substr);
		if (idx == -1) { return EMPTY_STRING; }
		int len = substr.length();
		int tmp;
		while (true)
		{
			tmp = src.indexOf(substr, idx + len);
			if (tmp == -1) break;
			else idx = tmp;
		} // while
		return src.substring(idx + len);
	} // rightback ()

	/**
	 * return the substring between other 2 substrings
	 */
	public static String between(String src, String str1, String str2)
	{
		String rt = src;
		rt = right(rt, str1);
		rt = left(rt, str2);
		return rt;
	} // between ()

	/**
	 * Replaces first specific substring in a string with new substring. Case
	 * sensitive.
	 */
	public static String replaceFirst(String src, String from, String to)
	{
		StringBuilder sb = new StringBuilder(src);
		int idx = src.indexOf(from);
		int len = from.length();
		if (idx != -1) sb = sb.replace(idx, idx + len, to);
		return new String(sb);
	} // replaceFirst ()

	/**
	 * Replaces all specific substring in a string with new substring. Case
	 * sensitive.
	 */
	public static String replaceAll(String src, String from, String to)
	{
		StringBuilder sb = new StringBuilder(src);
		int i1, i2, tail;
		int len = from.length();
		int fromIndex = 0;
		while ((i1 = src.indexOf(from, fromIndex)) != -1)
		{
			i2 = i1 + len;
			tail = src.length() - i2;
			sb = sb.replace(i1, i2, to);
			src = new String(sb);
			fromIndex = src.length() - tail; // correct than following.
			// fromIndex = i1 + Math.max (from.length (), to.length ());
		}
		return new String(sb);
	} // replaceAll ()

	/**
	 * replaceFirst () wrapper
	 */
	public static String replace(String src, String from, String to)
	{
		return replaceFirst(src, from, to);
	} // replace ()

	/**
	 * replace old strings list to new strings list
	 */
	public static String replace(String src, String[] from, String[] to)
	{
		String rt = src;
		int len = Math.min(from.length, to.length);
		for (int i = 0; i < len; i++)
			rt = replace(rt, from[i], to[i]);
		return rt;
	} // replace ()

	/**
	 * replace all old strings in list to new strings.
	 */
	public static String replaceAll(String src, String[] from, String[] to)
	{
		String rt = src;
		int len = Math.min(from.length, to.length);
		for (int i = 0; i < len; i++)
			rt = replaceAll(rt, from[i], to[i]);
		return rt;
	} // replace ()

	/**
	 * replace a string between two sub string to a new string replaceBetween (
	 * "aaa bbb ccc", "aaa", "ccc", " zzz ") --> aaa zzz ccc
	 */
	public static String replaceBetween(String src, String before, String after, String to)
	{
		String from = between(src, before, after);
		return replaceFirst(src, from, to);
	} // replaceBetween ()

	/**
	 * trim all " ", "\t"," " in string; 注：第三个是全角空格
	 */
	public static String trim(String src)
	{
		if (nullity(src)) return EMPTY_STRING;
		return trim(src, TRIM_CHAR);
	}

	public static String null2emptystr(Object obj)
	{
		return obj == null ? EMPTY_STRING : obj.toString();
	}

	public static String null2emptystr(Object obj, String def)
	{
		return obj == null ? def : obj.toString();
	}

	public static String trim(String src, String chs)
	{
		return trim(src, chs.toCharArray());
	}

	public static String trim(String src, char[] chArray)
	{
		int st = 0;
		int len = src.length();

		while ((st < len) && (containChar(chArray, src.charAt(st))))
			st++;
		while ((st < len) && (containChar(chArray, src.charAt(len - 1))))
			len--;

		return ((st > 0) || (len < src.length())) ? src.substring(st, len) : src;
	}

	static boolean containChar(char[] chArray, char ch)
	{
		for (int i = 0; i < chArray.length; i++)
			if (chArray[i] == ch) return true;
		return false;
	}

	/**
	 * Returns an Array of Strings that are the substrings of the specified
	 * String.
	 */
	public static String[] split(String src, String sep)
	{
		if (src == null || src.length() == 0 || sep == null || sep.length() == 0)
			return new String[0];
		return (String[]) split2list(src, sep).toArray(new String[0]);
	} // split ()

	public static List<String> split2list(String src, String sep)
	{
		List<String> v = new ArrayList<>();
		if (src == null || src.length() == 0 || sep == null || sep.length() == 0) return v;
		int idx;
		int len = sep.length();
		while ((idx = src.indexOf(sep)) != -1)
		{
			v.add(src.substring(0, idx));
			idx += len;
			src = src.substring(idx);
		}
		v.add(src);
		return v;
	}

	/**
	 * <pre>
	 * &#64;author jamesqiu 2006-11-18 根据肖立彬的ETL需求做出
	 * Returns an Array of Strings that are the substrings of the specified String.
	 * 1111,abc,&quot;hello, abc&quot;,11.11,20050101 -&gt;
	 * [1111, abc, &quot;hello, abc&quot;, 11.11, 20050101]
	 * </pre>
	 * 
	 * @param quo
	 *            \", \' 等字符串引号
	 */
	public static String[] split(String src, String sep, String quo)
	{
		// Vector v = new Vector ();
		if (src == null || src.length() == 0 || sep == null || sep.length() == 0 || quo == null
				|| (!quo.equals("\"") && !quo.equals("\'")))
			return new String[0];
		List v = new ArrayList();
		int i0 = 0; // 单个列的开始
		int i1 = 1; // 单个列的结束
		int len = sep.length();
		boolean has_quo = false;
		while (true)
		{
			if (has_quo = src.startsWith(quo, i0)) i1 = src.indexOf(quo + sep, i0 + 1); // 1:
			// \"
			// 或者
			// \'
			// 的长度
			else i1 = src.indexOf(sep, i0);
			if (i1 != -1)
			{
				if (has_quo)
				{
					v.add(src.substring(i0, i1 + 1));
					i0 = i1 + (1 + len); // 1: \" 或者 \' 的长度
				}
				else
				{
					v.add(src.substring(i0, i1));
					i0 = i1 + len;
				}
			}
			else
			{
				v.add(src.substring(i0));
				break;
			}
		}
		return (String[]) v.toArray(new String[0]);
	} // split ()

	/**
	 * @author jamesqiu 2006-12-8 1,2;3,2;6,7 --> {{1,2}, {3,2}, {6,7}}
	 * @see TextAdaptor#getRawDataFromText(String, boolean)
	 */
	public static String[][] split2table(String express, String lineSep, String colSep)
	{
		if (StringX.nullity(express) || StringX.nullity(lineSep) || StringX.nullity(colSep))
			return new String[0][0];
		String[] rows = StringX.split(express, lineSep);
		String[][] table = new String[rows.length][];
		for (int i = 0; i < rows.length; i++)
			table[i] = StringX.split(rows[i], colSep);
		return table;
	}

	/**
	 * split string contains integer sequence to int[].
	 */
	public static int[] split2ints(String src, String sep)
	{
		String[] strs = split(src, sep);
		int[] ints0 = new int[strs.length];
		int nn = 0;
		for (int i = 0, n = strs.length; i < n; i++)
		{
			if (!strs[i].equals(EMPTY_STRING))
			{
				try
				{
					ints0[nn] = Integer.parseInt(strs[i]);
				}
				catch (Exception e)
				{
					ints0[nn] = 0;
				}
				nn++;
			}
		} // for
		int[] ints1 = new int[nn];
		System.arraycopy(ints0, 0, ints1, 0, nn);
		return ints1;
	} // split2ints ()

	public static double[] split2doubles(String src, String sep)
	{
		String[] strs = split(src, sep);
		double[] ints0 = new double[strs.length];
		int nn = 0;
		for (int i = 0, n = strs.length; i < n; i++)
		{
			if (!strs[i].equals(EMPTY_STRING))
			{
				try
				{
					ints0[nn] = Double.parseDouble(strs[i]);
				}
				catch (Exception e)
				{
					ints0[nn] = 0;
				}
				nn++;
			}
		} // for
		double[] ints1 = new double[nn];
		System.arraycopy(ints0, 0, ints1, 0, nn);
		return ints1;
	}

	/**
	 * split string contains boolean sequence to boolean[]. add by puning
	 * 2003-07-07
	 */
	public static boolean[] split2booleans(String src, String sep)
	{
		String[] strs = split(src, sep);
		boolean[] booleans0 = new boolean[strs.length];
		int nn = 0;
		for (int i = 0, n = strs.length; i < n; i++)
		{
			if (!strs[i].equals(EMPTY_STRING))
			{
				try
				{
					booleans0[nn] = new Boolean(strs[i]).booleanValue();
				}
				catch (Exception e)
				{
					booleans0[nn] = false;
				}
				nn++;
			}
		} // for
		boolean[] booleans1 = new boolean[nn];
		System.arraycopy(booleans0, 0, booleans1, 0, nn);
		return booleans1;
	} // split2ints ()

	/**
	 * join a string array to a single string with given join string.
	 */
	public static String join(String[] array, String sep)
	{
		if (array == null || array.length == 0) return EMPTY_STRING;
		int len = array.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (len - 1); i++)
			sb.append(array[i]).append(sep);
		sb.append(array[len - 1]);
		return sb.toString();
	} // join ()

	public static String join(List list, String sep)
	{
		if (list == null || list.size() == 0) return EMPTY_STRING;
		int len = list.size();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (len - 1); i++)
			sb.append(list.get(i)).append(sep);
		sb.append(list.get(len - 1));
		return sb.toString();
	}

	public static String join(Object[] array, String sep)
	{
		if (array == null || array.length == 0) return EMPTY_STRING;
		int len = array.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (len - 1); i++)
			sb.append(array[i]).append(sep);
		sb.append(array[len - 1]);
		return sb.toString();
	}

	/**
	 * join a string array to a single string with given join string.
	 * 
	 * @param array
	 *            strings need to join
	 * @param sep
	 *            seperate string
	 * @param pre
	 *            prefix before every string like "("
	 * @param post
	 *            postfix after every string like ")"
	 */
	public static String join(String[] array, String sep, String pre, String post)
	{
		if (array == null || array.length == 0) return EMPTY_STRING;
		int len = array.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (len - 1); i++)
			sb.append(pre).append(array[i]).append(post).append(sep);
		sb.append(pre).append(array[len - 1]).append(post);
		return sb.toString();
	} // join ()

	/**
	 * join a int array to a single string with given join string.
	 */
	public static String join(int[] array, String sep)
	{
		if (array == null || array.length == 0) return EMPTY_STRING;
		int len = array.length;
		StringBuilder sb = new StringBuilder();
		String item;
		for (int i = 0; i < (len - 1); i++)
		{
			item = (new Integer(array[i])).toString();
			sb.append(item).append(sep);
		}
		sb.append(array[len - 1]);
		return sb.toString();
	} // join ()

	/**
	 * join a boolean array to a single string with given join string. add by
	 * puning 2003-07-07
	 */
	public static String join(boolean[] array, String sep)
	{
		if (array == null || array.length == 0) return EMPTY_STRING;
		int len = array.length;
		StringBuilder sb = new StringBuilder();
		String item;
		for (int i = 0; i < (len - 1); i++)
		{
			item = (new Boolean(array[i])).toString();
			sb.append(item).append(sep);
		}
		sb.append(array[len - 1]);
		return sb.toString();
	} // join ()

	/**
	 * @author jamesqiu 2006-12-8 join a string[][] with line seperator and
	 *         column seperator
	 */
	public static String join(String[][] table, String lineSep, String colSep)
	{
		if (table == null || table.length == 0) return EMPTY_STRING;
		StringBuilder rt = new StringBuilder();
		for (int i = 0; i < table.length; i++)
		{
			if (i > 0) rt.append(lineSep);
			rt.append(join(table[i], colSep));
		}
		return rt.toString();
	}

	/**
	 * Make elements in a String Array unique.
	 */
	public static String[] unique(String[] strArray)
	{
		List unique = new ArrayList();
		for (int i = 0; i < strArray.length; i++)
		{
			if (unique.contains(strArray[i])) continue;
			unique.add(strArray[i]);
		}
		return (String[]) unique.toArray(new String[0]);
	} // unique ()

	/**
	 * Make elements in a String Array unique (like set). alias of unique
	 * (string[])
	 */
	public static String[] set(String[] strArray)
	{
		return unique(strArray);
	}

	/**
	 * @return if all string in string[] are different (like set), return true;
	 *         else, return false
	 */
	public static boolean isSet(String[] strArray)
	{
		if (strArray == null) return false;
		int n = strArray.length;
		String s1 = EMPTY_STRING;
		String s2 = EMPTY_STRING;
		for (int i = 0; i < n; i++)
		{
			s1 = strArray[i];
			for (int j = 0; j < n; j++)
			{
				if (i != j) s2 = strArray[j];
				if (s1.equals(s2)) return false;
			}
		}
		return true;
	}

	/**
	 * sort a String Array in natural order.
	 */
	public static void sort(String[] strArray)
	{
		Arrays.sort(strArray);
	} // unique ()

	/**
	 * group string array.
	 * 
	 * @param src_list
	 *            String array grouped with asc/desc order.
	 * @return String[2][n], String[0][n] is String, String[1][n] is length.
	 *         String[0][n]: [a][b][c][d] String[1][n]: [3][2][6][0]
	 */
	public static String[][] group(String[] src_list)
	{
		if (src_list == null
				|| src_list.length == 0) { return new String[][] { new String[0], new String[0] }; }
		List value = new ArrayList();
		List num = new ArrayList();
		String cur_str = src_list[0];
		value.add(cur_str);
		int n = 1;

		String tmp = null;
		for (int i = 1; i < src_list.length; i++)
		{
			tmp = src_list[i];
			if (tmp.equals(cur_str))
			{ // get same
				n++;
			}
			else
			{ // get different
				num.add(EMPTY_STRING + n);
				value.add(tmp);
				n = 1;
				cur_str = tmp;
			}
		} // for
		num.add(EMPTY_STRING + n);
		String[] rt_v = (String[]) value.toArray(new String[0]);
		String[] rt_n = (String[]) num.toArray(new String[0]);
		return new String[][] { rt_v, rt_n };
	} // group

	/**
	 * alias of in (string, string[])
	 * 
	 * @return If str is an element of strArray, return true, else return false.
	 */
	public static boolean include(String str, String[] strArray)
	{
		return in(str, strArray);
	} // include

	/**
	 * @return If str is an element of strArray, return true, else return false.
	 */
	public static boolean in(String str, String[] strArray)
	{
		if (str == null || strArray == null) return false;
		for (int i = 0, n = strArray.length; i < n; i++)
			if (str.equals(strArray[i])) return true;
		return false;
	}

	/**
	 * @return if str_array1 is subset of str_array2, return true, else return
	 *         false.
	 */
	public static boolean isSubset(String[] ss1, String[] ss2)
	{
		if (ss1 == null || ss2 == null) return false;
		for (int i = 0; i < ss1.length; i++)
			if (!in(ss1[i], ss2)) return false;
		return true;
	}

	/**
	 * remore these elements of source array which in remove array.
	 * 
	 * @return source[] - remove[]
	 * @author jamesqiu 2006-12-20
	 */
	public static String[] remove(String[] source, String[] remove)
	{
		if (source == null || remove == null) return new String[0];
		List rt = new ArrayList();
		for (int i = 0; i < source.length; i++)
			if (!in(source[i], remove)) rt.add(source[i]);
		return (String[]) rt.toArray(new String[rt.size()]);
	}

	/**
	 * @return If str is an element of strArray, return Index, else return -1.
	 */
	public static int indexOf(String str, String[] strArray)
	{
		if (str == null || strArray == null) return -1;
		for (int i = 0; i < strArray.length; i++)
			if (str.equals(strArray[i])) return i;
		return -1;
	} // indexOf

	/**
	 * plus version of indexOf (String, String[]),
	 * 
	 * @param ignoreCase
	 *            Ignore case if true;
	 */
	public static int indexOf(String str, String[] strArray, boolean ignoreCase)
	{
		if (str == null || strArray == null) return -1;
		boolean equals;
		for (int i = 0; i < strArray.length; i++)
		{
			equals = ignoreCase ? str.equalsIgnoreCase(strArray[i]) : str.equals(strArray[i]);
			if (equals) return i;
		}
		return -1;
	}

	/**
	 * insert new string before given substring
	 * 
	 * @param str
	 *            original string to insert
	 * @param substr
	 *            given substring of str before which to insert
	 * @param newstr
	 *            new string to insert befroe substring
	 */
	public static String insert(String str, String substr, String newstr)
	{
		if (str == null || substr == null || newstr == null) return EMPTY_STRING;
		String rt = left(str, substr) + newstr + substr + right(str, substr);
		return rt;
	}

	/**
	 * insert new string before given substring, find substring from back.
	 * 
	 * @param str
	 *            original string to insert
	 * @param substr
	 *            given substring of str before which to insert
	 * @param newstr
	 *            new string to insert befroe substring
	 */
	public static String insertback(String str, String substr, String newstr)
	{
		if (str == null || substr == null || newstr == null) return EMPTY_STRING;
		String rt = leftback(str, substr) + newstr + substr + rightback(str, substr);
		return rt;
	}

	public static String base64(String str) throws UnsupportedEncodingException
	{
		return new String(encodeBase64(str.getBytes(Common.CHARSET_UTF8)));
	}

	public static String base64(String str, String charset) throws UnsupportedEncodingException
	{
		return new String(encodeBase64(str.getBytes(charset)));
	}

	public static String encodeBase64Url(byte[] simple)
	{
		// Regular base64 encoder
		String s = new String(Base64.encodeBase64(simple));
		s = s.split("=")[0]; // Remove any trailing '='s
		s = s.replace('+', '-'); // 62nd char of encoding
		s = s.replace('/', '_'); // 63rd char of encoding
		return s;
	}

	public static byte[] decodeBase64Url(String cipher)
	{
		String s = cipher;
		s = s.replace('-', '+'); // 62nd char of encoding
		s = s.replace('_', '/'); // 63rd char of encoding
		switch (s.length() % 4)
		{ // Pad with trailing '='s
			case 0:
				break; // No pad chars in this case
			case 2:
				s += "==";
				break; // Two pad chars
			case 3:
				s += "=";
				break; // One pad char
			default:
				System.err.println("Illegal base64url String!");
		}
		return Base64.decodeBase64(s); // Standard base64 decoder
	}

	public static String base64(byte[] buf)
	{
		if (buf == null) return StringX.EMPTY_STRING;
		return new String(Base64.encodeBase64(buf));
	}

	public static byte[] encodeBase64(byte[] buf)
	{
		if (buf == null) return null;
		return Base64.encodeBase64(buf);
	}

	public static byte[] decodeBase64(String str)
	{
		if (str == null) return null;
		return Base64.decodeBase64(str.getBytes());
	}

	public static byte[] decodeBase64(byte[] buf)
	{
		if (buf == null) return null;
		return Base64.decodeBase64(buf);
	} // base64decode ()

	/**
	 * @param str
	 *            string to be encode
	 * @param md5
	 *            string md5("") = d41d8cd98f00b204e9800998ecf8427e md5("a") =
	 *            0cc175b9c0f1b6a831c399e269772661 md5("abc") =
	 *            900150983cd24fb0d6963f7d28e17f72 md5("message digest") =
	 *            f96b697d7cb7938d525a2f31aaf161d0
	 *            md5("abcdefghijklmnopqrstuvwxyz")=
	 *            c3fcd3d76192e4007dfb496cca67e13b
	 */
	public static String md5(byte[] buf)
	{
		return digest(buf, DIGEST_MD5, null);
	}

	public static String digest(String str, String charset, String algorithm)
	{
		if (nullity(str)) return EMPTY_STRING;
		try
		{
			return digest(str.getBytes(charset), algorithm, null);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException("fail digest:" + algorithm + ", e:" + e);
		}
	}

	public static String digest(byte[] buf, String algorithm)
	{
		return digest(buf, algorithm, null);
	}

	public static String digest(byte[] buf, String alg, byte[] key)
	{
		return digest(buf, 0, buf.length, alg, key);
	}

	public static String digest(byte[] buf, int offset, int len, String alg, byte[] key)
	{
		if (buf == null)
		{
			System.err.println("StringX.digest(byte[]) : null");
			return EMPTY_STRING;
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance(alg);
			digest.update(buf, offset, len);
			return key == null ? bigint2str(digest.digest()) : bigint2str(digest.digest(key));
		}
		catch (Exception e)
		{
			throw new RuntimeException("fail digest:" + alg + ", e:" + e);
		}
	}

	// 大文件加签名, 针对附件信息
	public static String digest(InputStream is, int sliceSize, String alg, byte[] key)
			throws Exception
	{
		try
		{
			String digest = null;
			byte[] buf = new byte[sliceSize];
			int len = is.read(buf);
			while (len > 0)
			{
				String bufDigest = digest(buf, 0, len, alg, key);
				if (digest == null) digest = bufDigest;
				else digest = digest((digest + bufDigest).getBytes(), alg, key); // 将上一片签名整合到当前片中
				if (len < buf.length) break;
				len = is.read(buf);
			}
			return digest;
		}
		finally
		{
			if (is != null) is.close();
		}
	}

	/**
	 * 将二进制的的大整数变为16进制的字符串， md5, 签名等使用
	 * 
	 * @param bigint
	 * @return
	 */
	public static String bigint2str(byte[] bigint)
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < bigint.length; i++)
		{
			String str = Integer.toHexString((bigint[i] & 0xff)).toUpperCase();
			buf.append(str.length() < 2 ? '0' + str : str);
		}
		return buf.toString();
	}

	/**
	 * Check jsp parameter, if parameter is null or is "", set to default value.
	 */
	public static String check(String param, String src_default)
	{
		if (param == null || param.trim().length() == 0) return src_default;
		else return param;
	} // check ()

	/**
	 * Check jsp parameter, if parameter is null or "" or " ", return true. ||
	 * param.trim().length() == 0
	 */
	public static boolean nullity(String param)
	{
		return (param == null || param.length() == 0);
	} // check ()

	/**
	 * Check is given string is "true"
	 */
	public static boolean isTrue(String param)
	{
		if (param == null) return false;
		return param.equalsIgnoreCase("true");
	}

	/**
	 * get jsp webapp real path.
	 */
	// public static String getRealPath(javax.servlet.ServletContext
	// application)
	// {
	// return getRealPath(application, "/");
	// }

	/**
	 * get jsp webapp real path.
	 */
	// public static String getRealPath(javax.servlet.ServletContext
	// application, String uri)
	// {
	// if (application == null)
	// {
	// System.err.println("StringX.getRealPath (.) : null");
	// return EMPTY_STRING;
	// }
	// return application.getRealPath(uri) + "/";
	// }

	/**
	 * get related template file name of jsp file. if current jsp file is
	 * "abc.jsp", return "abc_template.html"
	 */
	// public static String getTemplate(javax.servlet.ServletContext
	// application,
	// javax.servlet.http.HttpServletRequest request)
	// {
	// if (application == null || request == null)
	// {
	// System.err.println("StringX.getTemplate (..) : null");
	// return EMPTY_STRING;
	// }
	// String jspPath = application.getRealPath(request.getServletPath());
	// String tfile = StringX.leftback(jspPath, ".jsp") + "_template.html";
	// return tfile;
	// }
	//
	// /**
	// * get related binding file name of jsp file.
	// */
	// public static String getBinding(javax.servlet.ServletContext application,
	// javax.servlet.http.HttpServletRequest request)
	// {
	// if (application == null || request == null)
	// {
	// System.err.println("StringX.getBinding (..) : null");
	// return EMPTY_STRING;
	// }
	// String jspPath = application.getRealPath(request.getServletPath());
	// String bfile = StringX.leftback(jspPath, ".jsp") + "_binding.xml";
	// return bfile;
	// }

	/**
	 * get all variables in a expression, eg. _id1 + id2 + id_3 --> (_id1, id2,
	 * id_3) 0.3 * A + (_b-C_0_1)/2 --> (A, _b, C_0_1) -A + 2*B + 3*A - B -->
	 * (A, B) $A + B --> ($A, B)
	 * 
	 * @version 2007-8-11 add var start with $
	 */
	// public static String[] getExpVars(String expression)
	// {
	// if (expression == null) return new String[0];
	// jregex.Pattern p = new jregex.Pattern("[$_A-z]\\w*");
	// jregex.Matcher m = p.matcher(expression);
	// List list1 = new ArrayList();
	// for (jregex.MatchIterator mi = m.findAll(); mi.hasMore();)
	// {
	// jregex.MatchResult mr = mi.nextMatch();
	// String v = mr.toString();
	// list1.add(v);
	// }
	// // unique varial happen more than once.
	// String[] rt = (String[]) list1.toArray(new String[0]);
	// rt = StringX.unique(rt);
	// return rt;
	// }

	/**
	 * translate map's key as string array, map's value as string array.
	 * 
	 * @return String[2][] rt, rt[0] = key string array, rt[1] = value string
	 *         array.
	 */
	public static String[][] map2array(Map map)
	{
		if (map == null) return new String[2][0];
		int n = map.size();
		String[][] rt = new String[2][n];
		rt[0] = (String[]) map.keySet().toArray(new String[n]);
		rt[1] = (String[]) map.values().toArray(new String[n]);
		return rt;
	}

	/**
	 * translate List's elements as string array. condition: all elements of
	 * list is string (or null).
	 * 
	 * @return String[] rt.
	 */
	public static String[] list2array(List list)
	{
		return collection2array(list);
	}

	/**
	 * translate Collection's elements as string array. condition: all elements
	 * of collection is string (or null).
	 * 
	 * @return String[] rt.
	 */
	public static String[] collection2array(Collection c)
	{
		if (c == null) return new String[0];
		return (String[]) c.toArray(new String[c.size()]);
	}

	/**
	 * @param s
	 *            格式化字符串，例子
	 * 
	 *            <pre>
	 * ：
	 * {0}''s and {1}''s
	 * {0,number}
	 * {0,number,#.#}
	 * {0,number,currency}
	 * {0,number,percent}
	 * 
	 * {0,time}   {0,time,HH-mm-ss}
	 * {0,time,short}   {0,time,medium}   {0,time,long}   {0,time,full}
	 *  
	 * {0,date}   {0,date,yyyy-MM-dd}
	 * {0,date,short}    {0,date,medium}   {0,date,long}   {0,date,full}
	 *            </pre>
	 * 
	 * @param param
	 *            可以是String, Number, Date (yyyy/MM/dd HH:mm:ss a)
	 */
	public static String format(String s, Object[] param)
	{
		String rt = MessageFormat.format(s, param);
		return rt;
	}

	/**
	 * 格式化输出，类似C的printf，用Object[]模拟可变参数.\n 例如："[{0}]: {1} {2,number,#.##}
	 * {3,date,yyyy/MM/dd} {3,time,aHH:mm:ss}\n"
	 * 
	 * @see format ()
	 */
	public static void printf(String s, Object[] param)
	{
		System.out.print(format(s, param));
	}

	// -------------------------- main ------------------------//
	// public static void main(String[] args)
	// {
	// /**
	// * MD5 ("") = d41d8cd98f00b204e9800998ecf8427e MD5 ("a") =
	// * 0cc175b9c0f1b6a831c399e269772661 MD5 ("abc") =
	// * 900150983cd24fb0d6963f7d28e17f72 MD5 ("message digest") =
	// * f96b697d7cb7938d525a2f31aaf161d0 MD5 ("abcdefghijklmnopqrstuvwxyz") =
	// * c3fcd3d76192e4007dfb496cca67e13b
	// */
	// String[] sa = new String[] { EMPTY_STRING, "a", "abc",
	// "message digest", "abcdefghijklmnopqrstuvwxyz", "1" };
	// String s = null;
	// for (int i = 0; i < sa.length; i++)
	// {
	// s = md5(sa[i]);
	// System.out.println(sa[i] + "==" + s);
	// }
	//
	// System.out.println("test ---------- list2array ----------");
	// List l = null;
	// String[] rt = list2array(l);
	// System.out.println(rt.length);
	//
	// l = new ArrayList();
	// rt = list2array(l);
	// System.out.println(rt.length);
	//
	// l.add("hello");
	// l.add("中文");
	// l.add(null);
	// rt = list2array(l);
	// System.out.println(rt[1]);
	// System.out.println(join(rt, ", ", "\"", "\""));
	//
	// System.out.println("test ----- format -----");
	// printf(
	// "[{0}]: {1} {2,number,#.##} {3,date,yyyy/MM/dd} {3,time,aHH:mm:ss}\n",
	// new Object[] { "James", new Integer(100),
	// new Double(2003.14159265), new Date() });
	// // [James]: 100 2003.14 2007/07/25 下午15:05:12
	// }

	// for xml
	static ThreadLocal<XMLReader> LOCAL_XML = new ThreadLocal<>();

	private static XMLReader getXMLReader() throws Exception
	{
		XMLReader parser = LOCAL_XML.get();
		if (parser != null) return parser;

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		parser = factory.newSAXParser().getXMLReader();
		LOCAL_XML.set(parser);
		return parser;
	}

	static public class MapSaxHandler extends DefaultHandler
	{
		static final class Status
		{
			public Stack<Object> stack = new Stack<>();
			public boolean first = true;
			public Object current;
			public Object parent; // 父节点
			public String name; // 当前节点在父节点中名字
			public int index; // 如果父节点是ArrayNode节点.则记录在ArrayNode中的位置.
		}

		static final ThreadLocal<Status> STATUS = new ThreadLocal<>();
		static final ThreadLocal<Map> ROOT = new ThreadLocal<>();
		static final Logger log = LoggerFactory.getLogger(MapSaxHandler.class);
		static final MapSaxHandler handler = new MapSaxHandler();

		public MapSaxHandler()
		{
		}

		public void setRoot(Map<String, Object> root)
		{
			ROOT.set(root);
		}

		public Map root()
		{
			return ROOT.get();
		}

		public void startDocument() throws SAXException
		{
			Status status = new Status();
			status.current = ROOT.get();
			status.stack.clear();
			status.stack.push(status.current);
			status.first = true;
			STATUS.set(status);
		}

		public void endDocument() throws SAXException
		{
			STATUS.set(null);
		}

		public void endElement(String uri, String localname, String qName) throws SAXException
		{
			Status status = (Status) STATUS.get();
			if (status.stack.empty()) return;
			status.stack.pop();
			if (status.stack.empty()) return;
			status.current = status.stack.peek();
		}

		public void characters(char[] v, int start, int length)
		{
			if (length == 0) return;
			Status status = STATUS.get();

			String s = StringX.utf82str(new String(v, start, length));
			// StringX.utf82str(StringX.trim(new String(v, start, length),
			// CHAR_ARRAY));

			// if (s.trim().length() == 0) return; // 无效空格不予以处理
			Object node = status.current;
			// System.out.print("\nchars:" + s + "," + node.getClass());
			if (node == null || !(node instanceof StringBuilder)) return;
			String o = node.toString();
			if (o != null) s = o.toString() + s; // 对于文本中存在回车, 解析器会把每行作为一个事件进行触发
			// System.out.println("s:"+s);
			((StringBuilder) status.current).append(s);
		}

		public void startElement(String uri, String localname, String qName, Attributes attr)
				throws SAXException
		{
			Status status = STATUS.get();
			if (status.first)
			{
				status.first = false;
				return;
			}
			Object node = new StringBuilder();
			int idx = qName.indexOf(':');
			String name = idx > 0 ? qName.substring(idx + 1) : qName;
			if (status.current instanceof Map)
			{ // 如果当前父亲节点是结构类型
				Map parent = (Map) status.current;
				add2map(parent, name, node);

				status.parent = status.current;
				status.name = name;
				status.current = node;
			}
			else if (status.current instanceof List)
			{ // 如果当前父亲节点是数组节点
				((List) status.current).add(node);
				status.parent = status.current;
				status.index = ((List) status.current).size() - 1;
				status.current = node;
			}
			else
			{ // 修改父节点为compositenode节点
				Object pnode = null;
				// 如果当前是符合节点下的子元素
				// modifed by chenjs 2011-12-02 容许产生不同实例的cnode
				pnode = new HashMap<String, Object>();
				// System.out.println("pnode: "+pnode.getClass());
				((Map) pnode).put(name, node); // 修改一下原子节点所在父节点信息
				changeParent(status, pnode);

				status.stack.pop();
				status.stack.push(pnode);

				status.name = name;
				status.current = node;
				status.parent = pnode;
			}
			status.stack.push(node);
		}

		// 改变父节点类型, 由原子类型改为compositenode or arraynode
		void changeParent(Status status, Object node)
		{
			if (status.parent instanceof Map)
			{
				Object first = ((Map) status.parent).get(status.name);
				if (first instanceof StringBuilder) ((Map) status.parent).put(status.name, node);
				else if (first instanceof List) ((List) first).set(((List) first).size() - 1, node);
				else System.err
						.println("cannot change parent node, cos parent in parent is valid node.."
								+ first.getClass());
			}
			else
			{
				List pnode = (List) status.parent;
				pnode.set(status.index, node);
			}
		}

		// 把一个节点放入到父节点中，如果某个节点已经放入到父节点中时，采用把原节点合并数组处理
		// 返回当前节点此时的真正parent
		Object add2map(Map parent, String name, Object current)
		{
			Object first = parent.get(name);
			if (first != null)
			{ // 表面xml报文中在同一层级中已经包含了一个同名标签， 此时应该理解为数组
				if (first instanceof List)
				{
					((List) first).add(current);
					return first;
				}
				// 修改父亲节点中的元素，使之变为一个数组类型，
				// Note: 此时还没考虑元素类型的第一个类型就是数组类型，此时逻辑会出问题，对于数组嵌套比较少
				List<Object> arr = new ArrayList<>();
				arr.add(first);
				arr.add(current);
				parent.put(name, arr);
				return arr;
			}
			parent.put(name, current);
			return parent;
		}

		public void error(SAXParseException e) throws SAXException
		{
			log.error("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
		}

		public void fatalError(SAXParseException e) throws SAXException
		{
			log.error("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
		}

		public void warning(SAXParseException e) throws SAXException
		{
			log.warn("Line:" + e.getLineNumber() + ",Column:" + e.getColumnNumber(), e);
		}
	}
} // StringX class
