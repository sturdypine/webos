package spc.webos.web.view;

import java.io.IOException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

import freemarker.template.Template;
import spc.webos.util.FTLUtil;

public class FreeMarkerViewResolver extends AbstractTemplateViewResolver
{
	public FreeMarkerViewResolver()
	{
		setViewClass(requiredViewClass());
	}

	/**
	 * Requires {@link FreeMarkerView}.
	 */
	protected Class requiredViewClass()
	{
		return FreeMarkerView.class;
	}
}

/**
 * 800, 增加系统配置读取freemarker
 * 
 * @author chenjs
 * 
 */
class FreeMarkerView extends org.springframework.web.servlet.view.freemarker.FreeMarkerView
{
	Logger log = LoggerFactory.getLogger(getClass());

	protected Template getTemplate(String name, Locale locale) throws IOException
	{
		IOException ioe;
		try
		{ // 页面请求主要以module文件为主, 优先判断
			return super.getTemplate(name, locale);
		}
		catch (IOException e)
		{
			ioe = e;
		}

		String sname = name.substring(0, name.length() - 4).replaceAll("/ftl/", "/");
		Template t = FTLUtil.getTemplate(sname);
		if (t != null)
		{
			log.info("ftl in util:{}", sname);
			return t;
		}
		throw ioe;
	}
}
