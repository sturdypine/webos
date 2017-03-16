package spc.webos.advice.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存方法返回值
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MethodCache
{
	String value() default ""; // key值

	boolean read() default true; // 读缓存

	boolean write() default false; // 写缓存

	String delim() default "|"; // 多keys分隔线

	int expire() default 24 * 3600; // 缓存有效时间, 默认24小时

	boolean md5() default false; // key是否需要md5

	/**
	 * 方法中某一个参数的某个路径是当前缓存key组成部分
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface MCPath
	{
		String value() default ""; // 参数的具体路径属性

		String nullValue() default ""; // 如果参数为null则默认值
	}
}
