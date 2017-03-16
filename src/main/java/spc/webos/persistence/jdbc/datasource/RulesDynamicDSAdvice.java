package spc.webos.persistence.jdbc.datasource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanWrapperImpl;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.persistence.jdbc.datasource.DataSource.ColumnPath;
import spc.webos.util.StringX;

/**
 * 根据方法以及方法参数的注解来动态计算数据源
 * 
 * @author chenjs
 *
 */
public class RulesDynamicDSAdvice extends DynamicDSAdvice
{
	public Object routing(ProceedingJoinPoint pjp) throws Throwable
	{
		Method method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(),
				((MethodSignature) pjp.getSignature()).getParameterTypes());
		DataSource ds = method.getAnnotation(DataSource.class);
		if (ds == null) return pjp.proceed();

		String dsName = null;
		// 1.如果DataSource没有设置rule规则，则不能使用参数注解模式，只能是默认模式
		if (StringX.nullity(ds.rule())) dsName = ds.value();
		else
		{
			Object[] dsParamAndArg = findDSParamAndArg(method.getParameterAnnotations(),
					pjp.getArgs());
			if (dsParamAndArg == null) dsName = ds.value(); // 使用默认数据源
			else dsName = ds(ds, (ColumnPath) dsParamAndArg[0], dsParamAndArg[1]);
		}
		if (ds.jt()) return jt(dsName + ds.jtPostfix(), pjp);
		return routing(dsName + ds.dsPostfix(), pjp);
	}

	protected String ds(DataSource ds, ColumnPath dsParam, Object arg) throws Exception
	{
		Object ruleArg = getRuleArg(dsParam, arg);
		if (!ds.canNull() && ruleArg == null) throw new AppException(AppRetCode.DS_ARG_NULL);
		String dsName = ds(ds.rule(), ruleArg);
		log.info("DS rule:{}, column:{}, ds:{}", ds.rule(), ruleArg, dsName);
		return dsName;
	}

	// 通过规则名，当前动态参数获取一个数据源
	protected String ds(String rule, Object arg) throws Exception
	{
		RoutingFunction fun = rules.get(rule.toLowerCase()); // 规则名大小写不敏感
		if (fun == null) throw new AppException(AppRetCode.DS_RULE_NULL, new Object[] { rule });
		return fun.routing(arg);
	}

	// 根据注解路径获取规则参数
	protected Object getRuleArg(ColumnPath path, Object arg)
	{
		if (StringX.nullity(path.value()) || arg == null) return arg; // 如果注解路径没有则直接使用此参数
		BeanWrapperImpl wrapper = new BeanWrapperImpl(false);
		wrapper.setWrappedInstance(arg);
		Object column = wrapper.getPropertyValue(path.value());
		log.debug("DS path:{}, column:{}, arg:{}", path.value(), column, arg);
		return column;
	}

	// 找到第一个注解DSParam的参数注解信息，以及当前参数值
	protected Object[] findDSParamAndArg(Annotation[][] annotations, Object[] args)
	{
		if (annotations == null) return null;
		for (int i = 0; i < args.length; i++)
			for (int j = 0; annotations[i] != null && j < annotations[i].length; j++)
				if (annotations[i][j] instanceof ColumnPath)
					return new Object[] { annotations[i][j], args[i] };
		return null;
	}

	protected Map<String, RoutingFunction> rules = new HashMap<String, RoutingFunction>();

	public void setRules(Map<String, Object> rules) throws Exception
	{
		for (String key : rules.keySet())
		{
			Object obj = rules.get(key); // 规则名大小写不敏感
			if (obj instanceof RoutingFunction)
				this.rules.put(key.toLowerCase(), (RoutingFunction) obj);
			else this.rules.put(key.toLowerCase(), new JSRoutingFunction(obj.toString()));
		}
	}
}
