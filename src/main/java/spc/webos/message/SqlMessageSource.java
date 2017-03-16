package spc.webos.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import spc.webos.persistence.jdbc.rowtype.RowList;
import spc.webos.service.BaseService;
import spc.webos.util.StringX;

/**
 * 通过sql id获取数据库中的code,text对应关系, 需要对多字段进行转移，比如select code,name,email...from user
 * 我们需要根据code在传入不同变量的时候，返回不同的text值
 * 
 * @author spc
 * 
 */
public class SqlMessageSource extends BaseService implements MessageSource
{
	private String sqlId;
	private Map message;
	private List dict; // 用于做数据字典

	public final static Map<String, SqlMessageSource> SQL_MSG = new ConcurrentHashMap<>();

	public SqlMessageSource()
	{
		versionKey = "status.refresh.common.sqlms";
	}

	public SqlMessageSource(String sqlId)
	{
		this();
		this.sqlId = sqlId;
	}

	public final static SqlMessageSource getMessageSource(String sqlId)
	{
		return SQL_MSG.get(sqlId);
	}

	public final static SqlMessageSource createSqlMessage(String sqlId, Map params)
	{
		SqlMessageSource sqlmsg = new SqlMessageSource();
		sqlmsg.sqlId = sqlId;
		sqlmsg.message = sqlmsg.loadMessage(params);
		return sqlmsg;
	}

	public List getDict()
	{
		return dict;
	}

	public String toJsonDict()
	{
		return toJsonDict(null);
	}

	public String toJsonDict(String mf)
	{
		MessageFormat format = null;
		if (mf != null) format = new MessageFormat(mf);
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		for (int i = 0; i < dict.size(); i++)
		{
			RowList row = (RowList) dict.get(i);
			if (i != 0) buf.append(',');
			buf.append('[');
			for (int j = 0; j < row.size(); j++)
			{
				if (j != 0) buf.append(',');
				buf.append('\'');
				if (j == 1 && format != null)
					buf.append(StringX.str2utf8(format.format(row.toArray()))); // 把第一个变成指定的格式
				else buf.append(StringX.str2utf8((String) row.get(j)));
				buf.append('\'');
			}
			buf.append(']');
		}
		buf.append(']');
		return buf.toString();
	}

	public String toHTML(String value)
	{
		return toHTML(value, null);
	}

	/**
	 * 
	 * @param value
	 * @param format
	 *            html文本显示的格式
	 * @return
	 */
	public String toHTML(String value, String format)
	{
		MessageFormat mf = null;
		if (format != null) mf = new MessageFormat(format);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < dict.size(); i++)
		{
			RowList row = (RowList) dict.get(i);
			String key = row.get(0).toString();
			buf.append("<option value=\"");
			buf.append(key);
			if (value.equals(key)) buf.append("\" selected>");
			else buf.append("\">");
			if (mf != null) buf.append(mf.format(row.toArray()));
			else buf.append(row.get(1));
			buf.append("</option>\n");
		}
		return buf.toString();
	}

	public void refresh()
	{
		log.info("sqlId:{}", sqlId);
		this.message = loadMessage(null);
		SQL_MSG.put(sqlId, this);
	}

	Map loadMessage(Map params)
	{
		Map message = new HashMap();
		dict = (List) persistence.execute(sqlId, params);
		for (int i = 0; i < dict.size(); i++)
		{
			List row = (List) dict.get(i);
			String key = row.get(0).toString();
			message.put(key, row.toArray());
		}
		return message;
	}

	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
	{
		Object[] row = (Object[]) message.get(code);
		row = (Object[]) message.get(code);
		if (row == null) return defaultMessage;
		if (args == null || args.length == 0) return (String) row[1];
		return MessageFormat.format(args[0].toString(), row);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException
	{
		return resolvable.getDefaultMessage();
	}

	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException
	{
		return getMessage(code, args, null, locale);
	}

	public void setSqlId(String sqlId)
	{
		this.sqlId = sqlId;
	}

	public Map getMessage()
	{
		return message;
	}
}
