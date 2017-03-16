package spc.webos.constant;

/**
 * 配置key
 * 
 * @author spc
 * 
 */
public class Config
{
	public final static String APP_WORKERID = "app.workerId";

	public final static String app_pdf_fontpath = "app.pdf.fontpath";

	// 701_20131001 用于日志追踪底层网络发送的二进制内容
//	public final static String TCP_TRACE = "app.trace.tcp";
	public final static String app_trace_tcp = "app.trace.tcp.";
	public final static String app_trace_mq = "app.trace.mq.";

	public static String app_login_default_services = "app.login.default.services"; // 登录时默认服务权限
	public static String app_login_default_sqls = "app.login.default.sqls"; // 登录时默认sql权限
	public static String app_login_pwd_salt_len = "app.login.pwd.salt.len"; // 密码盐长度
	public static String app_login_verify = "app.login.verify"; // 登录时是否必须提供验证码
	public static String app_login_verify_len = "app.login.verify.len"; // 验证码长度

	// 非登录模式，公共授权部分
	public static String app_web_auth_public_service = "app.web.auth.public.service"; // 公共授权服务
	public static String app_web_auth_login_service = "app.web.auth.login.service"; // 登录后授权服务
	public static String app_web_auth_public_sql = "app.web.auth.public.sql"; // 公共授权SQL
	public static String app_web_auth_login_sql = "app.web.auth.login.sql"; // 登录后授权SQL

	public static String app_mq_msg_expire = "app.mq.msg.expire"; // mq消息的失效时间，防止收到过期消息
	public static String app_mq_cb_expire = "app.mq.cb.expire"; // 回调函数在cache存活时间

	public static String app_config_repeatInterval = "app.config.repeatInterval";

	public static String tcc_repeatSeconds = "tcc.repeatSeconds";
	public static String tcc_batchSize = "tcc.batchSize";

	// session
	// token Des key
	public static String app_web_session_token_des = "app.web.session.token.des";
	// 最大session不活跃时间为半小时
	public static String app_web_session_maxInactive = "app.web.session.maxInactive";
	public static String app_web_session_tps = "app.web.session.tps"; // 每10秒的最高访问量
	public static String app_web_cache_disable = "app.web.cache.disable"; // 前端页面缓存失效
	public static String app_web_token_expire = "app.web.token.expire"; // token失效时间秒
}
