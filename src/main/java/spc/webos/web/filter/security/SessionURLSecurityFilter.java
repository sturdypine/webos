package spc.webos.web.filter.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import spc.webos.cache.MapCache;
import spc.webos.constant.Config;
import spc.webos.redis.JedisTemplate;
import spc.webos.util.FileUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;
import spc.webos.web.util.WebUtil;

/**
 * 使用redis作为session存储
 * 
 * @author chenjs
 *
 */
public class SessionURLSecurityFilter extends URLSecurityFilter
{
	@Autowired(required = false)
	protected JedisTemplate jedis;
	protected MapCache<String, SUI> cache = new MapCache<>(1800, 200, 900000, 1800000); // 存储session
	@Value("${app.web.session.cookie.name?webossid}")
	protected String sessionIdInCookieName = "webossid";
	@Value("${app.web.session.cookie.expire?36000}")
	protected int cookieExpire = 36000; // cookie失效时间，10小时
	// 0: tomcat管理，1: 本地cache管理，2：redis管理，3：cache & redis
	@Value("${app.web.session.mode?3}")
	protected int sessionMode = 3;
	@Value("${app.web.session.len?32}")
	protected int sessionLen = 32;
	@Value("${app.web.session.redis.prefix?session:}")
	protected String redisKeyPrefix = "session:";

	public void init()
	{
		super.init();
		int maxInactiveInterval = config.getProperty(Config.app_web_session_maxInactive, false,
				1800);
		cache = new MapCache<>(maxInactiveInterval, 200, maxInactiveInterval * 500,
				maxInactiveInterval * 1000); // 存储session
	}

	protected SUI createSUI(HttpServletRequest req, HttpServletResponse res, String uri,
			boolean loginUri, boolean createSession)
	{
		if (sessionMode == 0) return super.createSUI(req, res, uri, loginUri, createSession);
		return createRedisSUI(req, res, uri, loginUri, createSession);
	}

	protected String getSessionId(HttpServletRequest req)
	{
		if (req.getCookies() == null) return null;
		if (log.isDebugEnabled())
		{
			StringBuilder str = new StringBuilder();
			for (Cookie cookie : req.getCookies())
				str.append("\n" + cookie.getName() + "=" + cookie.getValue());
			log.debug("cookies:{}", str);
		}
		for (Cookie cookie : req.getCookies())
			if (sessionIdInCookieName.equals(cookie.getName())) return cookie.getValue();
		return null;
	}

	protected void setSessionId(HttpServletRequest req, HttpServletResponse res, String sessionId)
	{
		Cookie cookie = new Cookie(sessionIdInCookieName, sessionId);
		log.debug("cookie path:{}", req.getContextPath());
		cookie.setPath(req.getContextPath());
		cookie.setMaxAge(cookieExpire);
		res.addCookie(cookie);
	}

	protected SUI createRedisSUI(HttpServletRequest req, HttpServletResponse res, String uri,
			boolean loginUri, boolean createSession)
	{
		String sessionId = getSessionId(req);
		SUI sui = null;
		try
		{
			sui = StringX.nullity(sessionId) ? null : resumeSession(sessionId);
			if (loginUri && sui != null)
			{ // sui存在
				log.info("login del original session:{}", sessionId);
				sui.clear();
			}
			else if ((loginUri || createSession) && sui == null)
			{ // sui不存在且需要创建
				sessionId = WebUtil.generateSessionId(sessionLen);
				sui = newSUI(req, sui);
				setSessionId(req, res, sessionId); // 保存session id到客户端
				log.info("create session:{}", sessionId);
			}
			log(req, res, sui, uri); // 日志打印当前登录信息
			if (sui != null) sui.request(req, res, sessionId, null); // 登记一次登录信息
		}
		catch (Exception e)
		{
			log.error("create user session fail:: uri: " + uri + ", remote: " + req.getRemoteAddr()
					+ ", id:" + sessionId, e);
		}
		return sui;
	}

