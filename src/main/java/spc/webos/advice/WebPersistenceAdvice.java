package spc.webos.advice;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.persistence.SQLItem;
import spc.webos.web.common.SUI;
import spc.webos.web.util.WebUtil;

/**
 * check SQL Auth in Web Context
 * 
 * @author chenjs
 *
 */
public class WebPersistenceAdvice
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	public Object isAuth(ProceedingJoinPoint jp) throws Throwable
	{
		boolean ret = (Boolean) jp.proceed();
		if (!ret) return false;
		Object[] args = jp.getArgs();
		SQLItem item = (SQLItem) args[1];
		log.debug("auth sql:{}, auth:{}", args[0], item.auth);
		if (item.auth == SQLItem.AUTH_public) return true;
		if (item.auth == SQLItem.AUTH_login && SUI.SUI.get() != null) return true;
		return WebUtil.containSqlId((String) args[0], (Map) args[2]);
	}
}
