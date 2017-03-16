package spc.webos.web.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Holds objects associated with a HTTP filter.
 * <P>
 * Guarantees the request and response are instances of
 * <code>HttpServletRequest</code> and <code>HttpServletResponse</code>,
 * and that there are no <code>null</code> objects.
 * </p>
 * <P>
 * Required so that security system classes can obtain access to the filter
 * environment, as well as the request and response.
 * </p>
 * 
 * @author Ben Alex
 * @author colin sampaleanu
 * @version $Id: FilterInvocation.java,v 1.6 2005/11/17 00:55:50 benalex Exp $
 */
public class FilterInvocation
{
	FilterChain chain;
	ServletRequest request;
	ServletResponse response;

	public FilterInvocation(ServletRequest request, ServletResponse response, FilterChain chain)
	{
		if ((request == null) || (response == null) || (chain == null))
		{
			throw new IllegalArgumentException("Cannot pass null values to constructor");
		}

		if (!(request instanceof HttpServletRequest))
		{
			throw new IllegalArgumentException("Can only process HttpServletRequest");
		}

		if (!(response instanceof HttpServletResponse))
		{
			throw new IllegalArgumentException("Can only process HttpServletResponse");
		}

		this.request = request;
		this.response = response;
		this.chain = chain;
	}

	protected FilterInvocation()
	{
		throw new IllegalArgumentException("Cannot use default constructor");
	}

	public FilterChain getChain()
	{
		return chain;
	}

	/**
	 * Indicates the URL that the user agent used for this request.
	 * <P>
	 * The returned URL does <b>not</b> reflect the port number determined from
	 * a {@link org.acegisecurity.util.PortResolver}.
	 * </p>
	 * 
	 * @return the full URL of this request
	 */
	public String getFullRequestUrl()
	{
		return getHttpRequest().getScheme() + "://" + getHttpRequest().getServerName() + ":"
				+ getHttpRequest().getServerPort() + getHttpRequest().getContextPath()
				+ getRequestUrl();
	}

	public HttpServletRequest getHttpRequest()
	{
		return (HttpServletRequest) request;
	}

	public HttpServletResponse getHttpResponse()
	{
		return (HttpServletResponse) response;
	}

	public ServletRequest getRequest()
	{
		return request;
	}

	public String getRequestUrl()
	{
		String pathInfo = getHttpRequest().getPathInfo();
		String queryString = getHttpRequest().getQueryString();

		String uri = getHttpRequest().getServletPath();

		if (uri == null)
		{
			uri = getHttpRequest().getRequestURI();
			uri = uri.substring(getHttpRequest().getContextPath().length());
		}

		return uri + ((pathInfo == null) ? "" : pathInfo)
				+ ((queryString == null) ? "" : ("?" + queryString));
	}

	public ServletResponse getResponse()
	{
		return response;
	}

	public String toString()
	{
		return "FilterInvocation: URL: " + getRequestUrl();
	}
}