	protected void logout(HttpServletRequest req, HttpServletResponse res)
	{
		log.info("logout session mode:{}", sessionMode);
		if (sessionMode == 0) super.logout(req, res);
		else
		{
			String sessionId = getSessionId(req);
			try
			{
				if (!StringX.nullity(sessionId)) deleteSession(sessionId);
			}
			catch (Exception e)
			{
				log.warn("fail to delete session:" + sessionId, e);
			}
		}
	}

	protected SUI resumeSession(String sessionId)
	{
		SUI sui = null;
		if (sessionMode == 1 || sessionMode == 3)
		{ // cache
			log.info("Cache resume session:{}, exsits:{}, mode:{}", sessionId,
					cache.containsKey(sessionId), sessionMode);
			sui = cache.get(sessionId);
		}
		if (sui != null) return sui;

		if (jedis.isReady() && (sessionMode == 2 || sessionMode == 3))
		{ // redis
			StringBuilder value = new StringBuilder();
			String key = redisKeyPrefix + sessionId;
			try
			{
				jedis.execute((redis) -> {
					String v = redis.get(key);
					log.info("Redis resume session:{}, len:{}, mode:{}", key,
							v == null ? 0 : v.length(), sessionMode);
					log.debug("Redis read:{}, value:{}", key, v);
					if (v != null) value.append(v);
				});
				if (value.length() > 0)
					return (SUI) FileUtil.unfst(StringX.decodeBase64(value.toString()));
			}
			catch (Exception e)
			{
				log.info("Fail to redis resume session: " + key + ", " + e);
			}
		}
		return sui;
	}

	protected void saveSession(SUI sui)
	{
		if (sui == null) return;
		int maxInactiveInterval = config.getProperty(Config.app_web_session_maxInactive, false,
				1800);
		if (sessionMode == 1 || sessionMode == 3)
		{ // cache
			log.info("Cache save session:{}, expire:{}", sui.getSessionId(), maxInactiveInterval);
			cache.put(sui.getSessionId(), sui);
		}
		if (jedis.isReady() && (sessionMode == 2 || sessionMode == 3))
		{ // redis
			String key = redisKeyPrefix + sui.getSessionId();
			try
			{
				String value = StringX.base64(FileUtil.fst(sui));
				jedis.execute((redis) -> {
					redis.setex(key, maxInactiveInterval, value);
					log.info("Redis save session:{}, expire:{}, len:{}", key, maxInactiveInterval,
							value.length());
				});
			}
			catch (Exception e)
			{
				// if (sessionMode == 2) throw e;
				log.warn("Fail to redis save:" + key + ", " + e);
			}
		}
	}

	protected void deleteSession(String sessionId)
	{
		if (sessionMode == 1 || sessionMode == 3)
		{
			log.info("Cache del session:{}, mode:{}", sessionId, sessionMode);
			cache.remove(sessionId);
		}
		if (jedis.isReady() && (sessionMode == 2 || sessionMode == 3))
		{
			String key = redisKeyPrefix + sessionId;
			try
			{
				jedis.execute((redis) -> {
					redis.del(key);
					log.info("Redis del session:{}, mode:{}", key, sessionMode);
				});
			}
			catch (Exception e)
			{
				log.info("Fail to redis del:" + key + ", " + e);
			}
		}
	}

	public void setJedis(JedisTemplate jedis)
	{
		this.jedis = jedis;
	}

	public void setSessionIdInCookieName(String sessionIdInCookieName)
	{
		this.sessionIdInCookieName = sessionIdInCookieName;
	}

	public void setCookieExpire(int cookieExpire)
	{
		this.cookieExpire = cookieExpire;
	}

	public void setSessionMode(int sessionMode)
	{
		this.sessionMode = sessionMode;
	}

	public void setRedisKeyPrefix(String redisKeyPrefix)
	{
		this.redisKeyPrefix = redisKeyPrefix;
	}

	public void setSessionLen(int sessionLen)
	{
		this.sessionLen = sessionLen;
	}
}
