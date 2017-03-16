package spc.webos.tcc.repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.service.seq.UUID;
import spc.webos.service.seq.impl.TimeMillisUUID;
import spc.webos.tcc.Repository;
import spc.webos.tcc.TCCTransactional;
import spc.webos.tcc.Terminator;
import spc.webos.tcc.Transaction;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public abstract class AbstractTCCRepository implements Repository
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected int sucTccExpireSeconds = 0; // 24 * 3600; // 过期事物保存时间
	protected boolean repositoryOnlyErr = false;

	protected String group; // tcc事务组
	protected int instanceId; // 每一个tcc jvm实例
	protected UUID uuid;

	public String createXid()
	{
		return group + "-" + uuid.format(uuid.uuid());
	}

	@PostConstruct
	public void init() throws Exception
	{
		if (uuid == null) uuid = new TimeMillisUUID(instanceId);
		if (StringX.nullity(group)) group = SpringUtil.APPCODE;
	}

	public String group()
	{
		return group;
	}

	public Terminator addTerminator(Transaction transaction, TCCTransactional tcc, String tid,
			int seq, Object target, String clazz, String m, Class[] parameterTypes, Object[] args)
					throws Exception
	{
		Terminator t = new Terminator(transaction.xid, transaction.sn, seq, tid, tcc.doTry(),
				tcc.doConfirm(), target, clazz, m, parameterTypes, args);
		addTerminator(transaction, t);
		updateStatus(transaction); // 将子服务信息进行保存
		return t;
	}

	protected void addTerminator(Transaction transaction, Terminator t)
	{
		transaction.getTerminatorsList().add(t);
	}

	public Terminator updateStatus(Terminator terminator)
	{
		return terminator;
	}

	@PreDestroy
	public void destroy()
	{
	}

	public void setSucTccExpireSeconds(int sucTccExpireSeconds)
	{
		this.sucTccExpireSeconds = sucTccExpireSeconds;
	}

	public boolean isRepositoryOnlyErr()
	{
		return repositoryOnlyErr;
	}

	public void setRepositoryOnlyErr(boolean repositoryOnlyErr)
	{
		this.repositoryOnlyErr = repositoryOnlyErr;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public void setInstanceId(int instanceId)
	{
		this.instanceId = instanceId;
	}

	public void setUuid(UUID uuid)
	{
		this.uuid = uuid;
	}
}
