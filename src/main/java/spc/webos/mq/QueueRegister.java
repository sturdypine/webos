package spc.webos.mq;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;

import spc.webos.util.JsonUtil;

public class QueueRegister implements BeanFactoryPostProcessor, PriorityOrdered
{
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void setRequestQ(String[] requestQ)
	{
		log.info("register requestQ:{}", Arrays.toString(requestQ));
		for (String q : requestQ)
			if (!MQ.REQUST_QUEUE.contains(q)) MQ.REQUST_QUEUE.add(q);
	}

	public void setQueue(String strQ)
	{
		log.info("register queue:{}", strQ);
		Map<String, String> queue = (Map<String, String>) JsonUtil.gson2obj(strQ);
		queue.forEach((k, v) -> MQ.QUEUES.put(k, v));
	}

	public void setResponseQ(String[] responseQ)
	{
		log.info("register responseQ:{}", Arrays.toString(responseQ));
		for (String q : responseQ)
			if (!MQ.RESPONSE_QUEUE.contains(q)) MQ.RESPONSE_QUEUE.add(q);
	}

	@Override
	public int getOrder()
	{
		return 10;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException
	{
	}
}
