package spc.webos.tcc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;

import com.google.gson.Gson;

import spc.webos.model.TccTerminatorPO;
import spc.webos.model.TccTransactionPO;
import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.util.FileUtil;

public class Transaction
{
	public String xid; // 内部生成的唯一事务编号
	public int status = STATUS_TRYING;

	public Integer exSeq;
	public String exSn;
	public String ex; // 当前事务执行的异常信息

	public String proxy; // 当前事务代理的方法信息
	public Object[] args; // 调用tcc事务的参数
	public String sn; // 外部TCC事务服务的流水号，用于和内部xid映射对应
	public Date createTm = new Date();
	protected List<Terminator> terminators = new ArrayList<Terminator>();

	public final static int STATUS_TRYING = 10;
	public final static int STATUS_TRY_FAIL = 11;
	public final static int STATUS_TRIED = 12;
	public final static int STATUS_CONFIRMING = 20;
	public final static int STATUS_CONFIRM_FAIL = 21;
	public final static int STATUS_CONFIRMED = 22;
	public final static int STATUS_CANCELLING = 30;
	public final static int STATUS_CANCEL_FAIL = 31;
	public final static int STATUS_CANCELED = 32;
	public static final String DF_ALL = "yyyy-MM-dd HH:mm:ss SSS";

	public Transaction()
	{
	}

	public Transaction(Map<String, String> map)
	{
		xid = map.get("xid").toString();
		status = Integer.parseInt(map.get("status").toString());
		ex = map.get("ex").toString();
		proxy = map.get("proxy").toString();
		sn = map.get("sn").toString();
		try
		{
			createTm = FastDateFormat.getInstance(DF_ALL).parse(map.get("createTm").toString());
			setTerminators(map.get("terms").toString());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Transaction(TccTransactionPO po)
	{
		xid = po.getXid();
		status = po.getStatus();
		exSeq = po.getExSeq();
		exSn = po.getExSn();
		ex = po.getEx();
		proxy = po.getProxy();
		sn = po.getSn();
		try
		{
			createTm = FastDateFormat.getInstance(DF_ALL).parse(po.getCreateTm());
			for (TccTerminatorPO t : po.getTerminators())
				terminators.add(new Terminator(t));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public Transaction(String xid)
	{
		this.xid = xid;
	}

	public void confirm() throws Exception
	{
		status = Transaction.STATUS_CONFIRMING;
		for (Terminator t : terminators)
		{
			try
			{
				t.confirm();
			}
			catch (Exception e)
			{
				status = Transaction.STATUS_CONFIRM_FAIL;
				exSeq = t.seq;
				exSn = t.sn;
				ex = e.toString();
				throw e;
			}
		}
		status = Transaction.STATUS_CONFIRMED;
	}

	public void cancel() throws Exception
	{
		status = Transaction.STATUS_CANCELLING;
		for (Terminator t : terminators)
		{
			try
			{
				t.cancel();
			}
			catch (Exception e)
			{
				status = Transaction.STATUS_CANCEL_FAIL;
				exSeq = t.seq;
				exSn = t.sn;
				ex = e.toString();
				throw e;
			}
		}
		status = Transaction.STATUS_CANCELED;
	}

	public String getXid()
	{
		return xid;
	}

	public void setXid(String xid)
	{
		this.xid = xid;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getEx()
	{
		return ex;
	}

	public void setEx(String ex)
	{
		this.ex = ex;
	}

	public String getProxy()
	{
		return proxy;
	}

	public void setProxy(String proxy)
	{
		this.proxy = proxy;
	}

	public String getSn()
	{
		return sn;
	}

	public void setSn(String sn)
	{
		this.sn = sn;
	}

	public Date getCreateTm()
	{
		return createTm;
	}

	public Map<String, String> toMap()
	{
		Map<String, String> map = new HashMap<>();
		map.put("xid", xid);
		map.put("status", String.valueOf(status));
		map.put("createTm", FastDateFormat.getInstance(DF_ALL).format(createTm));
		if (sn != null) map.put("sn", sn);
		map.put("proxy", proxy);
		if (ex != null) map.put("ex", ex);
		map.put("terms", getTerminators());

		return map;
	}

	public TccTransactionPO toPO()
	{
		TccTransactionPO po = new TccTransactionPO();
		po.setXid(xid);
		po.setStatus(status);
		po.setCreateTm(FastDateFormat.getInstance(DF_ALL).format(createTm));
		po.setSn(sn);
		po.setExSeq(exSeq);
		po.setExSn(exSn);
		po.setProxy(proxy);
		try
		{
			po.setArgs(new ByteArrayBlob(FileUtil.fst(args)));
		}
		catch (Exception e)
		{
		}
		po.setEx(ex);
		po.setTerminators(new ArrayList<TccTerminatorPO>());
		for (Terminator t : terminators)
			po.getTerminators().add(t.toPO());
		return po;
	}

	public List<Terminator> getTerminatorsList()
	{
		return terminators;
	}

	public String getTerminators()
	{
		StringBuffer terms = new StringBuffer();
		terms.append('[');
		for (Terminator t : terminators)
			terms.append((terms.length() < 2 ? "" : ",") + t.toJson());
		terms.append(']');
		return terms.toString();
	}

	public void setTerminators(String strterms) throws Exception
	{
		if (strterms == null || strterms.length() == 0) return;
		List<Map<String, String>> terms = new Gson().fromJson(strterms, ArrayList.class);
		for (Map<String, String> term : terms)
			terminators.add(new Terminator(term));
	}

	public void setTerminators(List<Terminator> terminators)
	{
		this.terminators = terminators;
	}

	public String toString()
	{
		return "TCC xid:" + xid + ",proxy" + proxy + ",createTm:" + createTm + ",status:" + status
				+ ",terms:" + terminators;
	}
}
