package spc.webos.bpl.jbpm3.action;

import java.util.Map;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.State;

import spc.webos.bpl.AbstractSpringServiceCall;
import spc.webos.bpl.jbpm3.IAfterAsynCall;
import spc.webos.util.StringX;

public class SpringServiceCallAction extends AbstractSpringServiceCall
		implements ActionHandler, IAfterAsynCall
{
	private static final long serialVersionUID = 1L;

	public void execute(ExecutionContext ec) throws Exception
	{
		boolean isAsynCall = isAsynCall(ec);
		if (!StringX.nullity(javaPreFn)) preFn(ec);
		Object[] args = createArgs(ec, ec.getContextInstance().getVariables());
		Object ret = null;
		Exception ex = null;
		if (log.isInfoEnabled())
			log.info(ec.getNode().getName() + (isAsynCall ? " asyn: " : " syn: ") + method);
		try
		{
			ret = invoke(ec, args, isAsynCall);
		}
		catch (Exception e)
		{
			ex = e;
		}
		if (!isAsynCall) afterInvoke(ec, ret, ex);
	}

	public void afterInvoke(ExecutionContext ec, Object ret, Throwable ex) throws Exception
	{
		doInvokeEx(ec, ex);
		postFn(ec, ret);
		transition(ec, ret, ex);
	}

	public void transition(ExecutionContext ec, Object ret, Throwable ex) throws Exception
	{
		String transition = decide(ec, ec.getContextInstance().getVariables(), ret, ex);
		if (log.isInfoEnabled()) log.info(ec.getNode().getName() + "'s transition:" + transition);
		if (!StringX.nullity(transition)) ec.leaveNode(transition);
		else ec.leaveNode();
	}

	public boolean isAsynCall(ExecutionContext ec)
	{
		return ec.getNode() instanceof State;
	}

	protected void setVariable(Object cxt, String name, Object ret)
	{
		ExecutionContext ec = (ExecutionContext) cxt;
		ec.getContextInstance().setVariable(name, ret);
	}

	protected void setVariables(Object cxt, Map ret)
	{
		ExecutionContext ec = (ExecutionContext) cxt;
		ec.getContextInstance().setVariables(ret);
	}
}
