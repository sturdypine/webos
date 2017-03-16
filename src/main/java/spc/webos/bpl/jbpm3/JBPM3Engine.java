package spc.webos.bpl.jbpm3;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import spc.webos.bpl.AbstractEngine;
import spc.webos.bpl.jbpm3.callback.DubboAsynInvokeCallback;
import spc.webos.bpl.jbpm3.callback.SubflowAsynInvokeCallback;
import spc.webos.constant.Common;
import spc.webos.util.FileUtil;

public class JBPM3Engine extends AbstractEngine
{
	public boolean contain(String process)
	{
		return processDefinitions.containsKey(process);
	}

	public Map call(String process, Map params) throws Exception
	{
		ProcessDefinition pdefinition = (ProcessDefinition) processDefinitions.get(process);
		if (log.isInfoEnabled()) log.info("syn jpdl start: " + pdefinition.getName());
		ProcessInstance instance = new ProcessInstance(pdefinition);

		instance.getContextInstance().setTransientVariable(JBPM3_ENGINE, this);
		instance.getContextInstance().setVariables(params);
		Token token = instance.getRootToken();
		try
		{
			token.signal();
		}
		catch (DelegationException e)
		{
			throw (Exception) e.getCause();
		}

		if (!instance.hasEnded())
			log.warn("syn flow has no ended, process:" + process + ", params:" + params);
		if (log.isInfoEnabled()) log.info("jpdl end: " + pdefinition.getName());
		return instance.getContextInstance().getVariables();
	}

	public ProcessInstance call(String process, Map params, IFlowEndCallback callback)
			throws Exception
	{
		ProcessDefinition pdefinition = (ProcessDefinition) processDefinitions.get(process);
		if (log.isInfoEnabled()) log.info("asyn jpdl start: " + pdefinition.getName());
		ProcessInstance instance = new ProcessInstance(pdefinition);
		instance.getContextInstance().setTransientVariable(DUBBO_ASYN_INVOKE,
				new DubboAsynInvokeCallback(callback));
		instance.getContextInstance().setTransientVariable(SUBFLOW_ASYN_INVOKE,
				new SubflowAsynInvokeCallback(callback));
		instance.getContextInstance().setTransientVariable(JBPM3_ENGINE, this);
		instance.getContextInstance().setTransientVariable(JBPM3_END_CALLBACK, callback);

		instance.getContextInstance().setVariables(params);
		Token token = instance.getRootToken();
		try
		{
			token.signal();
		}
		catch (DelegationException de)
		{
			throw (Exception) de.getCause();
		}

		if (instance.hasEnded())
		{
			log.info("asyn process:" + process + " ended");
			callback.end(instance, null);
		}
		return instance;
	}

	public void init() throws Exception
	{
		PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();
		Resource[] reses = pmrpr.getResources(resource);
		for (Resource res : reses)
		{
			ProcessDefinition definition = ProcessDefinition.parseXmlString(
					new String(FileUtil.is2bytes(res.getInputStream()), jpdlCharset));
			log.info("jpdl32:" + res + ", process:" + definition.getName());
			if (processDefinitions.containsKey(definition.getName()))
				log.warn("process override:" + definition.getName() + ", res:" + res);
			processDefinitions.put(definition.getName(), definition);
		}
	}

	protected String jpdlCharset = Common.CHARSET_UTF8;
	protected String resource = "classpath*:META-INF/jpdl32/**/processdefinition.xml";
	protected Map<String, ProcessDefinition> processDefinitions = new HashMap<String, ProcessDefinition>(); // 流程定义
	protected IFlowEndCallback defaultJBPM3EndCallback;
	public final static String DUBBO_ASYN_INVOKE = "_DUBBO_ASYN_INVOKE";
	public final static String SUBFLOW_ASYN_INVOKE = "_SUBFLOW_ASYN_INVOKE";
	public final static String JBPM3_ENGINE = "_JBPM3_ENGINE";
	public final static String JBPM3_END_CALLBACK = "_JBPM3_END_CALLBACK";

	public void setDefaultJBPM3EndCallback(IFlowEndCallback defaultJBPM3EndCallback)
	{
		this.defaultJBPM3EndCallback = defaultJBPM3EndCallback;
	}

	public void setJpdlCharset(String jpdlCharset)
	{
		this.jpdlCharset = jpdlCharset;
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}
}
