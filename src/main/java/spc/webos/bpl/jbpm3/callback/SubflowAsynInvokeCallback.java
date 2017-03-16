package spc.webos.bpl.jbpm3.callback;

import org.jbpm.graph.exe.ProcessInstance;

import spc.webos.bpl.jbpm3.IFlowEndCallback;

public class SubflowAsynInvokeCallback extends AsynInvokeCallback implements IFlowEndCallback
{
	public SubflowAsynInvokeCallback(IFlowEndCallback callback)
	{
		this.callback = callback;
	}

	public void end(ProcessInstance instance, Throwable t)
	{
		afterInvoke(instance.getContextInstance().getVariables(), t);
	}
}
