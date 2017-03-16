package spc.webos.web.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class FilterToBeanProxy implements Filter
{
	Filter delegate;
	FilterConfig filterConfig;
	boolean initialized = false;
	boolean servletContainerManaged = false;

	public void destroy()
	{
		if ((delegate != null) && servletContainerManaged) delegate.destroy();
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		if (!initialized) doInit();
		delegate.doFilter(request, response, chain);
	}

	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
		String strategy = filterConfig.getInitParameter("init");
		if ((strategy != null) && strategy.toLowerCase().equals("lazy")) return;
		doInit();
//		String servletCxtName = filterConfig.getServletContext().getServletContextName();
//		System.out
//				.println("FilterToBeanProxy: Success to load FilterToBeanProxy(servletCxtName = "
//						+ servletCxtName + ")...");
	}

	protected ApplicationContext getContext(FilterConfig filterConfig)
	{
		return WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig
				.getServletContext());
	}

	private synchronized void doInit() throws ServletException
	{
		// already initialized, so don't re-initialize
		if (initialized) return;
		try
		{
			String targetBean = filterConfig.getInitParameter("targetBean");
			if (targetBean.length() == 0) targetBean = null;
			String lifecycle = filterConfig.getInitParameter("lifecycle");
			if ("servlet-container-managed".equals(lifecycle)) servletContainerManaged = true;
			ApplicationContext ctx = this.getContext(filterConfig);
			String beanName = null;
			if ((targetBean != null) && ctx.containsBean(targetBean))
			{
				beanName = targetBean;
			}
			else if (targetBean != null)
			{
				throw new ServletException("targetBean '" + targetBean + "' not found in context");
			}
			else
			{
				String targetClassString = filterConfig.getInitParameter("targetClass");

				if (targetClassString == null || targetClassString.length() == 0)
					throw new ServletException("targetClass or targetBean must be specified");

				Class targetClass;

				try
				{
					targetClass = Thread.currentThread().getContextClassLoader().loadClass(
							targetClassString);
				}
				catch (ClassNotFoundException ex)
				{
					throw new ServletException("Class of type " + targetClassString
							+ " not found in classloader");
				}
				Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, targetClass, true,
						true);
				if (beans.size() == 0)
					throw new ServletException(
							"Bean context must contain at least one bean of type "
									+ targetClassString);
				beanName = (String) beans.keySet().iterator().next();
			}
			Object object = ctx.getBean(beanName);
			if (!(object instanceof Filter))
				throw new ServletException("Bean '" + beanName
						+ "' does not implement javax.servlet.Filter");
			delegate = (Filter) object;
			if (servletContainerManaged) delegate.init(filterConfig);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		initialized = true;
	}
}
