package spc.webos.bpl.mule.callable;

import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

import spc.webos.bpl.AbstractSpringServiceCall;
import spc.webos.util.StringX;

public class SpringServiceCallable extends AbstractSpringServiceCall implements Callable
{
	public Object onCall(MuleEventContext cxt) throws Exception
	{
		MuleMessage msg = cxt.getMessage();
		Map payload = (Map) msg.getPayload();
		if (!StringX.nullity(javaPreFn)) preFn(cxt);
		Object[] args = createArgs(cxt, payload);
		Object ret = null;
		Exception ex = null;
		if (log.isInfoEnabled()) log.info("call: " + method);
		try
		{
			ret = invoke(cxt, args, false);
		}
		catch (Exception e)
		{
			ex = e;
		}
		afterInvoke(cxt, ret, ex);
		return msg;
	}

	public void afterInvoke(MuleEventContext cxt, Object ret, Throwable ex) throws Exception
	{
		doInvokeEx(cxt, ex);
		postFn(cxt, ret);
		choice(cxt, ret, ex);
	}

	protected void choice(MuleEventContext cxt, Object ret, Throwable ex) throws Exception
	{
		String choice = decide(cxt, (Map) cxt.getMessage().getPayload(), ret, ex);
		if (log.isInfoEnabled()) log.info(method + "'s choice: " + choiceKey + "=" + choice);
		if (choice == null) ((Map) cxt.getMessage().getPayload()).remove(choiceKey);
		else((Map) cxt.getMessage().getPayload()).put(choiceKey, choice);
	}

	protected void setVariable(Object cxt, String name, Object ret)
	{
		MuleEventContext mule = (MuleEventContext) cxt;
		((Map) mule.getMessage().getPayload()).put(name, ret);
	}

	protected void setVariables(Object cxt, Map ret)
	{
		MuleEventContext mule = (MuleEventContext) cxt;
		((Map) mule.getMessage().getPayload()).putAll(ret);
	}

	protected String choiceKey = "_choice";

	public void setChoiceKey(String choiceKey)
	{
		this.choiceKey = choiceKey;
	}
}
