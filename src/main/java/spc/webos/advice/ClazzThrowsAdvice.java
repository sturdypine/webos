package spc.webos.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.exception.AppException;
import spc.webos.util.POJOUtil;
import spc.webos.util.StringX;

public class ClazzThrowsAdvice
{
	private Logger log = LoggerFactory.getLogger(getClass());
	String clazz; // 异常类
	String filter;
	boolean initCause = true;
	boolean setStackTrace = true;
	String[] properties = new String[] { "code", "desc" };
	String[] defValues; //

	public Object throwing(ProceedingJoinPoint pjp) throws Throwable
	{
		try
		{
			return pjp.proceed();
		}
		catch (AppException ae)
		{
			throw ae;
		}
		catch (Throwable t)
		{
			log.info("undefined throwable", t);
			// 如果是指定可以直接throw的异常则直接throw
			if (!StringX.nullity(filter) && t.getClass().getName().startsWith(filter)) throw t;
			RuntimeException re = (RuntimeException) Class
					.forName(clazz, true, Thread.currentThread().getContextClassLoader())
					.newInstance();
			if (defValues != null) POJOUtil.setPropertyValue(re, properties, defValues);
			if (initCause) re.initCause(t);
			if (setStackTrace) re.setStackTrace(t.getStackTrace());
			throw re;
		}
	}

	public boolean isInitCause()
	{
		return initCause;
	}

	public void setInitCause(boolean initCause)
	{
		this.initCause = initCause;
	}

	public boolean isSetStackTrace()
	{
		return setStackTrace;
	}

	public void setSetStackTrace(boolean setStackTrace)
	{
		this.setStackTrace = setStackTrace;
	}

	public String getClazz()
	{
		return clazz;
	}

	public void setClazz(String clazz)
	{
		this.clazz = clazz;
	}

	public String getFilter()
	{
		return filter;
	}

	public void setFilter(String filter)
	{
		this.filter = filter;
	}

	public String[] getProperties()
	{
		return properties;
	}

	public void setProperties(String[] properties)
	{
		this.properties = properties;
	}

	public String[] getDefValues()
	{
		return defValues;
	}

	public void setDefValues(String[] defValues)
	{
		this.defValues = defValues;
	}
}
