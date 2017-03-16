package spc.webos.tcc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.model.TccTransLogPO;
import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.tcc.TCCTransactional.XidParamPath;
import spc.webos.tcc.service.TccAtomService;
import spc.webos.util.FileUtil;

/**
 * 拦截tcc原子服务impl实现类，辅助完成登记tcc_tranlog表
 * 
 * @author chenjs
 *
 */
public class TccAtomAdvice extends TccAdvice
{
	protected TccAtomService tccAtomService;

	// tcc atom service实现中拦截 tryXXX方法
	public Object doTry(ProceedingJoinPoint jp) throws Throwable
	{
		// 1. 当前方法是否属于原子实现方法
		Method methodImpl = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		TCCTransactional tcc = methodImpl.getAnnotation(TCCTransactional.class);
		if (tcc == null) return jp.proceed(); // 当前try执行可能是rpc客户端，而非impl实现端
		String sn = null; // 事务原子交易的业务流水号信息
		// 通过注解模式，获取当前原子交易的业务流水号，并将此流水号存入TM
		MethodSignature sig = (MethodSignature) jp.getSignature();
		Class<?> declar = sig.getDeclaringType();
		String m = sig.getName();
		String clazz = declar.getCanonicalName();
		Method method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);
		log.info("impl try sn:{}", sn);
		TccTransLogPO po = new TccTransLogPO(sn);
		po.setArgs(new ByteArrayBlob(FileUtil.fst(args)));
		po.setMethod(m);
		po.setClazz(clazz);
		po.setTypes(Arrays.toString(sig.getParameterTypes()));
		po.setTryTm(FastDateFormat.getInstance(Transaction.DF_ALL).format(new Date()));
		if (!tcc.tranlog()) return jp.proceed();
		// 当前原子服务impl需要登记tranlog表
		tccAtomService.doTring(po); // start try
		Object ret = jp.proceed();
		tccAtomService.doTried(new TccTransLogPO(sn)); // finish
		return ret;
	}

	// tcc atom service实现中拦截 confirmXXX方法
	public Object doConfirm(ProceedingJoinPoint jp) throws Throwable
	{
		Object ret = jp.proceed();
		// 1. 当前方法是否属于原子实现方法
		Method method = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		TCCTransactional trans = method.getAnnotation(TCCTransactional.class);
		if (trans == null || !trans.tranlog()) return ret;

		// 需要登记tranlog表
		MethodSignature sig = (MethodSignature) jp.getSignature();
		method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
		String sn = null; // 事务原子交易的业务流水号信息
		// 通过注解模式，获取当前原子交易的业务流水号，并将此流水号存入TM
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);

		log.info("impl confirm sn:{}", sn);
		int rows = tccAtomService.doConfirm(new TccTransLogPO(sn));
		// confirm时找不到原交易
		if (rows <= 0) throw new AppException(AppRetCode.CMM_BIZ_ERR);
		return ret;
	}

	// tcc atom service实现中拦截 cancelXXX方法
	public Object doCancel(ProceedingJoinPoint jp) throws Throwable
	{
		Object ret = jp.proceed();
		// 1. 当前方法是否属于原子实现方法
		Method method = jp.getTarget().getClass().getMethod(jp.getSignature().getName(),
				((MethodSignature) jp.getSignature()).getParameterTypes());
		TCCTransactional trans = method.getAnnotation(TCCTransactional.class);
		if (trans == null || !trans.tranlog()) return ret;

		// 需要登记tranlog表
		MethodSignature sig = (MethodSignature) jp.getSignature();
		method = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes());
		String sn = null; // 事务原子交易的业务流水号信息
		// 通过注解模式，获取当前原子交易的业务流水号，并将此流水号存入TM
		Object[] args = jp.getArgs();
		Object[] tidParamAndArg = findXIDParamAndArg(method.getParameterAnnotations(), args);
		if (tidParamAndArg != null)
			sn = getXid((XidParamPath) tidParamAndArg[0], args, (int) tidParamAndArg[1]);

		log.info("impl cancel sn:{}", sn);
		tccAtomService.doCancel(new TccTransLogPO(sn));
		return ret;
	}

	public void setTccAtomService(TccAtomService tccAtomService)
	{
		this.tccAtomService = tccAtomService;
	}
}
