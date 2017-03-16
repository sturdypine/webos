package spc.webos.advice;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;

/**
 * aop验证spring中所有service接口调用时参数合法性
 * 
 * @author chenjs
 *
 */
public class ArgsViolationsAdvice
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	public Object validate(ProceedingJoinPoint jp) throws Throwable
	{
		Method m = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		log.debug("validate args of method:{}", m);
		Set<ConstraintViolation<Object>> violations = validator.forExecutables()
				.validateParameters(jp.getTarget(), m, jp.getArgs());
		for (Object arg : jp.getArgs())
			if (arg != null) violations.addAll(validator.validate(arg));
		if (!violations.isEmpty())
		{
			String errMsg = violations.toString();
			log.info("method({}) args violations:{}", m.toString(), errMsg);
			throw new AppException(AppRetCode.MSG_ERRS, errMsg);
			// throw new ConstraintViolationException(
			// "Failed to validate: " + m + ", cause: " + violations,
			// violations);
		}
		return jp.proceed();
	}
}
