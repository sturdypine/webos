package spc.webos.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.web.util.WebUtil;

public class FilterChainProxy implements Filter
{
	static final Logger logger = LoggerFactory.getLogger(FilterChainProxy.class);
	static final FilterChainProxy filterChainProxy = new FilterChainProxy();
	AbstractURLFilter[] filters;

	public void setFilters(AbstractURLFilter[] filters)
	{
		this.filters = filters;
	}

	public FilterChainProxy getInstance()
	{
		return filterChainProxy;
	}

	private FilterChainProxy()
	{
	}

	public void destroy()
	{
		for (int i = 0; i < filters.length; i++)
		{
			if (logger.isDebugEnabled())
				logger.debug("Destroying Filter defined in ApplicationContext: '"
						+ filters[i].toString() + "'");

			filters[i].destroy();
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		try
		{
			WebUtil.WEB.set(true);
			FilterInvocation fi = new FilterInvocation(request, response, chain);
			VirtualFilterChain virtualFilterChain = new VirtualFilterChain(fi, filters);
			virtualFilterChain.doFilter(fi.getRequest(), fi.getResponse());
		}
		finally
		{
			WebUtil.WEB.set(null);
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException
	{
		for (int i = 0; i < filters.length; i++)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("Initializing Filter defined in ApplicationContext: '"
						+ filters[i].toString() + "'");
			}

			filters[i].init(filterConfig);
		}
	}

	/**
	 * A <code>FilterChain</code> that records whether or not
	 * {@link FilterChain#doFilter(javax.servlet.ServletRequest,javax.servlet.ServletResponse)}
	 * is called.
	 * <p>
	 * This <code>FilterChain</code> is used by <code>FilterChainProxy</code> to
	 * determine if the next <code>Filter</code> should be called or not.
	 * </p>
	 */
	private class VirtualFilterChain implements FilterChain
	{
		private FilterInvocation fi;
		private AbstractURLFilter[] additionalFilters;
		private int currentPosition = 0;

		public VirtualFilterChain(FilterInvocation filterInvocation,
				AbstractURLFilter[] additionalFilters)
		{
			this.fi = filterInvocation;
			this.additionalFilters = additionalFilters;
		}

		public void doFilter(ServletRequest request, ServletResponse response)
				throws IOException, ServletException
		{
			if (currentPosition == additionalFilters.length)
			{
				// if (logger.isDebugEnabled())
				// {
				// logger
				// .debug(fi.getRequestUrl()
				// + " reached end of additional filter chain; proceeding with
				// original chain");
				// }

				fi.getChain().doFilter(request, response);
			}
			else
			{
				currentPosition++;

				if (logger.isDebugEnabled())
				{
					logger.debug(fi.getRequestUrl() + " at position " + currentPosition + " of "
							+ additionalFilters.length
							+ " in additional filter chain; firing Filter: '"
							+ additionalFilters[currentPosition - 1] + "'");
				}

				additionalFilters[currentPosition - 1].doFilter(request, response, this);
			}
		}
	}
}
