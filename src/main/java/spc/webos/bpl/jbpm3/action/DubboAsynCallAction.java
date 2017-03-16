package spc.webos.bpl.jbpm3.action;

import org.jbpm.graph.exe.ExecutionContext;

import com.alibaba.dubbo.rpc.RpcContext;

import spc.webos.bpl.jbpm3.JBPM3Engine;
import spc.webos.bpl.jbpm3.callback.DubboAsynInvokeCallback;

public class DubboAsynCallAction extends SpringServiceCallAction
{
	private static final long serialVersionUID = 1L;

	protected Object invoke(ExecutionContext ec, Object[] args, boolean isAsynCall) throws Exception
	{
		if (isAsynCall)
		{
			DubboAsynInvokeCallback callback = (DubboAsynInvokeCallback) ec.getProcessInstance()
					.getContextInstance().getTransientVariable(JBPM3Engine.DUBBO_ASYN_INVOKE);
			callback.setAfterAsynCall(this);
			callback.setInstance(ec.getProcessInstance());
			callback.setEc(ec);
			RpcContext.getContext().asynResponseCallback = callback;
		}
		return super.invoke(ec, args, isAsynCall);
	}
}
