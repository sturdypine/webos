package spc.webos.web.filter.encode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

public class RequestProxy implements InvocationHandler
{
	HttpServletRequest request = null;
	String srcEncode;
	String targetEncode;

	public RequestProxy(HttpServletRequest request, String srcEncode,
			String targetEncode)
	{
		this.request = request;
		this.srcEncode = srcEncode;
		this.targetEncode = targetEncode;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{
		String methodName = method.getName();
		if (methodName.equals("getParameter"))
		{
			Object value = encoding(request.getParameter((String) args[0]));
			return value;
		}
		else if (methodName.equals("getParameterValues"))
		{
			String[] v = request.getParameterValues((String) args[0]);
			if (v == null) return null;
			String[] value = new String[v.length];
			for (int i = 0; i < v.length; i++)
			{
				value[i] = encoding(v[i]);
			}
			return value;
		}
		return forwardInvocation(method, args);
	}

	String encoding(String value) throws Throwable
	{
		if (value == null || value.length() == 0) return value;
		return new String(value.toString().getBytes(this.srcEncode),
				this.targetEncode);
	}

	private Object forwardInvocation(Method method, Object[] args)
			throws NoSuchMethodException
	{
		Method targetMethod = this.request.getClass().getMethod(
				method.getName(), method.getParameterTypes());
		try
		{
			return targetMethod.invoke(this.request, args);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("Error forwarding method call "
					+ method.getName(), e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException("Error forwarding method call "
					+ method.getName(), e);
		}
	}
}
