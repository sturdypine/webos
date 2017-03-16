package spc.webos.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;

/*
 * 对对象的wait方法增加timeout判断
 */
public abstract class WaitWithTime
{
	protected Object target;
	protected long timeout = 1000;
	static Logger log = LoggerFactory.getLogger(WaitWithTime.class);

	public WaitWithTime()
	{
	}

	public void setTarget(Object target)
	{
		this.target = target;
	}

	public WaitWithTime(long timeout)
	{
		this.timeout = timeout;
	}

	public WaitWithTime(Object target)
	{
		this.target = target;
	}

	public abstract boolean condition();

	public final void timeWait()
	{
		if (!condition()) return;
		long start = System.currentTimeMillis();
		long waitTime = timeout;
		while (true)
		{
			try
			{
				target.wait(timeout);
			}
			catch (InterruptedException e)
			{
				log.error("WaitWithTime.timeWait", e);
			}
			if (!condition()) return;
			long timeSoFar = System.currentTimeMillis() - start;
			if (timeSoFar >= waitTime)
			{
				if (log.isInfoEnabled()) log.info("Thread(" + Thread.currentThread().getName()
						+ ") wait timeout:" + timeout + ", target:" + target.getClass());
				throw new AppException(AppRetCode.CMMN_BUF_TIMEOUT, new Object[] { timeout });
			}
			else waitTime = timeout - timeSoFar;
		}
	}

	public void announce()
	{
		target.notifyAll();
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}
}
