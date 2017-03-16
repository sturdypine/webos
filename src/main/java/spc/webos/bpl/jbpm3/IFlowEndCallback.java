package spc.webos.bpl.jbpm3;

import org.jbpm.graph.exe.ProcessInstance;

public interface IFlowEndCallback
{
	void end(ProcessInstance instance, Throwable t);
}
