package spc.webos.mq.jms;

import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Message;

import spc.webos.constant.Common;

/**
 * 打印收到的消息
 * 
 * @author chenjs
 *
 */
public class LogMsgMessageListener extends AbstractBytesMessageListener
{
	final AtomicLong count = new AtomicLong(0);

	protected void onMessage(Message msg, String queue, String corId, byte[] buf)
	{
		try
		{
			log.info("thread:{},{}, id:{}, body:{}", Thread.currentThread().getName(),
					count.incrementAndGet(), msg.getJMSCorrelationID(),
					new String(buf, Common.CHARSET_UTF8));
		}
		catch (Exception e)
		{
			log.warn("onMessage:", e);
		}
	}
}
