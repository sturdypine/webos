package spc.webos.web.common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.filter.multipart.MultipartEntry;
import spc.webos.web.filter.multipart.MultipartFilter;
import spc.webos.web.util.URLEncoder;
import spc.webos.web.util.WebUtil;

public abstract class AbstractSessionUserInfo<T> implements SUI, Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;
	protected T user; // 操作员基本信息
	protected String loginIP; // 登陆的IP
	protected Date loginDt; // 登陆时间
	protected Date lastVisitDt; // 上一次访问服务器时间

	protected Map<String, TransientInSession<?>> transients = new ConcurrentHashMap<>();
	protected Map<String, Serializable> persists = new ConcurrentHashMap<>();
	protected String sgetSalt = SpringUtil.random(16); // 用于签名GET方式的url，防止客户端自己设置

	protected List<String> roles; // 授权的角色ID
	protected List<String> services; // 授权能访问的服务
	protected List<String> sqlIds; // 授权能访问的sqlId

	// 请求累计次数
	protected AtomicInteger requestCount = new AtomicInteger(0);
	// 最近10秒累计访问量
	protected long cur10s;
	protected AtomicInteger tps10 = new AtomicInteger(0);

	protected transient HttpSession session; // 当前用户的session
	final static transient ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();
	final static transient ThreadLocal<HttpServletResponse> RESPONSE = new ThreadLocal<>();
	protected transient String sessionId;

	final static String VERIFY_IMG_KEY = "_VF_IMG";

	public T getUser()
	{
		return user;
	}

	public void setUser(T user)
	{
		this.user = user;
	}

	public void clear() // 清楚当前用户信息
	{
		roles = null;
		services = null;
		sqlIds = null;
		TransientInSession<?> verify = transients.get(VERIFY_IMG_KEY);
		transients.clear();
		if (verify != null) transients.put(VERIFY_IMG_KEY, verify);
		persists.clear();
		user = null;
	}

	public boolean isEmpty()
	{
		return user == null;
	}

	public int tps()
	{
		return tps10.get();
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public boolean rejectUserCode(String userCode)
	{
		if (!userCode.equals(getUserCode()))
		{ // 如果当前用户不是session用户，则数据不合法
			throw new AppException(AppRetCode.MSG_ERRS, "Not session User!!!");
		}
		return false;
	}

	public boolean rejectUserName(String userName)
	{
		if (!userName.equals(getUserName()))
		{ // 如果当前用户不是session用户，则数据不合法
			throw new AppException(AppRetCode.MSG_ERRS, "Not session User!!!");
		}
		return false;
	}

	// 触发一次request请求，用于内部计数， 也可以统计当前用户的请求次数
	public void request(HttpServletRequest request, HttpServletResponse response, String sessionId,
			HttpSession session)
	{
		this.sessionId = sessionId;
		this.session = session;
		requestCount.incrementAndGet();
		REQUEST.set(request);
		RESPONSE.set(response);
		long cur10s = System.currentTimeMillis() / 10000; // 当前10秒
		if (this.cur10s == cur10s) tps10.incrementAndGet();
		else
		{
			tps10 = new AtomicInteger(0);
			this.cur10s = cur10s;
		}
	}

	public Map<String, List<MultipartEntry>> upload()
	{
		return (Map<String, List<MultipartEntry>>) REQUEST.get()
				.getAttribute(MultipartFilter.REQ_ATTR_ENTRIES_KEY);
	}

	// 客户端多线程请求时，可能当前线程多次读取过程中不一致
	public int requestCount()
	{
		return requestCount.get();
	}

	public HttpServletRequest request()
	{
		return REQUEST.get();
	}

	public HttpServletResponse response()
	{
		return RESPONSE.get();
	}

	public HttpSession session()
	{
		return session;
	}

	public void removeInPersist(String key)
	{
		persists.remove(key);
	}

	public void setInPersist(String key, Serializable value)
	{
		persists.put(key, value);
	}

	public <T> T getInPersist(String key, T def)
	{
		T v = (T) persists.get(key);
		return v == null ? def : v;
	}

	public void setInTransient(String key, TransientInSession<?> tis)
	{
		if (transients == null) transients = new ConcurrentHashMap<>();
		transients.put(key, tis);
	}

	public void removeInTransient(String key)
	{
		if (transients == null) transients = new ConcurrentHashMap<>();
		transients.remove(key);
	}

	public <T> void setInTransient(String key, T value, int cacheSeconds)
	{
		if (transients == null) transients = new ConcurrentHashMap<>();
		transients.put(key, new TransientInSession<T>(value, cacheSeconds));
	}

	public <T> T getInTransient(String key, T def)
	{
		if (transients == null) transients = new ConcurrentHashMap<>();
		TransientInSession<T> tis = (TransientInSession<T>) transients.get(key);
		return tis == null ? def : tis.value();
	}

	public void setVerifyCode(String verifyCode)
	{
		if (StringX.nullity(verifyCode)) transients.remove(VERIFY_IMG_KEY);
		else setInTransient(VERIFY_IMG_KEY, verifyCode, 120); // 验证码有效时间2分钟
	}

	public String getVerifyCode()
	{
		return getInTransient(VERIFY_IMG_KEY, "");
	}

	public String getLoginIP()
	{
		return loginIP;
	}

	public void setLoginIP(String loginIP)
	{
		this.loginIP = loginIP;
	}

	public boolean containSqlId(String s, Map param)
	{
		if (sqlIds == null) return true;
		return WebUtil.isAuth(sqlIds, s.replace('.', '_'));
	}

	public boolean containService(String s)
	{
		if (services == null) return true;
		return WebUtil.isAuth(services, s);
	}

	public boolean containRole(String role)
	{
		for (String r : roles)
			if (r.startsWith(role)) return true;
		return false;
	}

	public void removeExpiredTransient()
	{
		if (transients == null) transients = new ConcurrentHashMap<>();
		if (transients.isEmpty()) return;
		long currentTimeMillis = System.currentTimeMillis();
		transients.forEach((k, tis) -> {
			if (tis.isExpired(currentTimeMillis)) transients.remove(k);
		});
	}

	public Date getLoginDt()
	{
		return this.loginDt;
	}

	public void setLoginDt(Date loginDate)
	{
		this.loginDt = loginDate;
	}

	public Date getLastVisitDt()
	{
		return lastVisitDt;
	}

	public void setLastVisitDt(Date lastVisitDt)
	{
		this.lastVisitDt = lastVisitDt;
	}

	public Map<String, Serializable> getPermanence()
	{
		return persists;
	}

	public String sget(String queryString)
	{
		return URLEncoder.sget(queryString, sgetSalt);
	}

	public boolean isSGet(String queryString)
	{
		return URLEncoder.isSGet(queryString, sgetSalt);
	}

	public AtomicInteger getRequestCount()
	{
		return requestCount;
	}

	public void setRequestCount(AtomicInteger requestCount)
	{
		this.requestCount = requestCount;
	}

	public List<String> getRoles()
	{
		return roles;
	}

	public void setRoles(List<String> roles)
	{
		this.roles = roles;
	}

	public List<String> getServices()
	{
		return services;
	}

	public void setServices(List<String> services)
	{
		this.services = services;
	}

	public List<String> getSqlIds()
	{
		return sqlIds;
	}

	public void setSqlIds(List<String> sqlIds)
	{
		this.sqlIds = sqlIds;
	}
}
