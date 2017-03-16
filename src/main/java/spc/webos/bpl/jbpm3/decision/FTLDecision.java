package spc.webos.bpl.jbpm3.decision;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.DecisionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;

public class FTLDecision implements DecisionHandler
{
	private static final long serialVersionUID = 1L;
	protected transient Logger log = LoggerFactory.getLogger(getClass());

	public String decide(ExecutionContext ec) throws Exception
	{
		Map root = FTLUtil.model(new HashMap(ec.getContextInstance().getTransientVariables()));
		String transition = StringX.trim(FTLUtil.freemarker(ftl, root));
		if (log.isInfoEnabled()) log.info("transition:" + transition);
		return transition;
	}

	protected String ftl;
}
