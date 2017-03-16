package spc.webos.service.seq.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.model.SeqNoPO;
import spc.webos.service.BaseService;
import spc.webos.service.seq.DbSeqNoService;

/**
 *
 * 
 * @author spc
 * 
 */
@Service("dbSeqNoService")
@Transactional
public class DbSeqNoServiceImpl extends BaseService implements DbSeqNoService
{
	protected Map<String, SeqNoPO> seqNoMap = new ConcurrentHashMap<>();
	protected String defaultName = "default";
	protected int retryTimes = 3;

	public String format(long uuid)
	{
		return String.valueOf(uuid);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
	public long uuid()
	{
		return nextId(defaultName);
	}

	// 必须是可重复读，事务传播级别是新事务
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
	public long nextId(String name)
	{
		int fail = 0;
		while (true)
		{
			try
			{
				return doNextId(name);
			}
			catch (AppException ae)
			{
				log.info("fail to fecth seqno:" + name + ", fail:" + fail, ae);
				if (fail++ > retryTimes) throw ae;
			}
		}
	}

	protected long doNextId(String name)
	{
		SeqNoPO seqno = seqNoMap.get(name);
		if (seqno != null)
		{
			synchronized (seqno)
			{ // 锁住当前流水号单元
				if (seqno.seqNo < seqno.maxSeqNo) return next(seqno);
			}
		}
		return doNextIdInDB(name);
	}

	protected synchronized long doNextIdInDB(String name)
	{
		SeqNoPO seqno = seqNoMap.get(name); // 有可能另外一个线程已经申请了一批新流水号放入Map中
		if (seqno != null && seqno.seqNo < seqno.maxSeqNo) return next(seqno);

		seqno = persistence.find(new SeqNoPO(name));
		if (seqno == null)
		{
			String errMsg = "cannot find SeqNoPO by " + name;
			log.error(errMsg);
			throw new AppException(AppRetCode.FAIL_SEQ_NO, errMsg);
		}

		long curSeqNo = seqno.seqNo;
		seqno.seqNo += seqno.getBatchSize();
		seqno.setRemark(null);
		int rows = persistence.update(seqno, (String[]) null, " and seqNo=" + curSeqNo, false,
				null);
		if (rows != 1)
		{
			seqno = null;
			String errMsg = "SeqNoPO update influence rows not 1(" + rows + "):" + seqno
					+ ", curseqno:" + curSeqNo;
			log.info(errMsg);
			throw new AppException(AppRetCode.FAIL_SEQ_NO, errMsg);
		}
		seqno.seqNo = curSeqNo;
		seqno.maxSeqNo = seqno.seqNo + seqno.getBatchSize();
		seqNoMap.put(name, seqno);
		log.info("Fetch seqno from DB sn:{}, max:{}", seqno.seqNo, seqno.maxSeqNo);
		return next(seqno);
	}

	private long next(SeqNoPO seqno)
	{
		if (seqno.recycleNo != null && seqno.recycleNo > 0)
			return (seqno.seqNo++) % seqno.recycleNo;
		return seqno.seqNo++;
	}

	public synchronized void synDB()
	{
		seqNoMap.clear();
	}

	public void setRetryTimes(int retryTimes)
	{
		this.retryTimes = retryTimes;
	}

	public void setDefaultName(String defaultName)
	{
		this.defaultName = defaultName;
	}
}
