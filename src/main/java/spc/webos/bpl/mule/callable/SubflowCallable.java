package spc.webos.bpl.mule.callable;

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;

import spc.webos.util.StringX;

public class SubflowCallable extends SpringServiceCallable
{
	public SubflowCallable()
	{
		this.inArgClass = "map";
	}

	protected Object invoke(Object cxt, Object[] args, boolean isAsynCall) throws Exception
	{
		MuleEventContext mule = (MuleEventContext) cxt;
		Map payload = (Map) mule.getMessage().getPayload();
		String subflow = method; // vm://xxx
		if (StringX.nullity(subflow)) subflow = (String) payload.get(SUB_FLOW_KEY);
		if (log.isInfoEnabled()) log.info("call subflow: " + subflow);

		MuleClient client = mule.getMuleContext().getClient();
		MuleMessage result = client.send(subflow,
				new DefaultMuleMessage(args[0], (Map<String, Object>) null, mule.getMuleContext()));
		return result.getPayload();
	}
}
