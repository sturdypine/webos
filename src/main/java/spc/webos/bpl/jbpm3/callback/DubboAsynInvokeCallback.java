package spc.webos.bpl.jbpm3.callback;

import com.alibaba.dubbo.rpc.IResponseCallback;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;

import spc.webos.bpl.jbpm3.IFlowEndCallback;
import spc.webos.tcc.TccAdvice;

public class DubboAsynInvokeCallback extends AsynInvokeCallback implements IResponseCallback
{
	public DubboAsynInvokeCallback(IFlowEndCallback callback)
	{
		this.callback = callback;
	}

	public void caught(Invoker<?> arg0, Invocation arg1, Throwable t)
	{
		if (log.isInfoEnabled()) log.info("dubbo asyn call fail::" + instance.getId() + ":"
				+ instance.getProcessDefinition().getName() + " ## " + t);
		if (transaction != null) TccAdvice.startTransaction(transaction);
		endFlow(null, t);
	}

	public void done(Invoker<?> arg0, Invocation arg1, Result result)
	{
		if (log.isInfoEnabled()) log.info("dubbo asyn call done::" + instance.getId() + ":"
				+ instance.getProcessDefinition().getName() + ", ex:" + result.hasException());
		afterInvoke(result.getValue(), result.getException());
	}
}
