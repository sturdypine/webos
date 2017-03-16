package spc.webos.mq.jms;

import java.util.Map;

import javax.annotation.Resource;
import javax.jms.BytesMessage;
import javax.jms.Message;

import org.springframework.jms.core.JmsOperations;

import spc.webos.constant.Common;
import spc.webos.service.seq.UUID;
import spc.webos.util.JsonUtil;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;;

public class JSCallMessageListener extends AbstractBytesMessageListener
{
	protected boolean request = true;

	protected void onMessage(Message msg, String queue, String corId, byte[] buf)
	{
		try
		{
			String request = new String(buf, charset);
			log.debug("queue:{}, corId:{}, request:{}", queue, corId, request);
			Map<String, Object> soap = (Map<String, Object>) JsonUtil.gson2obj(request);
			Map<String, Object> header = (Map<String, Object>) soap.get(JsonUtil.TAG_HEADER);
			if (this.request)
			{ // 请求报文
				String replyToQ = (String) header.get(JsonUtil.TAG_HEADER_REPLYTOQ);
				log.info("MQ JS request:{},{}, replyToQ:{}, len:{}", corId, queue, replyToQ,
						buf.length);
				doRequest((BytesMessage) msg, soap);
			}
			else
			{ // 应答报文
				log.info("MQ JS response:{},{}, len:{}, refSeqNb:{}", corId, queue, buf.length,
						(String) header.get(JsonUtil.TAG_HEADER_REFSNDSN));
				JsonUtil.jsonResponse(soap);
			}
		}
		catch (Exception e)
		{
			log.warn("ex:: corId:" + corId + ", buf:" + buf == null ? "" : new String(buf), e);
		}
	}

	protected void doRequest(BytesMessage msg, Map<String, Object> soap) throws Exception
	{
		Map<String, Object> header = (Map<String, Object>) soap.get(JsonUtil.TAG_HEADER);
		String replyToQ = (String) header.get(JsonUtil.TAG_HEADER_REPLYTOQ);
		String replyMsgCd = (String) header.get(JsonUtil.TAG_HEADER_REPLYMSGCD);
		boolean response = !StringX.nullity(replyToQ);
		JsonUtil.jsonRequest(soap, SpringUtil.APPCODE, response);
		if (!response) return;

		header.put(JsonUtil.TAG_HEADER_SN, uuid.format(uuid.uuid()));
		String json = JsonUtil.obj2json(soap);
		final byte[] buf = json.getBytes(Common.CHARSET_UTF8);
		log.debug("response json:{}", json);
		log.info("MQ JS response to: {}, replyMsgCd:{}, corId:{},  len:{}", replyToQ, replyMsgCd,
				msg.getJMSCorrelationID(), buf.length);
		jms.send(replyToQ, (s) -> {
			BytesMessage m = s.createBytesMessage();
			m.setStringProperty(Common.JMS_TRACE_NO, LogUtil.getTraceNo());
			m.setJMSCorrelationID(msg.getJMSCorrelationID());
			m.writeBytes(buf);
			return m;
		});
	}

	@Resource
	protected UUID uuid;
	protected JmsOperations jms;

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setJms(JmsOperations jms)
	{
		this.jms = jms;
	}

	public void setRequest(boolean request)
	{
		this.request = request;
	}
}
