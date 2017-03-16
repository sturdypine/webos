package spc.webos.advice.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解使用在方法上，表示当前方法需要日志追踪
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogTrace
{
	LogTraceNoType value() default LogTraceNoType.AUTO;

	String location() default "";

	String appCd() default "";

	// 是否强制覆盖当前日志环境
	boolean replace() default false;

	public enum LogTraceNoType
	{
		AUTO, PARAM, REQUEST_USER, RPC
	}

	/**
	 * 方法中某一个参数的某个路径是当前环境需要日志追踪的字段
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface LTPath
	{
		String value() default ""; // 参数的具体路径属性

		String nullValue() default "";
	}
}
