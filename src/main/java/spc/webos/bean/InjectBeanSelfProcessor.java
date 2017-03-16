package spc.webos.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class InjectBeanSelfProcessor implements BeanPostProcessor
{
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException
	{
		if (bean instanceof BeanSelfAware) ((BeanSelfAware) bean).self(bean);
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException
	{
		return bean;
	}
}