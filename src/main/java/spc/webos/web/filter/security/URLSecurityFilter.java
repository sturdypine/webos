package spc.webos.web.filter.security;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.constant.Web;
import spc.webos.exception.AppException;
import spc.webos.service.common.LoginService;
import spc.webos.service.seq.UUID;
import spc.webos.service.seq.impl.TimeMillisUUID;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;
import spc.webos.web.filter.AbstractURLFilter;
import spc.webos.web.util.WebUtil;
import spc.webos.web.view.ExceptionView;

public class URLSecurityFilter extends AbstractURLFilter
{
	protected AntPathBasedFilterDefinitionMap source = new AntPathBasedFilterDefinitionMap();
	protected AntPathBasedFilterDefinitionMap errPageSource = new AntPathBasedFilterDefinitionMap();
	@Value("${app.web.session.suiClazz?spc.webos.web.common.SessionUserInfo}")
	protected String suiClazz = "spc.webos.web.common.SessionUserInfo";
	@Value("${app.web.session.uri.logout?/js/login/logout}")
	protected String logoutUri = "/js/login/logout";
	@Value("${app.web.session.uri.login?/js/login/login}")
	protected String loginUri = "/js/login/login";
	@Value("${app.web.session.uri.create?/v}")
	protected String[] createUri = { "/v" }; // 需要创建session的uri, 验证码
	@Value("${app.web.session.err.status?555}")
	protected int jsonErrStatus = 555;

	@Value("${app.web.session.token.info?_token}")
	protected String tokenKey = "_token";
	@Value("${app.web.session.token.app?_token}")
	protected String tokenAppKey = "_tokenapp";

	// 日志追踪信息格式：0: userCode+":"+counts, mvc
	// 1：session id+counts,mvc:+userCode, 2: uuid
	@Value("${app.web.session.logMode?2}")
	protected int logMode = 2;
	@Autowired(required = false)
	protected UUID uuid;
	protected LoginService loginService;
	public final static String AUTH_ANONYMOUS = "public"; // 匿名权限
	public final static String MUST_LOGIN = "login";
	public final static String GET_SECRET = "sget"; // url是服务器加密的
	public final static String XSS = "xss";
	public final static String EX_ACTION_PAGE = "page";
	public final static String EX_ACTION_JSON = "json";
	public final static String EX_ACTION_REDIRECT = "redirect";

	public void filter(ServletRequest request, ServletResponse response, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String uri = getUri(req);
		// 设置日志追踪信息
		boolean set = false;
		if (uuid != null) set = LogUtil.setTraceNo(SpringUtil.APPCODE + uuid.format(uuid.uuid()),
				LogUtil.shortUri(uri), false);
		SUI.SUI.set(null); // 清空当前线程的的登录环境，防止上次线程环境的残留
		boolean login = loginUri.equals(uri);
		Map<String, String> params = StringX.uri2params(uri, 0);
		if (!StringX.nullity(params.get(Web.REQ_KEY_LOGOUT)))
		{
			log.info("logout session uri:{}", uri);
			logout(req, res);
		}
		String token = params.get(tokenKey); // put token in uri only
		String tokenApp = params.get(tokenAppKey); // put token app in uri
		boolean createSession = login || !StringX.nullity(token)
				|| StringX.contain(createUri, uri, true);

		SUI sui = createSUI(req, res, uri, login, createSession); // 获得当前线程用户环境信息
		if (sui != null)
		{ // 非首次登陆，修改当前session信息
			sui.removeExpiredTransient(); // 删除过期临时信息
			sui.setLastVisitDt(new Date());
			SUI.SUI.set(sui); // 存放SessionUserInfo对象到ThreadLocal里面去
			// 防止频繁访问
			int tps = config.getProperty(Config.app_web_session_tps, true, -1);
			if (tps > 0 && sui.tps() > tps)
				throw new AppException(AppRetCode.FREQUENT_VISITS, new Object[] { sui.tps() });
		}
		try
		{ // 安全检查当前用户是否可以访问uri
			// 如果当前url带有token, 则check token
			if (!StringX.nullity(token) && loginService != null)
				loginService.token(tokenApp, token, false);
			String remoteHost = req.getRemoteHost();
			if (sui != null && !remoteHost.equals(sui.getLoginIP()))
			{ // 当前请求的IP不是登录IP
				log.info("{} is not login IP {}", remoteHost, sui.getLoginIP());
				logout(req, res); // 系统自动logout
				errorPage(req, res, uri, new AppException(AppRetCode.UN_LOGIN_IP));
				return;
			}
			security(req, response, chain, uri, sui, createSession);
			// 当前uri地址要退出
			if (uri.equalsIgnoreCase(logoutUri)) logout(req, res);
			else saveSession(sui); // 保存session
		}
		catch (Exception e)
		{
			errorPage(req, res, uri, e);
		}
		finally
		{
			if (set) LogUtil.removeTraceNo();
			SUI.SUI.set(null); // 清空当前线程的的登录环境，防止线程环境的残留
		}
	}

