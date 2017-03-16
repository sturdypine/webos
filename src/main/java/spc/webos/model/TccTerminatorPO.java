package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.util.StringX;

@Entity
@Table(name = "tcc_terminator")
public class TccTerminatorPO implements Serializable
{
	public static final long serialVersionUID = 20160901L;

	@Id
	@Column
	protected String xid;
	@Id
	@Column
	protected Integer seq;
	@Column
	protected String tsn;
	@Column
	protected String sn;
	@Column
	protected String doTry;
	@Column
	protected String doConfirm;
	@Column
	protected String cannotCancel;
	@Column
	protected Integer status;
	@Column
	protected String clazz;
	@Column
	protected String method;
	@Column
	protected String types;
	@Column(columnDefinition = "{prepare:true}")
	protected ByteArrayBlob args;
	@Column
	protected String argsMD5;
	@Column
	protected String createTm;

	public TccTerminatorPO()
	{
	}

	public TccTerminatorPO(String xid, Integer seq)
	{
		this.xid = xid;
		this.seq = seq;
	}

	public String getXid()
	{
		return xid;
	}

	public void setXid(String xid)
	{
		this.xid = xid;
	}

	public Integer getSeq()
	{
		return seq;
	}

	public void setSeq(Integer seq)
	{
		this.seq = seq;
	}

	public String getTsn()
	{
		return tsn;
	}

	public void setTsn(String tsn)
	{
		this.tsn = tsn;
	}

	public String getSn()
	{
		return sn;
	}

	public void setSn(String sn)
	{
		this.sn = sn;
	}

	public String getDoTry()
	{
		return doTry;
	}

	public void setDoTry(String doTry)
	{
		this.doTry = doTry;
	}

	public String getDoConfirm()
	{
		return doConfirm;
	}

	public void setDoConfirm(String doConfirm)
	{
		this.doConfirm = doConfirm;
	}

	public String getCannotCancel()
	{
		return cannotCancel;
	}

	public void setCannotCancel(String cannotCancel)
	{
		this.cannotCancel = cannotCancel;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
	}

	public String getClazz()
	{
		return clazz;
	}

	public void setClazz(String clazz)
	{
		this.clazz = clazz;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getTypes()
	{
		return types;
	}

	public void setTypes(String types)
	{
		this.types = types;
	}

	public ByteArrayBlob getArgs()
	{
		return args;
	}

	public void setArgs(ByteArrayBlob args)
	{
		this.args = args;
		if (args != null) this.argsMD5 = StringX.md5(args.bytes());
	}

	public String getArgsMD5()
	{
		return argsMD5;
	}

	public void setArgsMD5(String argsMD5)
	{
		this.argsMD5 = argsMD5;
	}

	public String getCreateTm()
	{
		return createTm;
	}

	public void setCreateTm(String createTm)
	{
		this.createTm = createTm;
	}
}
