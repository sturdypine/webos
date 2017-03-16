package spc.webos.bpl.jbpm3.callback;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.graph.exe.ProcessInstance;

import com.google.gson.Gson;

import spc.webos.util.POJOUtil;

public abstract class AbstractMQFlowEndCallback extends AbstractFlowEndCallback
{
	protected String topic;
	protected String tag;
	protected String charset = "utf-8";

	public AbstractMQFlowEndCallback(String topic, String tag, String corId, String refExt,
			String[][] properties)
	{
		super(corId, properties, refExt);
		this.topic = topic;
		this.tag = tag;
	}

	protected byte[] toMessage(ProcessInstance instance, Throwable t)
			throws UnsupportedEncodingException
	{
		Map ret = new HashMap();
		ret.put("success", t == null ? true : false);
		if (t == null) ret.put("result",
				POJOUtil.map2map(instance.getContextInstance().getVariables(), properties));
		else ret.putAll(ex2map(t));
		if (corId != null) ret.put("corId", corId);
		if (refExt != null) ret.put("refExt", refExt);
		ret.put("topic", topic);
		ret.put("processId", instance.getId());
		if (tag != null) ret.put("tag", tag);
		String json = new Gson().toJson(ret);
		if (log.isDebugEnabled()) log.debug("msg.json:" + json);
		return json.getBytes(charset);
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}
}
