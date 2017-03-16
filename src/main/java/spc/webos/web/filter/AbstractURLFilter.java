package spc.webos.web.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import spc.webos.config.AppConfig;
import spc.webos.config.Config;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.web.filter.security.AntPathBasedFilterDefinitionMap;

public abstract class AbstractURLFilter implements Filter, ApplicationContextAware
{
	protected Logger log = LoggerFactory.getLogger(getClass()); // 由于Filter数量不会太多,

	// 所以放到一个日志中
	protected String[] antPathURLPattern;
	protected boolean kickOffApplicationName = true;
	protected Map<String, Map> paramMaps; // 提高访问速度, 因为每个配置的Pattern
											// Url后面带有可配置的参数
	protected String delim = "@"; // 配置URL特殊
	protected ApplicationContext appCxt;
	@Autowired(required = false)
	protected Config config = AppConfig.getInstance();

	public void setDelim(String delim)
	{
		this.delim = delim;
	}

	public void setApplicationContext(ApplicationContext appCxt)
	{
		this.appCxt = appCxt;
	}

	public void setKickOffApplicationName(boolean kickOffApplicationName)
	{
		this.kickOffApplicationName = kickOffApplicationName;
	}

	protected String getUri(HttpServletRequest req)
	{
		String uri = req.getRequestURI();
		if (kickOffApplicationName)
		{
			String app = req.getContextPath();
			if (uri.startsWith(app)) uri = uri.substring(app.length());
		}
		return uri;
	}

	public void setAntPathURLPattern(String[] antPathURLPattern)
	{
		this.antPathURLPattern = antPathURLPattern;
	}

	/**
	 * 客户端刷新一个URL时候，可能采用增加一个时间参数的方式请求，防止客户端缓存，此方法去掉此参数_dc=2342342
	 * 
	 * @param queryString
	 * @param pattern
	 *            _dc=
	 * @return
	 */
	public String restoreURL(String queryString, String pattern)
	{
		if (queryString == null || queryString.length() == 0) return null;
		int start = queryString.indexOf(pattern); // 取消客户端增加的参数_dc=2023423902
		if (start < 0) return queryString;
		int end = queryString.indexOf('&', start + 1);
		StringBuilder buf = new StringBuilder();
		if (start > 0) buf.append(queryString.substring(0, start));
		if (end > 0) buf.append(queryString.substring(end));
		return buf.toString();
	}

	/**
	 * 判断此Filter是否支持当前特定的URI, 如果不支持返回NULL, 否则返回配置文件中的URL模式字符串 因为此模式字符串后面可能带有参数信息
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public String supportRequest(ServletRequest req, ServletResponse res)
	{
		if (!(req instanceof HttpServletRequest)) return null;
		// 如果当前Filter没有配置资源匹配符, 默认是对所有资源进行filter
		if (this.antPathURLPattern == null) return StringX.EMPTY_STRING;
		AntPathMatcher apm = new AntPathMatcher();
		String uri = ((HttpServletRequest) req).getRequestURI();
		if (kickOffApplicationName)
		{ // kick off appliction name...
			String appName = ((HttpServletRequest) req).getContextPath();
			if (uri.startsWith(appName)) uri = uri.substring(appName.length());
		}
		for (int i = 0; i < antPathURLPattern.length; i++)
		{
			String uriPattern = antPathURLPattern[i];
			if (uriPattern.indexOf('$') > 0)
			{
				String query = ((HttpServletRequest) req).getQueryString();
				if (query != null) uri += "$" + query;
			}
			if (uriPattern.startsWith("-"))
			{ // 遇到此Filter不支持资源的匹配模式
				if (apm.match(uriPattern.substring(1), uri))
					// 当前URL匹配上此不支持的URL资源
					return null;
			}
			else if (apm.match(uriPattern, uri)) return uriPattern;
		}
		return null; // 没有匹配成功, 不执行当前Filter的动作
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException
	{
		String patternURL = supportRequest(req, res);
		if (patternURL == null) chain.doFilter(req, res);
		else filter(req, res, chain, patternURL);
	}

	public abstract void filter(ServletRequest req, ServletResponse res, FilterChain chain,
			String patternURL) throws IOException, ServletException;

	public void init(FilterConfig filterConfig)
	{
	}

	@PreDestroy
	public void destroy()
	{
	}

	/**
	 * 初始化, 把配置的Pattern Url中#后面带的参数, 放入到Map中缓存起来, 提高访问速度
	 */
	@PostConstruct
	public void init()
	{
		if (antPathURLPattern == null || antPathURLPattern.length == 0) return;
		paramMaps = new HashMap<>();
		for (int i = 0; i < antPathURLPattern.length; i++)
		{
			int idx = antPathURLPattern[i].indexOf(delim);
			if (idx < 0) continue; // 没有配置参数

			String param = antPathURLPattern[i].substring(idx + 1);
			// Map paramMap = StringX.str2map(param, '&'); //
			// 每个URL的参数配置采用json格式，提升功能.modified by spc 2010-3-10
			Map paramMap = (Map) JsonUtil.gson2obj(param);
			String pattern = antPathURLPattern[i].substring(0, idx);
			antPathURLPattern[i] = pattern;
			if (paramMap != null)
				// 采用patternURL 而不采用queryString, 是因为前者也是唯一的, 并可能短一些
				paramMaps.put(pattern, paramMap);
		}
		if (paramMaps.isEmpty()) paramMaps = null;
	}

	protected Map queryStringToMap(String queryString)
	{
		if (paramMaps == null || paramMaps.isEmpty()) return Collections.EMPTY_MAP;
		Map param = paramMaps.get(queryString);
		if (param != null) return param;
		return Collections.EMPTY_MAP;
	}

	protected void setDefinitionMap(AntPathBasedFilterDefinitionMap source, String s)
	{
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line;

		while (true)
		{
			try
			{
				line = br.readLine();
			}
			catch (IOException ioe)
			{
				throw new IllegalArgumentException(ioe.getMessage());
			}
			if (line == null) break;
			line = line.trim();
			if (line.startsWith("//")) continue;
			if (line.equals("CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON"))
			{
				source.setConvertUrlToLowercaseBeforeComparison(true);
				continue;
			}
			if (line.lastIndexOf('=') == -1) continue;

			// Tokenize the line into its name/value tokens
			String[] nameValue = StringUtils.delimitedListToStringArray(line, "=");
			String name = nameValue[0].trim().toLowerCase();
			String value = nameValue[1].trim();

			if (!StringUtils.hasLength(name) || !StringUtils.hasLength(value))
				throw new IllegalArgumentException(
						"Failed to parse a valid name/value pair from " + line);
			String[] roles = StringUtils.delimitedListToStringArray(value, StringX.COMMA);
			// 每一个URL对应的角色 或者 权限配置 或者 模块功能号 信息
			List<String> configAttributes = new ArrayList<String>();
			for (int i = 0; roles != null && i < roles.length; i++)
				configAttributes.add(roles[i]);
			roles = null;

			// Register the regular expression and its attribute
			source.addSecureUrl(name, configAttributes);
		}
	}

	public void setConfig(Config config)
	{
		this.config = config;
	}
}
