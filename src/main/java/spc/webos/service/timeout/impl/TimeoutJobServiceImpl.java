package spc.webos.service.timeout.impl;

import java.util.List;

import spc.webos.service.BaseService;
import spc.webos.service.timeout.TimeoutHandlerService;
import spc.webos.service.timeout.TimeoutJobService;
import spc.webos.service.timeout.TimeoutService;
import spc.webos.service.timeout.Timeout;

public class TimeoutJobServiceImpl extends BaseService implements TimeoutJobService
{
	public void timeout() throws Exception
	{
		List timeouts = timeoutFinderService.find();
		while (timeouts != null && timeouts.size() > 0)
		{
			for (int i = 0; i < timeouts.size(); i++)
			{
				Timeout timeout = (Timeout) timeouts.get(i);
				if (timeoutFinderService.remove(timeout))
				{
					if (log.isInfoEnabled()) log
							.info("start to handle timeout: " + timeout.getSn());
					try
					{
						timeoutHandlerService.doTimeout(timeout);
					}
					catch (Throwable t)
					{
						log.warn("handler err for " + timeout, t);
					}
				}
				else if (log.isInfoEnabled()) log
						.info("fail to remove timeout: " + timeout.getSn());
			}
			// 循环处理，直到没有超时信息后完成
			timeouts = timeoutFinderService.find();
		}
	}

	protected TimeoutService timeoutFinderService;
	protected TimeoutHandlerService timeoutHandlerService;

	public void setTimeoutFinderService(TimeoutService timeoutFinderService)
	{
		this.timeoutFinderService = timeoutFinderService;
	}

	public void setTimeoutHandlerService(TimeoutHandlerService timeoutHandlerService)
	{
		this.timeoutHandlerService = timeoutHandlerService;
	}
}
