package spc.webos.bpl.mule.callable;

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;

public class MuleESBServiceCallable extends SpringServiceCallable
{
	public MuleESBServiceCallable()
	{
		this.method = "vm://ESBXML";
	}

	protected Object invoke(Object cxt, Object[] args, boolean isAsynCall) throws Exception
	{
		MuleEventContext mule = (MuleEventContext) cxt;
		MuleClient client = mule.getMuleContext().getClient();
		MuleMessage result = client.send(method,
				new DefaultMuleMessage(args[0], (Map<String, Object>) null, mule.getMuleContext()));
		return result.getPayload();
	}
}
