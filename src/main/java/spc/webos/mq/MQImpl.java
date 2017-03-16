package spc.webos.mq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;
import javax.jms.BytesMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;

import com.google.gson.Gson;

import spc.webos.cache.MapCache;
import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.service.seq.UUID;
import spc.webos.util.FTLUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

/**
 * 启动AMQ监听
 * 
 * @author chenjs
 *
 */
public class MQImpl implements MQ, ApplicationListener<ApplicationEvent>
{
	@Value("${app.mq.brokers?}")
	protected String[] brokers;
	@Value("${app.mq.start?true}")
	protected boolean start = true;
	@Value("${app.mq.reqQ?}")
	protected String[] reqQ;
	@Value("${app.mq.resQ?}")
	protected String[] resQ;
	protected String defResQ;
	@Value("${app.mq.reqJvmQ?}")
	protected String reqJvmQ;
	@Value("${app.mq.resJvmQ?}")
	protected String resJvmQ;
	protected JmsOperations jms;
	@Autowired(required = false)
	protected UUID uuid;
	// jms配置信息

	@Value("${app.mq.callback?mq.callback}")
	protected String callback = "mq.callback";
	@Value("${app.mq.ftl?webos/amq}")
	protected String ftl = "webos/amq";
	@Value("${app.mq.jmsName?amqJms}")
	protected String jmsName = "amqJms";
	protected MapCache<String, Callback<?>> callbacks = new MapCache<>(200, 120);

	@Autowired(required = false)
	protected spc.webos.config.Config config = AppConfig.getInstance();
	protected final Logger log = LoggerFactory.getLogger(getClass());

	final static int MSG_EXPIRE = 60;
	final static int CB_EXPIRE = 120;

	public void notice(String queue, String method, String args) throws AppException
	{
		send(queue, null, null, method, null, (String) null, args,
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null);
	}

	public void noticeList(String queue, String method, List<Object> args) throws AppException
	{
		send(queue, null, null, method, null, (String) null, args,
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null);
	}

	public void noticeMap(String queue, String method, Map<String, Object> args) throws AppException
	{
		send(queue, null, null, method, null, (String) null, args,
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null);
	}

	public void snd(String queue, String method, String resQ, String callback, String args)
	{
		send(queue, null, null, method, StringX.nullity(resQ) ? defResQ : resQ, callback, args,
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null);
	}

	public void sndList(String queue, String method, String resQ, String callback,
			List<Object> args)
	{
		send(queue, null, null, method, StringX.nullity(resQ) ? defResQ : resQ, callback, args,
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null);
	}

	public void sndMap(String queue, String method, String resQ, String callback,
			Map<String, Object> args)
	{
		send(queue, null, null, method, StringX.nullity(resQ) ? defResQ : resQ, callback, args,
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null);
	}

	public void send(String queue, String sndAppCd, String sn, String method, String resQ,
			String callback, Object args, int expire, MessageCreator messageCreator)
	{
		String seqNb = StringX.nullity(sn) ? uuid.format(uuid.uuid()) : sn;
		String json = JsonUtil
				.obj2json(JsonUtil.soap(seqNb, sndAppCd, method, resQ, callback, args));
		jms.send(queue, (s) -> {
			BytesMessage msg = messageCreator != null
					? (BytesMessage) messageCreator.createMessage(s) : s.createBytesMessage();
			msg.setStringProperty(Common.JMS_TRACE_NO, LogUtil.getTraceNo());
			if (expire > 0) msg.setJMSExpiration(expire * 1000);
			msg.setJMSCorrelationID(seqNb);
			msg.writeBytes(json.getBytes());
			return msg;
		});
	}

	public <T> T sync(String queue, String method, String args, Future<T> future)
			throws AppException
	{
		int callbackExpire = future.timeout / 1000;
		if (callbackExpire < 1) callbackExpire = 1;
		asyncall(queue, null, null, method, args, callbackExpire, callbackExpire, null, future);
		return future.get();
	}

	public <T> T sync(String queue, String method, List<Object> args, Future<T> future)
			throws AppException
	{
		int callbackExpire = future.timeout / 1000;
		if (callbackExpire < 1) callbackExpire = 1;
		asyncall(queue, null, null, method, args, callbackExpire, callbackExpire, null, future);
		return future.get();
	}

	public <T> T sync(String queue, String method, Map<String, Object> args, Future<T> future)
			throws AppException
	{
		int callbackExpire = future.timeout / 1000;
		if (callbackExpire < 1) callbackExpire = 1;
		asyncall(queue, null, null, method, args, callbackExpire, callbackExpire, null, future);
		return future.get();
	}

	public <T> void async(String queue, String method, String args, Callback<T> callback)
			throws AppException
	{
		asyncall(queue, null, null, method, args,
				(Integer) config.getProperty(Config.app_mq_cb_expire, false, CB_EXPIRE),
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null,
				callback);
	}

	public <T> void async(String queue, String method, List<Object> args, Callback<T> callback)
			throws AppException
	{
		asyncall(queue, null, null, method, args,
				(Integer) config.getProperty(Config.app_mq_cb_expire, false, CB_EXPIRE),
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null,
				callback);
	}

