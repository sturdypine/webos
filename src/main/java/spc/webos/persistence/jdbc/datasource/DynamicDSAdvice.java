package spc.webos.persistence.jdbc.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.persistence.IPersistence;
import spc.webos.util.StringX;

public class DynamicDSAdvice
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	protected Object jt(String jt, ProceedingJoinPoint pjp) throws Throwable
	{
		String old = IPersistence.CURRENT_JT.get();
		boolean set = (force || old == null) && !StringX.nullity(jt) && !jt.equals(old);
		if (set)
		{
			log.info("JT routing:{}, old:{}, force:{}", jt, old, force);
			IPersistence.CURRENT_JT.set(jt);
		}
		try
		{
			return pjp.proceed();
		}
		finally
		{
			if (set)
			{
				IPersistence.CURRENT_JT.set(old);
				log.info("JT remove:{}, old:{}, force:{}", jt, old, force);
			}
		}
	}

	protected Object routing(String ds, ProceedingJoinPoint jp) throws Throwable
	{
		if (DynamicDataSource.current() != null && !StringX.nullity(ds)
				&& !DynamicDataSource.current().equals(ds))
		{ // 当前线程环境已经做过数据源切换，如果再切换不同数据源则报异常，因为不支持多数据源事务
			throw new AppException(AppRetCode.DB_MULTI_CHANGE_DS,
					new Object[] { ((MethodSignature) jp.getSignature()).getMethod().getName(),
							DynamicDataSource.current(), ds });
		}
		return set(ds, jp);
	}

	protected Object set(String ds, ProceedingJoinPoint jp) throws Throwable
	{
		// 因为服务可能嵌套调用，考虑到事务原因，设置数据源只能是一次
		// 如果当前线程环境已经设置了数据源，以后的服务调用不能再设置，并且谁设置谁负责清空线程环境
		String oldJT = IPersistence.CURRENT_JT.get(); // 切换DS时，需要取消现有JT路由设置。
		String oldDS = DynamicDataSource.current();
		boolean set = (force || oldDS == null) && !StringX.nullity(ds) && !ds.equals(oldDS);
		if (set)
		{
			DynamicDataSource.current(ds);
			IPersistence.CURRENT_JT.set(null);
			log.info("DS routing:{}, oldDS:{}, oldJT:{}, force:{}", ds, oldDS, oldJT, force);
		}
		else log.debug("NO DS routing:{}, old:{}, force:{}", ds, oldDS, force);
		try
		{
			return jp.proceed();
		}
		finally
		{
			if (set)
			{
				log.info("DS remove:{}, oldDS:{}, oldJT:{}", DynamicDataSource.current(), oldDS,
						oldJT);
				DynamicDataSource.current(oldDS); // 清空当前线程环境的数据源设置
				IPersistence.CURRENT_JT.set(oldJT);
			}
		}
	}

	protected boolean force; // 强制切换到当前数据源, 主要针对某些服务是独立事务(数据库流水号申请)

	public void setForce(boolean force)
	{
		this.force = force;
	}
}
