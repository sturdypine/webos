package spc.webos.web.filter.multipart;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class HttpServletRequestProxy implements InvocationHandler
{
	HttpServletRequest request = null;
	Map entries = null;

	public HttpServletRequestProxy(HttpServletRequest request, Map entries)
	{
		this.request = request;
		this.entries = entries;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		String methodName = method.getName();
		if (methodName.equals("getParameter")) return getParameterValue(args[0]);
		else if (methodName.equals("getParameterMap")) return getParameterValueMap();
		else if (methodName.equals("getParameterValues")) return getParameterValues(args[0]);
		else if (methodName.equals("getParameterNames")) return new HttpServletRequestParameterNameEnumeration(
				entries.keySet().iterator());
		return forwardInvocation(method, args);
	}

	private Object getParameterValueMap()
	{
		Map parameterValues = new HashMap();
		Iterator parameterNames = this.entries.keySet().iterator();
		while (parameterNames.hasNext())
		{
			String parameterName = (String) parameterNames.next();
			Object obj = this.entries.get(parameterName);
			if (obj instanceof MultipartEntry)
			{
				MultipartEntry entry = (MultipartEntry) obj;
				if (entry.isParameter())
				{
					parameterValues.put(parameterName, entry.getParameterValue());
				}
				else if (entry.isFile())
				{
					parameterValues.put(parameterName, entry);
				}
			}
			else if (obj instanceof List)
			{
				parameterValues.put(parameterName, getParameterValues(parameterName));
			}
		}

		return parameterValues;
	}

	private Object getParameterValues(Object arg)
	{
		Object entryObj = entries.get(arg);
		if (entryObj instanceof MultipartEntry) { return new String[] { ((MultipartEntry) entryObj)
				.getParameterValue() }; }
		if (entryObj instanceof List)
		{
			List entryList = (List) entries.get(arg);
			String[] parameterValues = new String[entryList.size()];
			Iterator iterator = entryList.iterator();
			int index = 0;
			while (iterator.hasNext())
			{
				MultipartEntry parameterValue = (MultipartEntry) iterator.next();
				parameterValues[index++] = parameterValue.getParameterValue();
			}
			return parameterValues;
		}
		return new String[0];
	}

	private Object getParameterValue(Object parameterKey)
	{
		Object entryObj = entries.get(parameterKey);
		if (entryObj instanceof MultipartEntry)
		{
			MultipartEntry entry = (MultipartEntry) entryObj;
			return entry.getParameterValue();
		}
		else if (entryObj instanceof List)
		{
			MultipartEntry entry = (MultipartEntry) ((List) entryObj).get(0);
			return entry.getParameterValue();
		}

		return null;
	}

	private Object forwardInvocation(Method method, Object[] args) throws NoSuchMethodException
	{
		Method targetMethod = this.request.getClass().getMethod(method.getName(),
				method.getParameterTypes());
		try
		{
			return targetMethod.invoke(this.request, args);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("Error forwarding method call " + method.getName(), e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException("Error forwarding method call " + method.getName(), e);
		}
	}
}
