package spc.webos.bpl.jbpm3.callback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.jbpm.graph.exe.ProcessInstance;

import spc.webos.util.POJOUtil;

public class CallbackFlowEndCallback extends AbstractFlowEndCallback
{
	protected Object pojo;
	protected Object callback;
	protected String method;

	public CallbackFlowEndCallback(Object callback, String method, Object pojo,
			String[][] properties, String corId, String refExt)
	{
		super(corId, properties, refExt);
		this.callback = callback;
		this.method = method;
		this.pojo = pojo;
	}

	public void end(ProcessInstance instance, Throwable t)
	{
		Object retValue = getRetValue(instance, t);
		try
		{
			if (log.isDebugEnabled())
				log.debug("callback corId:" + corId + ", retValue:" + retValue + ",t:" + t);
			Method method = callback.getClass().getMethod(this.method, new Class[] { String.class,
					pojo != null ? pojo.getClass() : Map.class, String.class, Map.class });
			method.invoke(callback, new Object[] { corId, retValue, refExt, ex2map(t) });
		}
		catch (InvocationTargetException e)
		{
			log.warn("fail to callback:" + instance.getId() + "," + corId + ","
					+ callback.getClass(), e.getTargetException());
		}
		catch (Exception e)
		{
			log.warn("fail to callback:" + instance.getId() + "," + corId + ","
					+ callback.getClass(), e);
		}
	}

	protected Object getRetValue(ProcessInstance instance, Throwable t)
	{
		if (t != null) return null;
		if (pojo == null)
			return POJOUtil.map2map(instance.getContextInstance().getVariables(), properties);
		return POJOUtil.map2pojo(
				POJOUtil.map2map(instance.getContextInstance().getVariables(), properties), pojo);
	}
}
