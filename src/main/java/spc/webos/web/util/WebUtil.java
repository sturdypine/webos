package spc.webos.web.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.ThreadLocalRandom;
import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.util.FTLUtil;
import spc.webos.util.FileUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.MethodUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;

public class WebUtil
{
	static Logger log = LoggerFactory.getLogger(WebUtil.class);
	public final static ThreadLocal<Boolean> WEB = new ThreadLocal<>();
	static List<String> DEF_PUBLIC_SERVICE = Arrays.asList("login.*", "extjs.*",
			"persistence.query*");
	static List<String> DEF_PUBLIC_SQL = new ArrayList<>(); // Arrays.asList("class_spc_*");
	static List<String> DEF_LOGIN_SERVICE = new ArrayList<>();
	static List<String> DEF_LOGIN_SQL = new ArrayList<>();

	public static boolean isWeb()
	{ // 当前线程是否是Web环境
		return WEB.get() != null && WEB.get();
	}

	public static boolean isAuth(List<String> auth, String s)
	{ // 是否授权资源:service & sqlId
		s = s.toLowerCase();
		for (String ss : auth)
		{
			if (ss.equals(s)
					|| (ss.endsWith("*") && s.startsWith(ss.substring(0, ss.length() - 1))))
				return true;
		}
		return false;
	}

	public static boolean containSqlId(String sqlId, Map<String, Object> param)
	{
		if (!isWeb()) return true; // 1. 非Web环境不限制SQL访问
		// 2. 是否属于公共可访问SQL
		if (isAuth(AppConfig.getInstance().getProperty(Config.app_web_auth_public_sql, false,
				DEF_PUBLIC_SQL), sqlId))
			return true;
		// 3. 是否属于当前用户授权SQL
		SUI sui = SUI.SUI.get();
		if (sui != null)
		{
			if (isAuth(AppConfig.getInstance().getProperty(Config.app_web_auth_login_sql, false,
					DEF_LOGIN_SQL), sqlId))
				return true;
			return sui.containSqlId(sqlId, param);
		}
		return false;
	}

	public static void containService(String s, String m, int argNum)
	{
		String service = s + '.' + m + '$' + argNum;
		List<String> publicService = AppConfig.getInstance()
				.getProperty(Config.app_web_auth_public_service, false, DEF_PUBLIC_SERVICE);
		if (isAuth(publicService, service)) return;
		SUI sui = SUI.SUI.get();
		if (sui != null)
		{ // 如果已经登录，则必须检查当前登陆者执行服务的权限
			log.info("unauthorized service:({}.{}), user:{}", s, m, sui.getUserCode());
			boolean auth = false;
			if (isAuth(AppConfig.getInstance().getProperty(Config.app_web_auth_login_service, false,
					DEF_LOGIN_SERVICE), service))
				auth = true;
			if (!auth) auth = sui.containService(service);
			if (!auth) throw new AppException(AppRetCode.SERVICE_UNAUTH, new Object[] { service });
		}
	}

	public static String generateSessionId(int sessionLen)
	{
		byte[] id = new byte[sessionLen];
		ThreadLocalRandom.current().nextBytes(id);
		return SpringUtil.APPCODE + SpringUtil.JVM + "." + StringX.md5(id);
	}

	/**
	 * 支持前端发送json格式数据，但http header必须是json格式
	 * 
	 * @param req
	 * @param params
	 * @return
	 */
	public static Map<String, Object> request2map(HttpServletRequest req,
			Map<String, Object> params) throws Exception
	{
		params = FTLUtil.model(params);
		String contentType = req.getContentType();
		if (!StringX.nullity(contentType) && contentType.indexOf("json") >= 0)
		{
			String json = new String(FileUtil.is2bytes(req.getInputStream(), false),
					Common.CHARSET_UTF8);
			if (!StringX.nullity(json)) params.putAll((Map) JsonUtil.json2obj(json));
		}
		else
		{
			Enumeration names = req.getParameterNames();
			while (names.hasMoreElements())
			{
				String paramName = names.nextElement().toString();
				String value = req.getParameter(paramName);
				if (value != null && value.length() > 0)
					params.put(paramName, StringX.utf82str(value));
			}
		}

		params.put(Common.MODEL_REQUEST_KEY, req);
		params.put(Common.MODEL_APP_PATH_KEY, req.getContextPath());
		return params;
	}

	// 从请求中得到所有附件的参数名
	public static List<String> getUploadFileNames(HttpServletRequest req)
	{
		List<String> names = new ArrayList<>();
		Enumeration enu = req.getParameterNames();
		while (enu.hasMoreElements())
		{
			String name = (String) enu.nextElement();
			if (name.startsWith("file.")) names.add(name);
		}
		return names;
	}

	// 执行一批json参数格式的服务，用于类似BATCH_SQL查询，返回给page or 指定view的数据model
	// 执行的服务必须是S.开头
	public static void invokeJsonService(HttpServletRequest req, Map params, String servicePostfix)
			throws Exception
	{
		for (Object k : params.keySet())
		{
			String name = k.toString();
			if (!name.startsWith("S.")) continue;
			int idx = name.lastIndexOf('.');
			if (idx < 2) continue;
			String s = name.substring(2, idx);
			String m = name.substring(idx + 1);
			String p = (String) params.get(name);
			log.info("S. jscall:{}.{}", s, m);
			log.debug("args:{}", p);
			// 自动添加Service后缀
			Object args = StringX.nullity(p) ? null : JsonUtil.json2obj(p);
			int argNum = getMethodArgNum(s, m, args);
			Object ret = SpringUtil.jsonCall(s + servicePostfix, m, args, argNum);
			if (ret != null) params.put(s + '_' + m, ret);
		}
	}

	public static int getMethodArgNum(String s, String m, Object requestArgs)
	{
		int argNum = -1;
		if (requestArgs == null) argNum = 0;
		else if (requestArgs instanceof List) argNum = ((List) requestArgs).size();
		else
		{ // restful style
			int idx = m.indexOf('$');
			if (idx > 0)
			{
				argNum = Integer.parseInt(m.substring(idx + 1));
				m = m.substring(0, idx);
			}
			else
			{
				Method me = MethodUtil
						.findMethod(SpringUtil.getInstance().getBean(s, null).getClass(), m, -1);
				argNum = me.getParameterCount();
			}
		}
		containService(s, m, argNum);
		return argNum;
	}

	public static final String sgetSalt = SpringUtil.random(32); // 公共sget连接安全盐

	public static String sget(String queryString)
	{
		SUI sui = SUI.SUI.get();
		if (sui != null) return sui.sget(queryString);
		return URLEncoder.sget(queryString, sgetSalt);
	}

	public static boolean isSGet(String queryString)
	{
		SUI sui = SUI.SUI.get();
		if (sui != null) return sui.isSGet(queryString);
		return URLEncoder.isSGet(queryString, sgetSalt);
	}
}
