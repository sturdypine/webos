package spc.webos.bpl.jbpm3.callback;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.bpl.jbpm3.IAfterAsynCall;
import spc.webos.bpl.jbpm3.IFlowEndCallback;
import spc.webos.tcc.TccAdvice;
import spc.webos.tcc.Transaction;

public class AsynInvokeCallback
{
	protected ProcessInstance instance;
	protected ExecutionContext ec;
	protected IAfterAsynCall afterAsynCall;
	protected IFlowEndCallback callback;
	protected Transaction transaction = TccAdvice.currentTransaction();
	protected TccAdvice advice = TccAdvice.CUR_ADVICE.get();
	protected transient Logger log = LoggerFactory.getLogger(getClass());

	public void afterInvoke(Object result, Throwable t)
	{
		if (transaction != null) TccAdvice.startTransaction(transaction);
		try
		{
			afterAsynCall.afterInvoke(ec, result, t);
		}
		catch (Exception e)
		{
			endFlow(result, e);
			return;
		}
		if (instance.hasEnded())
		{
			if (log.isInfoEnabled()) log.info("asyn flow end:" + instance.getId() + ":"
					+ instance.getProcessDefinition().getName());
			endFlow(result, null);
		}
	}

	protected void endFlow(Object result, Throwable t)
	{
		try
		{
			if (transaction != null)
			{
				if (t == null) advice.confirm(transaction);
				else advice.cancel(transaction, t);
			}
			callback.end(instance, t);
		}
		catch (Exception e)
		{
			log.warn("Flow AsynTX fail to commit", e);
			callback.end(instance, t == null ? e : t);
		}
		finally
		{
			TccAdvice.removeTransaction();
		}
	}

	public ProcessInstance getInstance()
	{
		return instance;
	}

	public void setInstance(ProcessInstance instance)
	{
		this.instance = instance;
	}

	public ExecutionContext getEc()
	{
		return ec;
	}

	public void setEc(ExecutionContext ec)
	{
		this.ec = ec;
	}

	public IAfterAsynCall getAfterAsynCall()
	{
		return afterAsynCall;
	}

	public void setAfterAsynCall(IAfterAsynCall afterAsynCall)
	{
		this.afterAsynCall = afterAsynCall;
	}
}
