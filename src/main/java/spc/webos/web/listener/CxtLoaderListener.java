package spc.webos.web.listener;

import java.util.Date;

import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

/**
 * 在jetty下, 在加载容器出错时没有具体错误信息展现, 所以继承Spring自带的ContextLoaderListener
 * 
 * @author chenjs
 */
public class CxtLoaderListener extends ContextLoaderListener
{
	public void contextInitialized(ServletContextEvent event)
	{
		try
		{
			String webPath = event.getServletContext().getRealPath(StringX.EMPTY_STRING)
					.replace('\\', '/');
			AppConfig.getInstance().setProperty(Common.WEBAPP_ROOT_PATH_KEY, webPath);

			String webAppName = event.getServletContext().getInitParameter(Common.WEBAPP_ROOT_KEY);
			if (!StringX.nullity(webAppName)) System.setProperty(webAppName, webPath);

			String servletCxtName = event.getServletContext().getServletContextName();
			String strmsg = "Load:" + servletCxtName + ", web:" + webAppName + ", webos:"
					+ Common.VER() + ", startTm: "
					+ FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
			System.out.println(strmsg);
			if (log == null) log = LoggerFactory.getLogger(getClass());
			log.info(strmsg);
			long currentTimeMillis = System.currentTimeMillis();
			super.contextInitialized(event);

			strmsg = "Load successfully, product mode:" + AppConfig.isProductMode() + ", app:"
					+ SpringUtil.APPCODE + "/" + SpringUtil.JVM + ", cost "
					+ ((System.currentTimeMillis() - currentTimeMillis) / 1000) + " seconds...\n\n";
			System.out.println(strmsg);
			log.info(strmsg);
		}
		catch (Throwable t)
		{
			System.out.print("Err in loading spring web context!!!");
			t.printStackTrace(System.out);
			throw new RuntimeException(t);
		}
	}

	public void contextDestroyed(ServletContextEvent event)
	{
		try
		{
			super.contextDestroyed(event);
		}
		catch (RuntimeException e)
		{
			System.err.print("Error in destroying spring web context...");
			e.printStackTrace();
			throw e;
		}
	}

	protected Logger log;
}
