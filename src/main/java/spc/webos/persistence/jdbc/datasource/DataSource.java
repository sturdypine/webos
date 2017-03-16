package spc.webos.persistence.jdbc.datasource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 容许每个方法动态指定数据源
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DataSource
{
	String value() default ""; // 默认数据源名称

	String ds() default ""; // 默认数据源名称

	String dsPostfix() default "DS"; // ds spring bean id后缀

	String jtPostfix() default "JT"; // ds spring bean id后缀

	boolean jt() default false; // 决定ds()值得类型是datasource还是jdbctemplate, 默认是ds

	// 如果是基于参数内容动态决定，则需要提供规则编号, 由统一的拦截中心，根据规则编号，以及参数动态提供的值计算出数据源
	String rule() default "";

	// rule配置存在时，是否可以参数不存在
	boolean canNull() default false;

	/**
	 * 由具体参数内容动态决定数据源，参数路径用.分隔
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface ColumnPath
	{
		String value() default ""; // 参数的具体路径属性
	}
}