	protected void logout(HttpServletRequest req, HttpServletResponse res)
	{
		try
		{
			HttpSession session = req.getSession();
			log.info("logout session:{}", session != null ? session.getId() : "");
			if (session != null) session.invalidate();
		}
		catch (Exception e)
		{
		}
	}

	protected void security(ServletRequest request, ServletResponse response, FilterChain chain,
			String uri, SUI sui, boolean createSession) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		List<String> validRoles = source.lookupAttributes(uri); // 获得能合法请求此URI的角色,功能
		boolean xss = validRoles.contains(XSS);
		log.debug("validRoles:{}, by:{}, xss:{}", validRoles,
				(sui == null ? "" : sui.getUserCode()), xss);
		// 此资源需要防止xss攻击
		if (xss) req = new XssHttpServletRequestWrapper(req);
		String method = req.getMethod().toLowerCase(); // 当前资源是否定义了合法请求方式get/post
		if (!validRoles.contains(method))
		{
			log.info("method:{} unvalid for uri:{}", method, uri);
			errorPage(req, res, uri,
					new AppException(AppRetCode.URL_SECURIY, new Object[] { method }));
			return;
		}
		if (validRoles.contains(AUTH_ANONYMOUS) || createSession)
		{ // 不用登陆就能访问的资源
			chain.doFilter(request, response);
			return;
		}
		if (sui == null || sui.isEmpty())
		{
			log.info("sui empty:{}", uri);
			errorPage(req, res, uri, new AppException(AppRetCode.UN_LOGIN, new Object[] { uri }));
			return;
		}
		if (validRoles.contains(GET_SECRET) && !WebUtil.isSGet(req.getQueryString()))
		{ // 是否是服务器端生成的加密的get url
			log.info("not secret get queryString:{}", req.getQueryString());
			errorPage(req, res, uri, new AppException(AppRetCode.URL_SECURIY,
					new Object[] { req.getQueryString() }));
			return;
		}

		if (validRoles.contains(MUST_LOGIN))
		{ // 只需要登陆就能访问的资源
			chain.doFilter(req, response);
			return;
		}
		// 判断权限, 是否满足请求此URL
		// 1. 获得前置的非法访问的资源的角色
		List<String> unValidRoles = source.lookupAttributes('-' + uri); // 获得不能合法请求此URI的角色,功能
		if (unValidRoles.size() > 0)
		{
			int unValidTimes = 0;
			for (int i = 0; i < unValidRoles.size(); i++)
			{// 说明此资源是当前角色不能访问的, 由于一个人可能拥有多个角色, 只有他所拥有的角色都不能访问才能说明不能访问
				if (sui.containRole(unValidRoles.get(i))) unValidTimes++;
			}
			if (unValidTimes > 0)
			{ // 此人所拥有的所有角色都不能访问此模块
				request.setAttribute(Web.RESP_ATTR_ERROR_KEY, Boolean.TRUE);
				log.info("no right for:{}", uri);
				errorPage(req, res, uri, new AppException(AppRetCode.URL_SECURIY));
				return;
			}
		}

