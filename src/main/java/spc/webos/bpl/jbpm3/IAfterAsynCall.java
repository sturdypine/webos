package spc.webos.bpl.jbpm3;

import org.jbpm.graph.exe.ExecutionContext;

public interface IAfterAsynCall
{
	void afterInvoke(ExecutionContext ec, Object result, Throwable t) throws Exception;
}
