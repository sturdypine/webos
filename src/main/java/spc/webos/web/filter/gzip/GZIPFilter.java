package spc.webos.web.filter.gzip;

/**
 * 把数据压缩后返回客户端, 减少网络数据流量.. 此filter应该放在多Filter的最后面
 */
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spc.webos.web.filter.AbstractURLFilter;

public class GZIPFilter extends AbstractURLFilter
{
	public final static String HTTP_HEADER_CONTENT_ENCODING = "content-encoding";
	public final static String HTTP_HEADER_ACCEPT_ENCODING = "accept-encoding";
	public final static String ENCODING_GZIP = "gzip";

	public void filter(ServletRequest req, ServletResponse res, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		HttpServletRequest wrappedRequest = request;
		String ce = request.getHeader(HTTP_HEADER_CONTENT_ENCODING);
		if (ce != null && ce.indexOf(ENCODING_GZIP) >= 0)
		{
			log.info("content: {}, uri:{}", ce, request.getRequestURI());
			wrappedRequest = new GZIPRequestWrapper(request);
		}

		String ae = request.getHeader(HTTP_HEADER_ACCEPT_ENCODING);
		if (ae != null && ae.indexOf(ENCODING_GZIP) >= 0)
		{
			log.info("accept: {}, uri:{}", ae, request.getRequestURI());
			GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(response);
			chain.doFilter(wrappedRequest, wrappedResponse);
			wrappedResponse.finishResponse();
			return;
		}
		chain.doFilter(wrappedRequest, res);
	}
}
