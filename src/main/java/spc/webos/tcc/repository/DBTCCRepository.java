package spc.webos.tcc.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.exception.AppException;
import spc.webos.model.TccTerminatorPO;
import spc.webos.model.TccTransactionPO;
import spc.webos.persistence.IPersistence;
import spc.webos.tcc.TCCTransactional;
import spc.webos.tcc.Terminator;
import spc.webos.tcc.Transaction;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class DBTCCRepository extends AbstractTCCRepository
{
	@Override
	@Transactional(value = "tcc", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public Transaction create(Transaction transaction) throws Exception
	{
		TccTransactionPO po = transaction.toPO();
		po.setGname(group);
		po.setApp(SpringUtil.APPCODE);
		po.setJvm(SpringUtil.JVM);
		persistence.insert(po);
		return transaction;
	}

	@Override
	@Transactional(value = "tcc", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public Transaction updateStatus(Transaction transaction)
	{
		// 为了减少数据库交互次数，只有终态update数据库(21,22,31,32)
		if (transaction.status <= Transaction.STATUS_CONFIRMING
				|| transaction.status == Transaction.STATUS_CANCELLING)
			return transaction;
		List<Integer> tseq = new ArrayList<>();
		transaction.getTerminatorsList().forEach((t) -> {
			if (t.status == Transaction.STATUS_CONFIRMED || t.status == Transaction.STATUS_CANCELED)
				tseq.add(t.seq);
		});
		TccTransactionPO po = new TccTransactionPO(transaction.xid);
		po.setStatus(transaction.status);
		po.setExSeq(transaction.exSeq);
		po.setExSn(transaction.exSn);
		po.setEx(transaction.ex);
		po.setLastStatusTm(FastDateFormat.getInstance(Transaction.DF_ALL).format(new Date()));
		try
		{ // 容许修改状态时数据库失败，不影响业务调用
			int row = persistence.update(po);
			if (!tseq.isEmpty())
			{ // 修改每个原子交易的已完成的confirmed/canceled状态
				Map<String, Object> params = new HashMap<>();
				params.put("xid", transaction.xid);
				params.put("status", (transaction.status / 10) * 10 + 2);
				params.put("seq", StringX.join(tseq, StringX.COMMA));
				int[] rows = (int[]) persistence.execute(tccUpdTermStatusSql, params);
				log.info("update trans({}) & term status({})", row, rows[0]);
			}
			else log.info("update trans({})", row);
		}
		catch (Exception e)
		{
			log.warn("fail to update tx: " + transaction.xid + ", " + transaction.sn + ", "
					+ transaction.status, e);
		}
		return transaction;
	}

	@Transactional(value = "tcc", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public Terminator updateStatus(Terminator t)
	{
		TccTerminatorPO po = new TccTerminatorPO(t.xid, t.seq);
		po.setStatus(t.status);
		po.setCannotCancel(t.cannotCancel ? "1" : "0");
		persistence.update(po);
		return t;
	}

	@Transactional(value = "tcc", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public Terminator addTerminator(Transaction transaction, TCCTransactional tcc, String tid,
			int seq, Object target, String clazz, String m, Class[] parameterTypes, Object[] args)
			throws Exception
	{
		return super.addTerminator(transaction, tcc, tid, seq, target, clazz, m, parameterTypes,
				args);
	}

	protected void addTerminator(Transaction transaction, Terminator t)
	{
		super.addTerminator(transaction, t);
		persistence.insert(t.toPO());
	}

	@Override
	@Transactional(value = "tcc", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public Transaction delete(Transaction transaction, boolean byFail) throws Exception
	{ // 数据库存储时，不需要删除，作为历史数据统一清理
		return transaction;
	}

	@Override
	public Transaction find(String xid) throws Exception
	{
		TccTransactionPO po = persistence.find(new TccTransactionPO(xid));
		if (po == null) return null;
		return new Transaction(po);
	}

	@Override
	public Collection<String> findAll(int start, int limit) throws AppException
	{
		Map<String, Object> params = new HashMap();
		params.put("start", String.valueOf(start));
		params.put("limit", String.valueOf(limit));
		params.put("gname", group);
		return (Collection<String>) persistence.execute(tccXidSql, params);
	}

	@Override
	public Collection<String> findErr(int start, int limit) throws AppException
	{
		Map<String, Object> params = new HashMap();
		params.put("start", String.valueOf(start));
		params.put("limit", String.valueOf(limit));
		params.put("gname", group);
		params.put("status", "21,31");
		return (Collection<String>) persistence.execute(tccXidSql, params);
	}

	protected String tccXidSql = "common_tccXid";
	protected String tccUpdTermStatusSql = "common_tccUpdTermStatus";
	@Resource
	protected IPersistence persistence;

	public void setPersistence(IPersistence persistence)
	{
		this.persistence = persistence;
	}

	public void setTccXidSql(String tccXidSql)
	{
		this.tccXidSql = tccXidSql;
	}

	public void setTccUpdTermStatusSql(String tccUpdTermStatusSql)
	{
		this.tccUpdTermStatusSql = tccUpdTermStatusSql;
	}
}
