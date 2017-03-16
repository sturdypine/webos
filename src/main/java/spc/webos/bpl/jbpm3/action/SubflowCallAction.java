package spc.webos.bpl.jbpm3.action;

import java.util.Map;

import org.jbpm.graph.exe.ExecutionContext;

import spc.webos.bpl.jbpm3.JBPM3Engine;
import spc.webos.bpl.jbpm3.callback.SubflowAsynInvokeCallback;
import spc.webos.util.StringX;

public class SubflowCallAction extends SpringServiceCallAction
{
	private static final long serialVersionUID = 1L;

	public SubflowCallAction()
	{
		this.inArgClass = "map";
	}

	protected Object invoke(ExecutionContext ec, Object[] args, boolean isAsynCall) throws Exception
	{
		String subflow = method;
		if (StringX.nullity(subflow))
			subflow = (String) ec.getContextInstance().getVariable(SUB_FLOW_KEY);
		if (log.isInfoEnabled()) log.info("subflow: " + subflow);
		JBPM3Engine engine = (JBPM3Engine) ec.getContextInstance()
				.getTransientVariable(JBPM3Engine.JBPM3_ENGINE);
		if (!isAsynCall) return engine.call(subflow, (Map) args[0]);

		SubflowAsynInvokeCallback callback = (SubflowAsynInvokeCallback) ec.getProcessInstance()
				.getContextInstance().getTransientVariable(JBPM3Engine.SUBFLOW_ASYN_INVOKE);
		callback.setAfterAsynCall(this);
		callback.setInstance(ec.getProcessInstance());
		callback.setEc(ec);
		engine.call(subflow, (Map) args[0], callback);
		return null;
	}
}
