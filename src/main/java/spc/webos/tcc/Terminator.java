package spc.webos.tcc;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import spc.webos.model.TccTerminatorPO;
import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.util.FileUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class Terminator
{
	public String xid; // 所属事务号
	public int seq; // 每个子事务服务在事务中的顺序号，0开始
	public String tsn; // Transaction sn
	public String sn; // 当前原子事务外部业务唯一流水号, 类似于Transaction.sn
	public transient Object target;
	public String clazz;
	public String method;
	public Class[] parameterTypes;
	public Object[] args;
	public int status = Transaction.STATUS_TRYING; // 每个原子交易的状态
	public boolean doTry = true; // 当前原子交易无需try
	public boolean doConfirm = true; // 当前原子交易无需confirm
	public boolean cannotCancel = false; // 当前执行原子交易不能cancel,流水号重复

	protected transient Logger log = LoggerFactory.getLogger(getClass());
	public static String PREFIX_CONFIRM = "confirm"; // confirmXXX
	public static String PREFIX_CANCEL = "cancel"; // cancelXXX
	public static int PREFIX_TRY_LENGTH = 3; // "tryXXX"

	public Terminator(String xid, String tsn, int seq, String sn, boolean doTry, boolean doConfirm,
			Object target, String clazz, String m, Class[] parameterTypes, Object[] args)
			throws Exception
	{
		this.xid = xid;
		this.tsn = tsn;
		this.seq = seq;
		this.sn = sn;
		this.doTry = doTry;
		this.cannotCancel = !doTry; // 不需要执行try方法的，也不能cancel
		this.doConfirm = doConfirm;
		this.target = target;
		this.clazz = clazz;
		method = m.substring(PREFIX_TRY_LENGTH);
		this.parameterTypes = parameterTypes;
		this.args = args;
	}

	public Terminator(Map<String, String> map) throws Exception
	{
		this.clazz = map.get("clazz");
		this.method = map.get("method");
		this.xid = map.get("xid");
		this.sn = map.get("sn");
		this.seq = Integer.parseInt(map.get("seq"));
		String[] types = StringX.split(map.get("types"), ",");
		parameterTypes = new Class[types.length];
		for (int i = 0; i < types.length; i++)
			parameterTypes[i] = Class.forName(types[i]);
		args = (Object[]) FileUtil.deserialize(StringX.decodeBase64(map.get("args")), false);
		Class c = Class.forName(clazz, false, Thread.currentThread().getContextClassLoader());
		String[] beanNames = SpringUtil.APPCXT.getBeanNamesForType(c);
		target = SpringUtil.APPCXT.getBean(beanNames[0], c);
	}

	public Terminator(TccTerminatorPO po) throws Exception
	{
		this.clazz = po.getClazz();
		this.method = po.getMethod();
		this.xid = po.getXid();
		this.tsn = po.getTsn();
		this.sn = po.getSn();
		this.seq = po.getSeq();
		this.doConfirm = !"0".equals(po.getDoConfirm());
		this.doTry = !"0".equals(po.getDoTry());
		this.cannotCancel = "1".equals(po.getCannotCancel());
		this.status = po.getStatus();
		String[] types = StringX.split(po.getTypes(), ",");
		parameterTypes = new Class[types.length];
		for (int i = 0; i < types.length; i++)
			parameterTypes[i] = Class.forName(types[i]);
		byte[] buf = po.getArgs() == null ? null : po.getArgs().bytes();
		args = buf == null ? null : (Object[]) FileUtil.deserialize(buf, false);
		Class c = Class.forName(clazz, false, Thread.currentThread().getContextClassLoader());
		String[] beanNames = SpringUtil.APPCXT.getBeanNamesForType(c);
		target = SpringUtil.APPCXT.getBean(beanNames[0], c);
	}

	public void confirm() throws Exception
	{
		if (!doConfirm || status == Transaction.STATUS_CONFIRMED)
		{
			status = Transaction.STATUS_CONFIRMED;
			log.info("TX auto confirm:({}_{},{}), status:{}, confirm:{}", xid, seq, sn, status,
					doConfirm);
			return;
		}
		String m = PREFIX_CONFIRM + method;
		log.info("TX confirm:({}_{},{}), {}.{}", xid, seq, sn, target.getClass().getSimpleName(),
				m);
		try
		{
			target.getClass().getMethod(m, parameterTypes).invoke(target, args);
			status = Transaction.STATUS_CONFIRMED;
		}
		catch (InvocationTargetException e)
		{
			log.warn("TX confirm:({}_{},{}), {}.{}, ex:{}", xid, seq, sn,
					target.getClass().getSimpleName(), m, e.getTargetException().toString());
			throw (Exception) e.getTargetException();
		}
	}

	public void cancel() throws Exception
	{
		if (cannotCancel || status == Transaction.STATUS_CANCELED)
		{
			status = Transaction.STATUS_CANCELED;
			log.info("TX cannot cancel:({}_{},{}), status:{}, cannotCancel:{}", xid, seq, sn,
					status, cannotCancel);
			return;
		}
		String m = PREFIX_CANCEL + method;
		log.info("TX cancel:({}_{},{}), {}.{}", xid, seq, sn, target.getClass().getSimpleName(), m);
		try
		{
			target.getClass().getMethod(m, parameterTypes).invoke(target, args);
			status = Transaction.STATUS_CANCELED;
		}
		catch (InvocationTargetException e)
		{
			log.warn("TX cancel:({}_{},{}), {}.{}, ex:{}", xid, seq, sn,
					target.getClass().getSimpleName(), m, e.getTargetException().toString());
			throw (Exception) e.getTargetException();
		}
	}

	public String toJson()
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put("clazz", clazz);
		map.put("method", method);
		map.put("xid", xid);
		map.put("sn", sn);
		map.put("seq", String.valueOf(seq));
		StringBuffer buf = new StringBuffer();
		for (Class<?> c : parameterTypes)
			buf.append((buf.length() == 0 ? "" : ",") + c.getName());
		map.put("types", buf.toString());
		try
		{
			map.put("args", StringX.base64(FileUtil.serialize((java.io.Serializable) args, false)));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return new Gson().toJson(map);
	}

	public TccTerminatorPO toPO()
	{
		TccTerminatorPO po = new TccTerminatorPO();
		po.setClazz(clazz);
		po.setMethod(method);
		po.setXid(xid);
		po.setTsn(tsn);
		po.setSn(sn);
		po.setSeq(seq);
		po.setCreateTm(FastDateFormat.getInstance(Transaction.DF_ALL).format(new Date()));
		po.setDoConfirm(doConfirm ? "1" : "0");
		po.setDoTry(doTry ? "1" : "0");
		po.setCannotCancel(cannotCancel ? "1" : "0");
		po.setStatus(status);
		StringBuffer buf = new StringBuffer();
		for (Class<?> c : parameterTypes)
			buf.append((buf.length() == 0 ? "" : ",") + c.getName());
		po.setTypes(buf.toString());
		try
		{
			po.setArgs(new ByteArrayBlob(FileUtil.serialize((java.io.Serializable) args, false)));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return po;
	}

	public String toString()
	{
		return "\nTerm:" + clazz + "." + method + ":" + Arrays.toString(args);
	}
}
