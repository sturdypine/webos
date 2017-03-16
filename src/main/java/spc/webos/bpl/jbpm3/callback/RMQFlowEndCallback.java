package spc.webos.bpl.jbpm3.callback;

import org.jbpm.graph.exe.ProcessInstance;

import com.alibaba.rocketmq.client.producer.MQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

public class RMQFlowEndCallback extends AbstractMQFlowEndCallback
{
	protected MQProducer producer;

	public RMQFlowEndCallback(MQProducer producer, String corId, String refExt, String topic,
			String tag, String[][] properties)
	{
		super(topic, tag, corId, refExt, properties);
		this.producer = producer;
	}

	public void end(ProcessInstance instance, Throwable t)
	{
		SendResult result = null;
		try
		{
			Message msg = new Message(topic, tag, toMessage(instance, t));
			result = producer.send(msg);
			if (log.isInfoEnabled()) log.info("RMQ snd:" + result.getSendStatus() + ", instance:"
					+ instance.getId() + ", id:" + corId + ", rmqId:" + result.getMsgId()
					+ ", topic:" + topic + ", tag:" + tag);
		}
		catch (Exception e)
		{
			log.warn("Fail to RMQ snd id:" + corId + ", instance:" + instance.getId() + ", topic:"
					+ topic + ", tag:" + tag
					+ (result == null ? ""
							: ", status:" + result.getSendStatus() + ", rmqId:"
									+ result.getMsgId()),
					e);
		}
	}
}
