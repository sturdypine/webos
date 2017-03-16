package spc.webos.web.filter.encode;

import java.io.IOException;
import java.lang.reflect.Proxy;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spc.webos.web.filter.AbstractURLFilter;
import spc.webos.web.filter.multipart.MultipartFilter;

public class CharacterEncodingFilter extends AbstractURLFilter
{
	String srcEncode = "ISO8859_1";
	String targetEncode = "UTF-8";

	public void setSrcEncode(String srcEncode)
	{
		this.srcEncode = srcEncode;
	}

	public void setTargetEncode(String targetEncode)
	{
		this.targetEncode = targetEncode;
	}

	public void filter(ServletRequest req, ServletResponse resp,
			FilterChain chain, String patternURL) throws IOException,
			ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// if (requestEncode) request.setCharacterEncoding(encoding);

		// if (convertUTF8)
		// {
		// try
		// {
		// chain.doFilter(request, new EncodingResponseWrapper(response));
		// }
		// catch(Exception e)
		// {
		// e.printStackTrace();
		// }
		// return;
		// }
		chain.doFilter(createRequestProxy(request), response);
		if (responseEncode) response.setCharacterEncoding(encoding);
		// String uri = request.getRequestURI();

		// response.addHeader("pageEncoding", encoding);
		// response.addHeader("contentType", "text/html;charset=" + encoding);
	}

	HttpServletRequest createRequestProxy(HttpServletRequest request)
	{
		Class[] clazzArray = (Class[]) MultipartFilter.CLASS_MAP.get(request
				.getClass());
		if (clazzArray == null) clazzArray = MultipartFilter.getClazz(request
				.getClass());
		return (HttpServletRequest) Proxy.newProxyInstance(
				HttpServletRequest.class.getClassLoader(), clazzArray,
				new RequestProxy(request, this.srcEncode, this.targetEncode));
	}

	boolean convertUTF8;
	String encoding; // 输入输出流的字符集
	boolean requestEncode;
	boolean responseEncode;

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public void setRequestEncode(boolean requestEncode)
	{
		this.requestEncode = requestEncode;
	}

	public void setResponseEncode(boolean responseEncode)
	{
		this.responseEncode = responseEncode;
	}

	public void setConvertUTF8(boolean convertUTF8)
	{
		this.convertUTF8 = convertUTF8;
	}
}
