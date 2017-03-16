package spc.webos.bpl.jbpm3.callback;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.bpl.jbpm3.IFlowEndCallback;
import spc.webos.util.POJOUtil;

public abstract class AbstractFlowEndCallback implements IFlowEndCallback
{
	protected String[][] properties;
	protected String corId;
	protected String refExt;
	protected Logger log = LoggerFactory.getLogger(getClass());

	public AbstractFlowEndCallback(String corId, String[][] properties, String refExt)
	{
		this.corId = corId;
		this.properties = properties;
		this.refExt = refExt;
	}

	public static Map ex2map(Throwable t)
	{
		if (t == null) return null;
		Map ex = POJOUtil.pojo2map(t, new HashMap(), new String[] { "code", "desc" });
		if (!ex.containsKey("code"))
		{
			ex.put("code", "999999");
			ex.put("desc", t.toString());
		}
		return ex;
	}
}
