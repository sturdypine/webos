package spc.webos.tcc.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.tcc.Transaction;

public class MemoryTCCRepository extends AbstractTCCRepository
{
	private final static Map<String, Transaction> TRANSACTIONS = new ConcurrentHashMap<String, Transaction>();
	private final static List<String> ERR_TRANSACTIONS = new java.util.concurrent.CopyOnWriteArrayList<String>();
	private AtomicLong xid = new AtomicLong(0l);

	public String createXid()
	{
		return String.valueOf(xid.incrementAndGet());
	}

	public Transaction create(Transaction transaction)
	{
		if (TRANSACTIONS.containsKey(transaction.xid))
			throw new AppException(AppRetCode.TCC_XID_REPEAT, new Object[] { transaction.xid });
		TRANSACTIONS.put(transaction.xid, transaction);
		return transaction;
	}

	public Transaction updateStatus(Transaction transaction)
	{
		if (transaction.status == Transaction.STATUS_CANCEL_FAIL
				|| transaction.status == Transaction.STATUS_CONFIRM_FAIL)
			ERR_TRANSACTIONS.add(transaction.xid);
		return transaction;
	}

	public Transaction delete(Transaction transaction, boolean byFail)
	{
		TRANSACTIONS.remove(transaction.xid);
		ERR_TRANSACTIONS.remove(transaction.xid);
		return transaction;
	}

	public Transaction find(String xid)
	{
		return TRANSACTIONS.get(xid);
	}

	public Collection<String> findAll(int from, int limit)
	{
		return TRANSACTIONS.keySet();
	}

	public Collection<String> findErr(int from, int limit)
	{
		return (Collection<String>) ERR_TRANSACTIONS;
	}
}
