package spc.webos.bpl.mule.callable;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

public class ExceptionHandlerCallable implements Callable
{
	public Object onCall(MuleEventContext cxt) throws Exception
	{
		System.out.println("ex...");
		return cxt.getMessage();
	}
}
