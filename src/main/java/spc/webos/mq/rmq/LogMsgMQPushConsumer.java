package spc.webos.mq.rmq;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.common.message.MessageExt;

import spc.webos.mq.rmq.AsbstractDefaultMQPushConsumer;

/**
 * 打印收到的消息
 * 
 * @author chenjs
 *
 */
public class LogMsgMQPushConsumer extends AsbstractDefaultMQPushConsumer
{
	final AtomicLong count = new AtomicLong(0);

	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
			ConsumeConcurrentlyContext context)
	{
		for (MessageExt msg : msgs)
		{
			log.info("thread:{},{}, tags:{}, keys:{}, body:{}", Thread.currentThread().getName(),
					count.incrementAndGet(), msg.getTags(), msg.getKeys(),
					new String(msg.getBody()));
		}
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
}
