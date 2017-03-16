package spc.webos.persistence.jdbc.datasource;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 根据拦截包的路径，固定设置某些包下的服务使用某个数据源
 * 
 * @author chenjs
 *
 */
public class DSDynamicDSAdvice extends DynamicDSAdvice
{
	public Object routing(ProceedingJoinPoint pjp) throws Throwable
	{ // RulesDynamicDSAdvice 可能优先于DDDSA执行
		// System.out.print("DefaultDynamicDSAdvice:"+jp.getSignature());
		// 939_20170302 如果当前接口配置了DataSource则不拦截设置数据源，而是以注解为准
		Method method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(),
				((MethodSignature) pjp.getSignature()).getParameterTypes());
		if (method.getAnnotation(DataSource.class) != null)
		{
			log.debug("DataSource exists...");
			return pjp.proceed();
		}

		return set(ds, pjp);
	}

	String ds;

	public void setDs(String ds)
	{
		this.ds = ds;
	}
}
