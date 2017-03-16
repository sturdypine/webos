package spc.webos.mq.rmq;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.producer.MQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

import spc.webos.constant.Common;
import spc.webos.service.seq.UUID;
import spc.webos.util.JsonUtil;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

/**
 * 接受RMQ消息类型的服务调用请求，如果有结果返回则将结果发往指定的topic, 请求和应答报文格式和esb soap一样 { Header:{
 * sndDt:'20160808', sndTm:'0909009', msgCd:'', seqNb:'', sndAppCd:'',
 * refSndAppCd:'', refSndDt:'', refMsgCd:'', refSeqNb:'', replyToQ:'',
 * status:{retcd:'',...} }, Body:{ args:[...] }}
 * 
 * @author chenjs
 *
 */
public class JSCallMQPushConsumer extends AsbstractDefaultMQPushConsumer
{
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
			ConsumeConcurrentlyContext context)
	{
		for (MessageExt msg : msgs)
		{
			boolean set = LogUtil.setTraceNo(msg.getKeys(), "jscrmq:" + msg.getTags(), true);
			try
			{
				Map<String, Object> soap = (Map<String, Object>) JsonUtil
						.gson2obj(new String(msg.getBody(), charset));
				Map<String, Object> header = (Map<String, Object>) soap.get(JsonUtil.TAG_HEADER);
				String refSeqNb = (String) header.get(JsonUtil.TAG_HEADER_REFSNDSN);
				if (StringX.nullity(refSeqNb))
				{ // 请求报文
					log.info("JS request tag:{},key:{}", msg.getTags(), msg.getKeys());
					doRequest(msg, soap);
				}
				else
				{ // 应答报文
					log.info("JS response tag:{},key:{}", msg.getTags(), msg.getKeys());
					JsonUtil.jsonResponse(soap);
				}
			}
			catch (Exception e)
			{
				log.warn("fail to call:" + new String(msg.getBody()), e);
			}
			finally
			{
				if (set) LogUtil.removeTraceNo();
			}
		}
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}

	protected void doRequest(MessageExt msg, Map<String, Object> soap) throws Exception
	{
		Map<String, Object> header = (Map<String, Object>) soap.get(JsonUtil.TAG_HEADER);
		String replyToQ = (String) header.get(JsonUtil.TAG_HEADER_REPLYTOQ);
		String replyMsgCd = (String) header.get(JsonUtil.TAG_HEADER_REPLYMSGCD);
		boolean response = !StringX.nullity(replyToQ);
		JsonUtil.jsonRequest(soap, SpringUtil.APPCODE, response);
		if (!response)
		{
			log.info("replyToQ is empty");
			return;
		}

		header.put(JsonUtil.TAG_HEADER_SN, uuid.format(uuid.uuid()));
		String json = JsonUtil.obj2json(soap);
		byte[] buf = json.getBytes(Common.CHARSET_UTF8);
		String tag = msg.getTags();
		String key = msg.getKeys();
		log.debug("response json:{}", json);
		log.info("RMQ send topic:{}, msgCd:{}, tag:{}, key:{}, len:{}", replyToQ, replyMsgCd, tag,
				key, buf.length);
		producer.send(new Message(replyToQ, tag, key, buf));
	}

	protected String charset = Common.CHARSET_UTF8;
	@Autowired
	protected UUID uuid;
	protected MQProducer producer;

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setProducer(MQProducer producer)
	{
		this.producer = producer;
	}
}
