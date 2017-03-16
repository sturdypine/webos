package spc.webos.mq.rmq;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;

public abstract class AsbstractDefaultMQPushConsumer implements MessageListenerConcurrently
{
	protected String[] topic;
	protected String[] subExpression; // "TagA || TagC || TagD"
	protected DefaultMQPushConsumer consumer;
	protected Logger log = LoggerFactory.getLogger(getClass());

	public void start() throws Exception
	{
		for (int i = 0; i < topic.length; i++)
			consumer.subscribe(topic[i],
					subExpression != null && i < subExpression.length ? subExpression[i] : "*");
		consumer.registerMessageListener(this);
		consumer.start();
		log.info("consumer start group:{}, instance:{}, topic:{},{}", consumer.getConsumerGroup(),
				consumer.getInstanceName(), Arrays.toString(topic), Arrays.toString(subExpression));
	}

	public void shutdown()
	{
		if (consumer == null) return;
		log.info("consumer shutdown group:{}, instance:{}", consumer.getConsumerGroup(),
				consumer.getInstanceName());
		consumer.shutdown();
	}

	public String[] getTopic()
	{
		return topic;
	}

	public void setTopic(String[] topic)
	{
		this.topic = topic;
	}

	public String[] getSubExpression()
	{
		return subExpression;
	}

	public void setSubExpression(String[] subExpression)
	{
		this.subExpression = subExpression;
	}

	public DefaultMQPushConsumer getConsumer()
	{
		return consumer;
	}

	public void setConsumer(DefaultMQPushConsumer consumer)
	{
		this.consumer = consumer;
	}
}
