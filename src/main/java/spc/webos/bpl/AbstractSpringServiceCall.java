package spc.webos.bpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import bsh.Interpreter;
import spc.webos.util.FTLUtil;
import spc.webos.util.POJOUtil;
import spc.webos.util.StringX;
import spc.webos.util.SpringUtil;

public abstract class AbstractSpringServiceCall
{
	protected String ftlDecision;
	protected String javaDecision;
	private transient Interpreter interDecision;

	protected String inDefArgsJson;
	private transient Map inDefArgs;
	protected String inProperties;
	private transient String[] inProps;
	private transient String[][] inSTProps;
	protected String inArgClass;

	protected String inJavaArgs;
	private transient Interpreter interInJavaArgs;

	protected String outArgName = "_ret";
	protected String outProperties;
	private transient String[] outProps;
	private transient String[][] outSTProps;

	protected String javaPreFn;
	private transient Interpreter interPreFn;
	protected String javaPostFn;
	private transient Interpreter interPostFn;

	protected int throwEx = 0; // 0: throw, 1:print, 2:ignore
	protected String parameterTypes;
	private transient Class[] types;
	public String method;
	private transient Method targetMethod;
	private transient Object target;
	protected transient Logger log = LoggerFactory.getLogger(getClass());

	public final static String SUB_FLOW_KEY = "_subflow";

	public Object[] createArgs(Object cxt, Map vars) throws Exception
	{
		// java args
		if (!StringX.nullity(inJavaArgs)) return createJavaArgs(cxt, vars);

		// no args
		if (StringX.nullity(inArgClass)) return null;

		// is a map
		if (inArgClass.equalsIgnoreCase("map")) return new Object[] { getInParams(cxt, vars) };

		// is a pojo
		Object pojo = POJOUtil.map2pojo(getInParams(cxt, vars),
				Class.forName(inArgClass).newInstance());
		if (log.isDebugEnabled())
			log.debug("pojo:" + pojo.getClass() + "::" + new Gson().toJson(pojo));
		return new Object[] { pojo };
	}

	public synchronized Object[] createJavaArgs(Object cxt, Map vars) throws Exception
	{
		if (interInJavaArgs == null)
		{
			interInJavaArgs = new Interpreter();
			interInJavaArgs.eval("Object fun(Object cxt, Object vars){" + inJavaArgs + "}");
		}
		interInJavaArgs.set("cxt", cxt);
		interInJavaArgs.set("vars", vars);
		interInJavaArgs.eval("Object args = fun(cxt,vars);");
		return (Object[]) interInJavaArgs.get("args");
	}

	protected Map getInParams(Object cxt, Map vars)
	{
		if (inDefArgs == null && !StringX.nullity(inDefArgsJson))
			inDefArgs = new Gson().fromJson(inDefArgsJson, HashMap.class);
		Map params = inDefArgs == null ? new HashMap() : new HashMap(inDefArgs);
		if (log.isDebugEnabled()) log.debug("default inParams:" + params);
		if (StringX.nullity(inProperties)) params.putAll(vars);
		else
		{
			if (inProperties.startsWith("["))
			{
				if (inSTProps == null)
					inSTProps = new Gson().fromJson(inProperties, String[][].class);
				for (String[] p : inSTProps)
				{
					Object v = vars.get(p[0]);
					if (v != null)
						params.put((p.length == 1 || StringX.nullity(p[1])) ? p[0] : p[1], v);
				}
			}
			else
			{
				if (inProps == null) inProps = StringX.split(inProperties, ",");
				for (String p : inProps)
				{
					Object v = vars.get(p);
					if (v != null) params.put(p, p);
				}
			}
		}

		if (log.isDebugEnabled()) log.debug("inParams:" + params);
		return params;
	}