		// 2. 获得合法资源权限
		for (int i = 0; i < validRoles.size(); i++)
		{
			if (sui.containRole(validRoles.get(i)))
			{ // 此资源的权限被此人拥有, 则合法通过
				chain.doFilter(req, response);
				return;
			}
		}
		// 没能通过第 2 步的资源请求合法检查. 重定位到登陆页面
		log.info("no right to visit {}", uri);
		request.setAttribute(Web.RESP_ATTR_ERROR_KEY, Boolean.TRUE);
		errorPage(req, res, uri, new AppException(AppRetCode.URL_SECURIY));
	}

	protected SUI createSUI(HttpServletRequest req, HttpServletResponse res, String uri,
			boolean loginUri, boolean createSession)
	{
		SUI sui = null;
		HttpSession session = req.getSession(false); // 只有登录url才能创建session
		if (createSession || loginUri)
		{ // 当前uri需要创建session, 如果存在不创建，如果不存在则创建
			if (session != null)
			{ // 如果是当前session需要重新登录，无需删除当前session， 只需要情况sui的userCode信息即可
				sui = (SUI) session.getAttribute(SUI.USER_SESSION_INFO_KEY);
				if (sui != null && loginUri) sui.clear(); // 如果当前行为是要登录，则直接清除sui信息
				log.info("session:{}, sui exists:{}, loginUri:{}", session.getId(), sui != null,
						loginUri);
			}
			else
			{ // 没有session则创建
				session = req.getSession(true);
				int maxInactiveInterval = config.getProperty(Config.app_web_session_maxInactive,
						false, 1800);
				session.setMaxInactiveInterval(maxInactiveInterval); // session会话最大时间
				try
				{
					sui = newSUI(req, sui);
					session.setAttribute(SUI.USER_SESSION_INFO_KEY, sui);
					log.info("login create session:{}, max:{}, uri:{}", session.getId(),
							maxInactiveInterval, uri);
				}
				catch (Exception e)
				{
					log.error("create user session fail:: uri: " + uri + ", remote: "
							+ req.getRemoteAddr(), e);
				}
			}
		}
		if (session != null) sui = (SUI) session.getAttribute(SUI.USER_SESSION_INFO_KEY);
		log(req, res, sui, uri); // 日志打印当前登录信息
		if (sui != null) sui.request(req, res, session.getId(), session); // 登记一次登录信息
		return sui;
	}

	protected void saveSession(SUI sui) throws Exception
	{
	}

	protected SUI newSUI(HttpServletRequest req, SUI sui)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		log.info("login sui clazz:{}, maxInactiveInterval:{}, exists:{}", suiClazz,
				config.getProperty(Config.app_web_session_maxInactive, false, 1800), sui != null);
		if (sui == null) sui = (SUI) (Class.forName(suiClazz, true,
				Thread.currentThread().getContextClassLoader())).newInstance();
		sui.setLoginDt(new Date()); // 登录时间
		sui.setLoginIP(req.getRemoteHost()); // 登录客户端IP
		return sui;
	}

	protected void log(HttpServletRequest req, HttpServletResponse res, SUI sui, String uri)
	{
		String method = req.getMethod();
		String remoteHost = req.getRemoteHost();
		String user = sui == null ? "" : StringX.null2emptystr(sui.getUserCode());
		if ("GET".equalsIgnoreCase(method)) log.info("{} {} rc:{}_{}, {},{} {}?{}", user,
				remoteHost, (sui == null ? -1 : sui.requestCount()), (sui == null ? 0 : sui.tps()),
				req.getContentType(), method, uri, StringX.null2emptystr(req.getQueryString()));
		else log.info("{} {} rc:{}_{}, {} {},{}, len:{}", user, remoteHost,
				(sui == null ? -1 : sui.requestCount()), (sui == null ? 0 : sui.tps()),
				req.getContentType(), method, uri, req.getContentLength());
	}

	protected void errorPage(HttpServletRequest req, HttpServletResponse res, String uri,
			Exception ex) throws IOException
	{
		res.setStatus(500);
		boolean login = !((ex instanceof AppException)
				&& AppRetCode.UN_LOGIN.equalsIgnoreCase(((AppException) ex).getCode()));
		String redirect = req.getContextPath();
		List<String> handler = errPageSource.lookupAttributes(uri);
		log.info("err page:{}, uri:{}, login:{}, ex:{}", handler, uri, login, ex.toString());
		String action = handler != null && !handler.isEmpty() ? handler.get(0) : "";
		if (handler == null || handler.size() == 0) redirect = req.getContextPath();
		else if (action.startsWith("json"))
		{ // json 应答
			Map model = new HashMap();
			model.put(Common.MODEL_EXCEPTION, ex);
			// extjs ds格式错误应答时status必须是2xx正常状态, 其它json格式错误应答时status必须是5xx错误状态
			new ExceptionView(handler.get(1), Common.FILE_JSON_CONTENTTYPE,
					!StringX.nullity(req.getParameter("_extds")) ? 222 : jsonErrStatus)
							.render(model, req, res);
			return;
		}
		else if (login && EX_ACTION_PAGE.equalsIgnoreCase(action))
		{ // err page
			Map model = new HashMap();
			model.put(Common.MODEL_EXCEPTION, ex);
			new ExceptionView(handler.get(1), Common.FILE_HTML_CONTENTTYPE, 0).render(model, req,
					res);
			return;
		}
		else if (EX_ACTION_REDIRECT.equalsIgnoreCase(action)) redirect = handler.get(1);
		log.info("redirect to {}", redirect);
		res.sendRedirect(redirect);
	}

	/**
	 * 获得配置的Url资源对应的权限 or 角色 or 功能模块信息
	 * 
	 * @param s
	 */
	public void setDefinitionSource(String s)
	{
		setDefinitionMap(source, s);
	}

	public void setSuiClazz(String suiClazz)
	{
		this.suiClazz = suiClazz;
	}

	public void setLoginUri(String loginUri)
	{
		this.loginUri = loginUri;
	}

	public void setLogMode(int logMode)
	{
		this.logMode = logMode;
	}

	public void setTokenKey(String tokenKey)
	{
		this.tokenKey = tokenKey;
	}

	public void setTokenAppKey(String tokenAppKey)
	{
		this.tokenAppKey = tokenAppKey;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setUuidWorkId(int workId)
	{
		this.uuid = new TimeMillisUUID(workId);
	}

	public void setErrPageSource(String s)
	{
		setDefinitionMap(errPageSource, s);
	}

	public void setLogoutUri(String logoutUri)
	{
		this.logoutUri = logoutUri;
	}

	public void setJsonErrStatus(int errStatus)
	{
		this.jsonErrStatus = errStatus;
	}

	public void setCreateUri(String[] createUri)
	{
		this.createUri = createUri;
	}

	public void setLoginService(LoginService loginService)
	{
		this.loginService = loginService;
	}
}

class XssHttpServletRequestWrapper extends HttpServletRequestWrapper
{
	public XssHttpServletRequestWrapper(HttpServletRequest servletRequest)
	{
		super(servletRequest);
	}

	public String[] getParameterValues(String parameter)
	{
		String[] values = super.getParameterValues(parameter);
		if (values == null) return null;
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++)
			encodedValues[i] = StringEscapeUtils.escapeHtml4(values[i]);
		return encodedValues;
	}

	public String getParameter(String parameter)
	{
		String value = super.getParameter(parameter);
		if (value == null) return null;
		return StringEscapeUtils.escapeHtml4(value);
	}

	public String getHeader(String name)
	{
		String value = super.getHeader(name);
		if (value == null) return null;
		return StringEscapeUtils.escapeHtml4(value);
	}
}
