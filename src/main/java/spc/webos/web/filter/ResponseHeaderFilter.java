package spc.webos.web.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spc.webos.config.AppConfig;

public class ResponseHeaderFilter extends AbstractURLFilter
{
	protected Map<String, String> defaultResponseParam = new HashMap<>();

	public void filter(ServletRequest req, ServletResponse res, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		// 加载特定URI的配置信息
		Map<String, String> ext = (Map<String, String>) queryStringToMap(patternURL);
		log.info("header uri:{}, ext:{}", request.getRequestURI(), ext != null);
		defaultResponseParam.forEach((k, v) -> {
			if (!AppConfig.isProductMode() && k.equalsIgnoreCase("Cache-Control")) return;
			log.debug("header::{}={}", k, v);
			response.setHeader(k, v);
		});
		if (ext == null) return;
		ext.forEach((k, v) -> {
			if (!AppConfig.isProductMode() && k.equalsIgnoreCase("Cache-Control")) return;
			response.setHeader(k, v);
			log.debug("header ext::{}={}", k, v);
		});
		chain.doFilter(req, res);
	}

	public void setDefaultResponseParam(Map<String, String> defaultResponseParam)
	{
		this.defaultResponseParam = defaultResponseParam;
	}
}
