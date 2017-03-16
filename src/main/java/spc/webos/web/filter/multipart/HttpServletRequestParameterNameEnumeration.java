package spc.webos.web.filter.multipart;

import java.util.Enumeration;
import java.util.Iterator;

public class HttpServletRequestParameterNameEnumeration implements Enumeration
{
	Iterator parameterNames = null;

	public HttpServletRequestParameterNameEnumeration(Iterator parameterNames)
	{
		this.parameterNames = parameterNames;
	}

	public boolean hasMoreElements()
	{
		return this.parameterNames.hasNext();
	}

	public Object nextElement()
	{
		Object nextParameterName = this.parameterNames.next();
		return nextParameterName;
	}
}