	protected Map ret2map(Object ret) throws Exception
	{
		if (!StringX.nullity(outArgName)) return null;
		if (!StringX.nullity(outProperties))
		{
			if (outSTProps == null && outProperties.startsWith("[["))
				outSTProps = new Gson().fromJson(outProperties, String[][].class);
			else if (outProps == null && !outProperties.startsWith("["))
				outProps = StringX.split(outProperties, ",");
			else if (outProps == null)
				outProps = new Gson().fromJson(outProperties, String[].class);
			if (outSTProps != null) return POJOUtil.pojo2map(ret, new HashMap(), outSTProps);
			return POJOUtil.pojo2map(ret, new HashMap(), outProps);
		}
		return POJOUtil.pojo2map(ret, new HashMap());
	}

	public synchronized void preFn(Object cxt) throws Exception
	{
		if (StringX.nullity(javaPreFn)) return;
		if (interPreFn == null)
		{
			interPreFn = new Interpreter();
			interPreFn.eval("String prefn(Object cxt){" + javaPreFn + "}");
		}
		interPreFn.set("cxt", cxt);
		interPreFn.eval("prefn(cxt);");
	}

	public synchronized void javaPostFn(Object cxt, Object ret) throws Exception
	{
		if (StringX.nullity(javaPostFn)) return;
		if (interPostFn == null)
		{
			interPostFn = new Interpreter();
			interPostFn.eval("void postfn(Object cxt,  Object ret){" + javaPostFn + "}");
		}
		interPostFn.set("cxt", cxt);
		interPostFn.set("ret", ret);
		interPostFn.eval("postfn(cxt,ret);");
	}

	protected Object invoke(Object cxt, Object[] args, boolean isAsynCall) throws Exception
	{
		if (StringX.nullity(method)) return null;
		if (targetMethod == null)
		{
			int index = method.indexOf('.');
			String beanId = method.substring(0, index);
			target = SpringUtil.getInstance().getBean(beanId, null);
			if (types == null && !StringX.nullity(parameterTypes))
			{
				String[] strTypes = StringX.split(parameterTypes, ",");
				types = new Class[strTypes.length];
				for (int i = 0; i < strTypes.length; i++)
					types[i] = Class.forName(strTypes[i]);
			}
			targetMethod = findMethod(target, method.substring(index + 1), args, types);
		}
		if (log.isDebugEnabled()) log.debug("invoke:" + method + "::" + targetMethod);
		try
		{
			return targetMethod.invoke(target, args);
		}
		catch (InvocationTargetException e)
		{
			log.warn("fail to invoke:" + targetMethod + "###" + e);
			throw (Exception) e.getTargetException();
		}
	}

	protected void postFn(Object cxt, Object ret) throws Exception
	{
		if (!StringX.nullity(javaPostFn)) javaPostFn(cxt, ret);
		else if (!StringX.nullity(outArgName))
		{
			setVariable(cxt, outArgName, ret);
			if (log.isDebugEnabled()) log.debug("set:" + outArgName + "=" + ret);
		}
		else
		{
			Map m = ret2map(ret);
			setVariables(cxt, m);
			if (log.isDebugEnabled()) log.debug("set M=" + m);
		}
	}

	protected abstract void setVariable(Object cxt, String name, Object ret);

	protected abstract void setVariables(Object cxt, Map ret);

	public void doInvokeEx(Object cxt, Throwable ex) throws Exception
	{
		if (ex == null) return;
		if (throwEx == 0) throw (Exception) ex;
		if (throwEx == 1 && log.isInfoEnabled()) log.info("print ex:" + method, ex);
	}

