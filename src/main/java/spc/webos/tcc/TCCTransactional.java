package spc.webos.tcc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解使用在*Service.tcc*, *Service.atcc*方法上 设置TCC事务特性
 * 
 * @author chenjs
 *
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TCCTransactional
{
	// 事务状态存储机制
	String value() default "";

	// 是否异步tcc事务处理
	boolean asyn() default false;

	// 事务执行完后，正常执行(正常confirm all，正常cancel all)事务是否保留
	boolean retention() default false;

	// for TCC原子服务注解
	boolean doTry() default true;

	boolean doConfirm() default true;
	
	boolean tranlog() default true; // 使用原子TCC服务需要tcc_tranlog表支持
	
	/**
	 * *TCCService.XXX or *Service.tryXXX方法中某一个参数的某个路径是当前服务的一个唯一编号。
	 * 此编号作为事务状态关联信息进行存储(相当于外部唯一流水号 和 内部事务流水号关联)
	 * 
	 * @author chenjs
	 *
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface XidParamPath
	{
		String value() default ""; // Xid参数的具体路径属性
	}
}
