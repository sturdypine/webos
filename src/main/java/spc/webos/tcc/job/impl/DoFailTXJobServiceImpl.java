package spc.webos.tcc.job.impl;

import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;

import spc.webos.constant.Config;
import spc.webos.service.job.MasterSlaveJobService;
import spc.webos.service.job.impl.LeaderLatchMSJobServiceImpl;
import spc.webos.tcc.Repository;
import spc.webos.tcc.Transaction;

public class DoFailTXJobServiceImpl extends LeaderLatchMSJobServiceImpl
{
	@Resource
	protected Repository repository;
	protected volatile int batchStart = 0; // 每批开始
	@Value("${tcc.runDoFailTx?false}")
	protected volatile boolean runDoFailTx;
	private Thread daemon;

	@PostConstruct
	public void init() throws Exception
	{
		if (!runDoFailTx) return;
		// String beans = FTLUtil.ftl("webos/tcc", null);
		// SpringUtil.registerXMLBean(beans, false);
		// repository = SpringUtil.APPCXT.getBean("tccRepository",
		// Repository.class);
		// SpringUtil.activeRegisterBeans("tccDSAdvice","tccAdvice");
		name = "tccDoFailTXJob";
		zkPath = "/Job/LL/TCC/" + repository.group();
		log.info("TCC Job:{}, zkPath:{}, runDoFailTx:{}", name, zkPath, runDoFailTx);
		daemon = new Thread(() -> {
			while (runDoFailTx)
			{
				try
				{
					Thread.sleep((Integer) config.getProperty(Config.tcc_repeatSeconds, false, 120)
							* 1000);
					if (!runDoFailTx) return;
					// 采用主从模式执行
					super.syn(concurrent,
							(l) -> ((MasterSlaveJobService) self).execute(false, null));
				}
				catch (Exception e)
				{
					log.warn("fail to run:" + name + "," + zkPath, e);
				}
			}
			log.info("runDoFailTx thread stopped...");
		});
		daemon.setDaemon(true);
		daemon.start();
	}

	@Override
	public void destroy()
	{
		if (daemon != null)
		{
			log.info("interrupt job:{}", name);
			runDoFailTx = false;
			daemon.interrupt();
			daemon = null;
		}
	}

	public void execute(boolean force, Map<String, Object> params)
	{
		Integer batchSize = (Integer) config.getProperty(Config.tcc_batchSize, false, 25);
		Collection<String> errXids = repository.findErr(batchStart, batchSize);
		if (errXids == null || errXids.size() == 0)
		{ // 如果查不到结果重置开始位置为0
			log.info("batchStart:{}, limit:{} is zero", batchStart, batchSize);
			batchStart = 0;
			return;
		}
		log.info("TCC() err start:{}, xid:{}", repository.group(), batchStart, errXids);
		batchStart += batchSize;
		for (String xid : errXids)
			completeTx(xid);
	}

	protected void completeTx(String xid)
	{
		String status = "find";
		try
		{
			Transaction t = repository.find(xid);
			if (t.status == Transaction.STATUS_CANCEL_FAIL)
			{
				status = "Cancel " + t.proxy;
				log.info("xid:{}, {}", xid, status);
				t.cancel();
				t.status = Transaction.STATUS_CANCELED;
				// 事务执行成功，单事务状态库更新不成功？？？？
				repository.updateStatus(t); // 只修改事务状态，是否删除由资源库内部决定
			}
			else if (t.status == Transaction.STATUS_CONFIRM_FAIL)
			{
				status = "Confirm " + t.proxy;
				log.info("xid:{}, {}", xid, status);
				t.confirm();
				t.status = Transaction.STATUS_CONFIRMED;
				// 事务执行成功，单事务状态库更新不成功？？？？
				repository.updateStatus(t); // 只修改事务状态，是否删除由资源库内部决定
			}
			else log.warn("TCC({}) err status:{}", xid, t.status);
		}
		catch (Exception e)
		{
			log.info("fail to doFailTx: " + status + ":" + xid, e);
		}
	}

	public void setRepository(Repository repository)
	{
		this.repository = repository;
	}

	public void setBatchStart(int batchStart)
	{
		this.batchStart = batchStart;
	}

	public void setRunDoFailTx(boolean runDoFailTx)
	{
		this.runDoFailTx = runDoFailTx;
	}
}
