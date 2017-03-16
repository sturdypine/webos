package spc.webos.web.common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import spc.webos.web.filter.multipart.MultipartEntry;

/**
 * @author spc
 */
public interface SUI
{
	void clear(); // 清楚当前用户信息

	boolean isEmpty(); // 是否为空信息

	int tps();

	String getSessionId();

	void removeInPersist(String key);

	void setInPersist(String key, Serializable value);

	<T> T getInPersist(String key, T def);

	void removeInTransient(String key);

	// void setInTransient(String key, TransientInSession<?> tis);

	<T> void setInTransient(String key, T value, int cacheSeconds);

	<T> T getInTransient(String key, T def);

	// 触发一次request请求，用于内部计数， 也可以统计当前用户的请求次数
	void request(HttpServletRequest request, HttpServletResponse response, String sessionId,
			HttpSession session);

	int requestCount(); // 获得当前请求计数

	// 上传文件
	Map<String, List<MultipartEntry>> upload();

	/**
	 * 把当前sessionID保存到SessionUserInfo中，在web remote, web service中的权限检查时使用此属性，
	 * 因为通过客户端调用login(user,pwd)后返回一个sessionID供以后的http调用会话使用。
	 * 
	 */
	// 2016-12-12 不容许开发人员直接操作session
	HttpSession session();

	HttpServletRequest request();

	HttpServletResponse response();

	void setVerifyCode(String verifyCode);

	String getVerifyCode();

	// 用户相关信息
	void setLoginIP(String loginIP);

	String getLoginIP(); // 登陆的IP

	Date getLastVisitDt(); // 获取上一次访问服务器时间

	void setLastVisitDt(Date lastVisitDt);

	void setLoginDt(Date loginDt);

	Date getLoginDt(); // 登陆时间

	/**
	 * 获得用户代码
	 * 
	 * @return
	 */
	String getUserCode();

	// 当前用户是否session用户
	boolean rejectUserCode(String userCode);

	/**
	 * 获得用户代码
	 * 
	 * @return
	 */
	String getUserName();

	// 当前用户是否session用户
	boolean rejectUserName(String userName);

	/**
	 * 支持URLSecurityFilter, 用户访问权限信息
	 * 
	 * @return
	 */
	boolean containRole(String role);

	List<String> getRoles();

	// 是否有执行此sqlid的权限
	boolean containSqlId(String sqlId, Map<String, Object> param);

	// 是否有执行此服务的权限
	boolean containService(String s);

	/**
	 * 删除Session中临时存储信息的接口, 在URLSecurityFilter中调用
	 */
	void removeExpiredTransient();

	/**
	 * GetMethodSecurityFilter调用, 用户获得当前Session中的对URL加密的Key. 每个登陆的用户获得一个随机的Key
	 * 
	 * @return
	 */
	String sget(String queryString);

	boolean isSGet(String queryString);

	/**
	 * 一个TheadLocal对象用来存放SessionUserInfo
	 */
	final static ThreadLocal<SUI> SUI = new ThreadLocal<SUI>();

	/**
	 * 在Session中存放用户信息的名称
	 */
	final static String USER_SESSION_INFO_KEY = "_SUI_";

	// final static String ALL_ONLINE_USER_KEY = "_ALL_ONLINE_USER_KEY_";
}
