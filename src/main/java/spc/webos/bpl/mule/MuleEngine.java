package spc.webos.bpl.mule;

import java.util.Map;

import org.mule.DefaultMuleMessage;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.mule.config.ConfigResource;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import spc.webos.bpl.AbstractEngine;

public class MuleEngine extends AbstractEngine implements ApplicationContextAware
{
	public Map<String, Object> call(String process, Map<String, Object> params) throws Exception
	{
		return (Map<String, Object>) call(process, (Object) params);
	}

	public Object call(String process, Object obj) throws Exception
	{
		MuleClient client = mule.getClient();
		MuleMessage result = client.send("vm://" + process,
				new DefaultMuleMessage(obj, (Map<String, Object>) null, mule));
		ExceptionPayload expayload = result.getExceptionPayload();
		if (expayload != null) throw (Exception) expayload.getRootException();
		return result.getPayload();
	}

	public void refresh() throws Exception
	{
		if (mule != null) return;
		SpringXmlConfigurationBuilder builder;
		if (resource == null)
		{
			log.info("load default mule.xml");
			builder = new SpringXmlConfigurationBuilder(new ConfigResource[] {
					new ConfigResource("mule", getClass().getResourceAsStream("mule.xml")) });
		}
		else
		{
			log.info("load mule:{}", resource.getFilename());
			builder = new SpringXmlConfigurationBuilder(new ConfigResource[] {
					new ConfigResource(resource.getFilename(), resource.getInputStream()) });
		}
		builder.setParentContext(spring);
		mule = new DefaultMuleContextFactory().createMuleContext(builder);
		mule.start();
		log.info("mule OK");
	}

	public void destroy()
	{
		if (mule == null) return;
		log.info("mule stop");
		try
		{
			mule.stop();
		}
		catch (Exception e)
		{
		}
		mule.dispose();
		mule = null;
	}

	public Object getMuleContext()
	{
		return mule;
	}

	protected MuleContext mule;
	private static MuleEngine engine = new MuleEngine();
	protected ApplicationContext spring;
	protected Resource resource;

	public static MuleEngine getInstance()
	{
		return engine;
	}

	private MuleEngine()
	{
	}

	public void setApplicationContext(ApplicationContext cxt) throws BeansException
	{
		spring = cxt;
	}

	public void setResource(Resource resource)
	{
		this.resource = resource;
	}
}
