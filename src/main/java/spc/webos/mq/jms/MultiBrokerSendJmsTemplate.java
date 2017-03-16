package spc.webos.mq.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Queue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.core.ProducerCallback;
import org.springframework.jms.core.SessionCallback;

public class MultiBrokerSendJmsTemplate implements JmsOperations
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected List<JmsTemplate> multiBrokerJms = new ArrayList<>();;
	private int index = 0;

	public void send(final String destination, final MessageCreator messageCreator)
			throws JmsException
	{
		sendCluster(destination, (jms) -> jms.send(destination, messageCreator));
	}

	public void send(final Destination destination, final MessageCreator messageCreator)
			throws JmsException
	{
		sendCluster(destination.toString(), (jms) -> jms.send(destination, messageCreator));
	}

	protected int sendCluster(final String destination, final Consumer<JmsTemplate> send)
			throws JmsException
	{
		if (index >= multiBrokerJms.size()) index = 0;
		int start = index++;
		JmsException ex = null;
		for (int i = 0; i < multiBrokerJms.size(); i++, start++)
		{
			if (start >= multiBrokerJms.size()) start = 0;
			JmsTemplate jms = multiBrokerJms.get(start);
			String broker = getBrokerInf(start, jms);
			try
			{
				log.info("snd des:{}, broker:{}", destination, broker);
				send.accept(jms);
				return start;
			}
			catch (JmsException jex)
			{
				log.info("Fail to send {} by index:{}, broker:{}, ex:{}", destination, start,
						broker, jex.toString());
				ex = jex;
			}
		}
		if (ex != null) throw ex;
		return -1;
	}

	protected String getBrokerInf(int index, JmsTemplate jms)
	{
		ConnectionFactory cf = jms.getConnectionFactory();
		if (cf instanceof CachingConnectionFactory && ((CachingConnectionFactory) cf)
				.getTargetConnectionFactory() instanceof ActiveMQConnectionFactory)
			return ((ActiveMQConnectionFactory) ((CachingConnectionFactory) cf)
					.getTargetConnectionFactory()).getBrokerURL();
		if (cf instanceof PooledConnectionFactory && ((PooledConnectionFactory) cf)
				.getConnectionFactory() instanceof ActiveMQConnectionFactory)
			return ((ActiveMQConnectionFactory) ((PooledConnectionFactory) cf)
					.getConnectionFactory()).getBrokerURL();
		return "broker_" + index;
	}

	public void setMultiBrokerJms(List<JmsTemplate> multiBrokerJms)
	{
		this.multiBrokerJms = multiBrokerJms;
	}

	@Override
	public <T> T execute(SessionCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}

	@Override
	public <T> T execute(ProducerCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}

	@Override
	public <T> T execute(Destination destination, ProducerCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public <T> T execute(String destinationName, ProducerCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public void send(MessageCreator messageCreator) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public void convertAndSend(Object message) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public void convertAndSend(Destination destination, Object message) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public void convertAndSend(String destinationName, Object message) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public void convertAndSend(Object message, MessagePostProcessor postProcessor)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}

	@Override
	public void convertAndSend(Destination destination, Object message,
			MessagePostProcessor postProcessor) throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}

	@Override
	public void convertAndSend(String destinationName, Object message,
			MessagePostProcessor postProcessor) throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}

	@Override
	public Message receive() throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message receive(Destination destination) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message receive(String destinationName) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message receiveSelected(String messageSelector) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message receiveSelected(Destination destination, String messageSelector)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message receiveSelected(String destinationName, String messageSelector)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Object receiveAndConvert() throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Object receiveAndConvert(Destination destination) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Object receiveAndConvert(String destinationName) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Object receiveSelectedAndConvert(String messageSelector) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Object receiveSelectedAndConvert(Destination destination, String messageSelector)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Object receiveSelectedAndConvert(String destinationName, String messageSelector)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message sendAndReceive(MessageCreator messageCreator) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message sendAndReceive(Destination destination, MessageCreator messageCreator)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public Message sendAndReceive(String destinationName, MessageCreator messageCreator)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public <T> T browse(BrowserCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public <T> T browse(Queue queue, BrowserCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public <T> T browse(String queueName, BrowserCallback<T> action) throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public <T> T browseSelected(String messageSelector, BrowserCallback<T> action)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");

	}

	@Override
	public <T> T browseSelected(Queue queue, String messageSelector, BrowserCallback<T> action)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}

	@Override
	public <T> T browseSelected(String queueName, String messageSelector, BrowserCallback<T> action)
			throws JmsException
	{
		throw new RuntimeException("unsupported method");
	}
}