	public <T> void async(String queue, String method, Map<String, Object> args,
			Callback<T> callback) throws AppException
	{
		asyncall(queue, null, null, method, args,
				(Integer) config.getProperty(Config.app_mq_cb_expire, false, CB_EXPIRE),
				(Integer) config.getProperty(Config.app_mq_msg_expire, false, MSG_EXPIRE), null,
				callback);
	}

	public <T> void asyncall(String queue, String sndAppCd, String sn, String method, Object args,
			int timeout, int expire, MessageCreator messageCreator, Callback<T> callback)
			throws AppException
	{
		if (StringX.nullity(sn)) sn = uuid.format(uuid.uuid());
		if (callbacks.get(sn) != null)
			throw new AppException(AppRetCode.REPEAT_SN, new Object[] { sn });
		if (StringX.nullity(sndAppCd)) sndAppCd = SpringUtil.APPCODE;
		if (timeout > 0) callbacks.put(sn, callback, timeout);
		else callbacks.put(sn, callback);
		log.info("call sn:{}, Q:{}, m:{}", sn, queue, method);
		send(queue, sndAppCd, sn, method, resJvmQ, this.callback, args, expire, messageCreator);
	}

	public void callback(Map<String, Object> soap)
	{
		Map<String, Object> header = (Map<String, Object>) soap.get(JsonUtil.TAG_HEADER);
		Map<String, String> status = (Map<String, String>) header.get(JsonUtil.TAG_HEADER_STATUS);
		String refSeqNb = (String) header.get(JsonUtil.TAG_HEADER_REFSNDSN);

		Callback cb = callbacks.remove(refSeqNb);
		if (cb == null)
		{
			log.warn("no MQ callback for:{}, probably timeout!!", refSeqNb);
			return;
		}
		try
		{
			Object ret = null;
			Object body = soap.get(JsonUtil.TAG_BODY);
			if (body != null)
			{
				Gson gson = new Gson();
				String args = gson.toJson(body);
				ret = gson.fromJson(args, cb.getType());
				if (log.isDebugEnabled())
					log.debug("callback:: type:{}, ret:{}, args:{}", cb.getType(), ret, args);
			}
			else log.info("json no body");
			cb.callback(ret, status);
		}
		finally
		{
			cb.close();
		}
	}

	public void execute(Consumer<JmsOperations> consumer) throws Exception
	{
		consumer.accept(jms);
	}

	public JmsOperations jms()
	{
		return jms;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event)
	{ // 只有spring容器启动OK后才能启动MQ监听
		if (!(event instanceof ContextRefreshedEvent)) return;
		try
		{
			init();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void init() throws Exception
	{
		if (brokers == null || brokers.length == 0 || !start || jms != null)
		{
			log.info("no MQ brokers or start !!!");
			return;
		}
		if (reqQ == null || reqQ.length == 0) reqQ = new String[] { "REQ_" + SpringUtil.APPCODE };
		if (StringX.nullity(reqJvmQ)) reqJvmQ = "REQ_" + SpringUtil.APPCODE + "_" + SpringUtil.JVM;
		defResQ = "REP_" + SpringUtil.APPCODE;
		if (resQ == null || resQ.length == 0) resQ = new String[] { defResQ };
		if (StringX.nullity(resJvmQ)) resJvmQ = "REP_" + SpringUtil.APPCODE + "_" + SpringUtil.JVM;
		log.info("MQ reqQ:{}+{}, reqJvmQ:{}, resQ:{}+{}, resJvmQ:{}, queues:{}, brokers:{}",
				Arrays.toString(reqQ), MQ.REQUST_QUEUE, reqJvmQ, Arrays.toString(resQ),
				MQ.RESPONSE_QUEUE, resJvmQ, QUEUES, brokers);
		Map<String, Object> root = new HashMap<>();
		root.put("brokers", brokers);
		root.put("reqQ", reqQ);
		root.put("resQ", resQ);
		root.put("reqJvmQ", reqJvmQ);
		root.put("resJvmQ", resJvmQ);
		root.put("queues", QUEUES);

		String beans = FTLUtil.ftl(ftl, root);
		SpringUtil.registerXMLBean(beans, false);
		jms = SpringUtil.APPCXT.getBean(jmsName, JmsOperations.class);
	}

	@PreDestroy
	public void destroy()
	{
		callbacks.destroy();
	}

	public void setBrokers(String[] brokers)
	{
		this.brokers = brokers;
	}

	public void setReqQ(String[] reqQ)
	{
		this.reqQ = reqQ;
	}

	public void setResQ(String[] resQ)
	{
		this.resQ = resQ;
	}

	public void setResJvmQ(String resJvmQ)
	{
		this.resJvmQ = resJvmQ;
	}

	public void setJms(JmsOperations jms)
	{
		this.jms = jms;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}

	public void setCallback(String callback)
	{
		this.callback = callback;
	}

	public void setReqJvmQ(String reqJvmQ)
	{
		this.reqJvmQ = reqJvmQ;
	}

	public void setFtl(String ftl)
	{
		this.ftl = ftl;
	}

	public void setStart(boolean start)
	{
		this.start = start;
	}

	public void setConfig(spc.webos.config.Config config)
	{
		this.config = config;
	}
}
