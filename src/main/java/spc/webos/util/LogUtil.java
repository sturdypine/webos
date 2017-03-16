package spc.webos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于日志唯一流水号追踪. 修改ch.qos.logback.classic.spi.LoggingEvent,
 * 增加threadlocal信息，用于存放唯一跟踪号，参看eslf4j项目
 * 
 * @author chenjs
 *
 */
public class LogUtil
{
	protected static Logger log = LoggerFactory.getLogger(LogUtil.class);
	public static String MDC_KEY_TRACE = "trace"; // 日志跟踪信息
	public static String MDC_KEY_LOCATION = "location"; // 发起日志跟踪的附加位置信息
	public static String MDC_APP_CD = "appCd"; // 发起的系统编号
	static ThreadLocal<String> LOG_TRACE_NO = new ThreadLocal<String>();
	static ThreadLocal<String> LOG_LOCATION = new ThreadLocal<String>();
	static ThreadLocal<String> LOG_APP_CD = new ThreadLocal<String>();
	public static int MAX_LOCATION_LEN = 20;

	public static String shortUri(String uri)
	{
		if (uri.length() <= MAX_LOCATION_LEN) return uri;
		return uri.substring(0, MAX_LOCATION_LEN) + "..";
	}

	public static String getTraceNo()
	{
		return LOG_TRACE_NO.get();
	}

	public static String getAppCd()
	{
		return LOG_APP_CD.get();
	}

	public static void setAppCd(String appCd)
	{
		LOG_APP_CD.set(appCd);
	}

	/**
	 * 
	 * @param traceNo
	 *            日志跟踪号
	 * @param replace
	 *            如果当前环境已经存在是否覆盖
	 * @return
	 */
	public static boolean setTraceNo(String traceNo, String location, boolean replace)
	{
		if (traceNo == null || traceNo.length() == 0) return false;
		// 如果已经存在traceNo, 则不覆盖
		if (!replace && getTraceNo() != null) return false;

		// 否则覆盖
		LOG_TRACE_NO.set(traceNo);
		LOG_LOCATION.set(location);

		// 设置Log MDC 环境
		putMDC(MDC_KEY_TRACE, traceNo);
		putMDC(MDC_KEY_LOCATION, location);

		log.debug("set trace:{}", traceNo);
		return true;
	}

	public static void putMDC(String key, String value)
	{
		org.slf4j.MDC.put(key, value);
	}

	public static void removeTraceNo()
	{
		log.debug("remove trace:{}", getTraceNo());
		LOG_TRACE_NO.set(null);
		LOG_LOCATION.set(null);

		// 取消Log MDC 环境
		removeMDC(MDC_KEY_TRACE);
		removeMDC(MDC_KEY_LOCATION);
	}

	public static void removeMDC(String key)
	{
		org.slf4j.MDC.remove(key);
	}
}