	public Method findMethod(Object target, String method, Object[] args, Class[] parameterTypes)
			throws SecurityException, NoSuchMethodException
	{
		if (parameterTypes != null) return target.getClass().getMethod(method, parameterTypes);
		Method[] candidates = target.getClass().getMethods();
		for (int i = 0; i < candidates.length; i++)
		{
			Method candidate = candidates[i];
			if (candidate.getName().equals(method))
			{
				Class[] paramTypes = candidate.getParameterTypes();
				if (args == null && paramTypes.length == 0) return candidate;
				if (paramTypes.length == args.length) return candidate;
				// if (args != null && args.length == paramTypes.length)
				// {
				// int j = 0;
				// for (; j < args.length; j++)
				// {
				// if (paramTypes[j] != args[j].getClass()) continue;
				// }
				// if (j >= args.length) return candidate;
				// }
			}
		}
		return null;
	}

	public String decide(Object cxt, Map vars, Object ret, Throwable t) throws Exception
	{
		if (!StringX.nullity(ftlDecision)) return bshDecide(cxt, vars, ret, t);
		if (!StringX.nullity(javaDecision)) return javaDecide(cxt, vars, ret, t);
		return null;
	}

	public String bshDecide(Object cxt, Map vars, Object ret, Throwable t) throws Exception
	{
		if (StringX.nullity(ftlDecision)) return null;
		Map root = FTLUtil.model(new HashMap(vars));
		root.put("_ret", ret);
		String transition = StringX.trim(FTLUtil.freemarker(ftlDecision, root));
		return transition;
	}

	public synchronized String javaDecide(Object cxt, Map vars, Object ret, Throwable t)
			throws Exception
	{
		if (interDecision == null)
		{
			interDecision = new Interpreter();
			interDecision.eval("String fun(Object cxt, Object vars, Object ret, Throwable t){"
					+ javaDecision + "}");
		}
		interDecision.set("cxt", cxt);
		interDecision.set("vars", vars);
		interDecision.set("ret", ret);
		interDecision.set("t", t);
		interDecision.eval("String transition = fun(ec,vars,ret,t);");
		return (String) interDecision.get("transition");
	}

	public String getFtlDecision()
	{
		return ftlDecision;
	}

	public void setFtlDecision(String ftlDecision)
	{
		this.ftlDecision = ftlDecision;
	}

	public String getJavaDecision()
	{
		return javaDecision;
	}

	public void setJavaDecision(String javaDecision)
	{
		this.javaDecision = javaDecision;
	}

	public String getInDefArgsJson()
	{
		return inDefArgsJson;
	}

	public void setInDefArgsJson(String inDefArgsJson)
	{
		this.inDefArgsJson = inDefArgsJson;
	}

	public String getInProperties()
	{
		return inProperties;
	}

	public void setInProperties(String inProperties)
	{
		this.inProperties = inProperties;
	}

	public String getInArgClass()
	{
		return inArgClass;
	}

	public void setInArgClass(String inArgClass)
	{
		this.inArgClass = inArgClass;
	}

	public String getInJavaArgs()
	{
		return inJavaArgs;
	}

	public void setInJavaArgs(String inJavaArgs)
	{
		this.inJavaArgs = inJavaArgs;
	}

	public String getOutArgName()
	{
		return outArgName;
	}

	public void setOutArgName(String outArgName)
	{
		this.outArgName = outArgName;
	}

	public String getOutProperties()
	{
		return outProperties;
	}

	public void setOutProperties(String outProperties)
	{
		this.outProperties = outProperties;
	}

	public String getJavaPreFn()
	{
		return javaPreFn;
	}

	public void setJavaPreFn(String javaPreFn)
	{
		this.javaPreFn = javaPreFn;
	}

	public String getJavaPostFn()
	{
		return javaPostFn;
	}

	public void setJavaPostFn(String javaPostFn)
	{
		this.javaPostFn = javaPostFn;
	}

	public int getThrowEx()
	{
		return throwEx;
	}

	public void setThrowEx(int throwEx)
	{
		this.throwEx = throwEx;
	}

	public String getParameterTypes()
	{
		return parameterTypes;
	}

	public void setParameterTypes(String parameterTypes)
	{
		this.parameterTypes = parameterTypes;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}
}
