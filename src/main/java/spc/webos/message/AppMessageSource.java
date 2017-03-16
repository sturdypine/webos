package spc.webos.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

public class AppMessageSource implements HierarchicalMessageSource, ApplicationContextAware
{
	protected MessageSource parent;
	protected Map dict;
	static AppMessageSource APP_MSG = new AppMessageSource();
	protected Logger log = LoggerFactory.getLogger(getClass());
	public final static char SEPARATOR = '/';
	public final static char MUL_SEPARATOR = ','; // 针对code里面多个，用逗号分隔

	@PostConstruct
	public void init()
	{
		if (dict != null && dict.size() > 0) return;
		// 从spring环境中查找MS
		dict = new HashMap();
		String[] beans = cxt.getBeanNamesForType(MessageSource.class);
		for (String bean : beans)
		{
			if (!bean.equalsIgnoreCase("messageSource"))
				dict.put(bean, cxt.getBean(bean, MessageSource.class));
		}
		log.info("MS in spring:{}", dict.keySet());
	}

	public String getMessage(String code, String defmsg)
	{
		return getMessage(code, null, defmsg, null);
	}

	public String getMessage(String code, Object[] args, String defmsg)
	{
		return getMessage(code, args, defmsg, null);
	}

	public String getMessage(String code, Object[] args, String defmsg, Locale locale)
	{
		log.debug("ms code:{}", code);
		int index = code.indexOf(SEPARATOR);
		String prefix = code.substring(0, index);
		Object ms = dict == null ? null : dict.get(prefix);
		if (ms == null) return parent == null ? new MessageFormat(defmsg).format(args)
				: parent.getMessage(code, args, defmsg, locale);
		// System.out.println("code is :" + index + "," + code);
		String msg = getMessage(ms, code.substring(index + 1), args, defmsg, locale);
		log.debug("code:{}, msg:{}", code, msg);
		// System.out.println("code:" + code + ",msg:" + msg);
		return msg;
	}

	protected String getMessage(Object ms, String code, Object[] args, String defaultMessage,
			Locale locale)
	{
		// System.out.println("MS code is:" + code + ", " + ms.getClass());
		int index = code.indexOf(SEPARATOR);
		String prefix = index > 0 ? code.substring(0, index) : code;
		String value = null;
		if (ms instanceof MessageSource)
		{ // Object is MessageSource
			value = (String) ((MessageSource) ms).getMessage(code, args, defaultMessage, locale);
			// System.out.println("MS.code=" + code + ", value=" + value);
		}
		else if (ms instanceof Map)
		{ // object is Map
			Object obj = ((Map) ms).get(prefix);
			if (obj == null) return defaultMessage;
			if (obj instanceof String) return new MessageFormat((String) obj).format(args);
			return getMessage(obj, code.substring(index + 1), args, defaultMessage, locale);
		}

		if (value != null) return value;
		if (defaultMessage == null) return null; // 默认消息也可以是一个消息模板
		return new MessageFormat(defaultMessage).format(args);
	}

	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException
	{
		return getMessage(code, args, null, locale);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException
	{
		return resolvable.getDefaultMessage();
	}

	private AppMessageSource()
	{
	}

	public static AppMessageSource getInstance()
	{
		return APP_MSG;
	}

	public void setParentMessageSource(MessageSource parent)
	{
		this.parent = parent;
	}

	public MessageSource getParentMessageSource()
	{
		return parent;
	}

	public Map getDict()
	{
		return dict;
	}

	public void setDict(Map dict)
	{
		this.dict = dict;
	}

	@Override
	public void setApplicationContext(ApplicationContext cxt) throws BeansException
	{
		this.cxt = cxt;
	}

	ApplicationContext cxt;
}
