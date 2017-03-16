package spc.webos.mq.jms;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.util.LogUtil;
import spc.webos.util.StringX;

public abstract class AbstractBytesMessageListener implements MessageListener
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected String charset = Common.CHARSET_UTF8;

	protected abstract void onMessage(Message msg, String queue, String corId, byte[] buf);

	public void onMessage(Message msg)
	{
		boolean set = false;
		byte[] buf = null;
		String corId = null, queue = null;
		try
		{
			queue = msg.getJMSDestination().toString().replaceAll("/", "");
			corId = msg.getJMSCorrelationID();
			String traceNo = msg.getStringProperty(Common.JMS_TRACE_NO);
			if (StringX.nullity(traceNo)) traceNo = corId;
			set = LogUtil.setTraceNo(traceNo, queue, true);

			buf = new byte[(int) ((BytesMessage) msg).getBodyLength()];
			((BytesMessage) msg).readBytes(buf);
			log.info("queue:{}, len:{}", queue, buf.length);
			if (AppConfig.getInstance().getProperty(Config.app_trace_mq + queue, true, false))
				log.info("queue:{}, msg:{}", queue, new String(buf));
			else if (log.isDebugEnabled()) log.debug("queue:{}, msg:{}", queue, new String(buf));
			onMessage(msg, queue, corId, buf);
		}
		catch (Exception e)
		{
			log.warn("ex:: corId:" + corId + ", buf:" + buf == null ? "" : new String(buf), e);
		}
		finally
		{
			if (set) LogUtil.removeTraceNo();
		}
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}
}
