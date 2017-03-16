package spc.webos.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.util.StringX;

@Entity
@Table(name = "tcc_transaction")
public class TccTransactionPO implements Serializable
{
	public static final long serialVersionUID = 20160901L;

	@Id
	@Column
	protected String xid;
	@Column
	protected String gname;
	@Column
	protected String sn;
	@Column
	protected Integer status;
	@Column
	protected String app;
	@Column
	protected String jvm;

	@Column
	protected Integer exSeq;
	@Column
	protected String exSn;
	@Column(columnDefinition = "{prepare:true}")
	protected String ex;

	@Column
	protected String proxy;
	@Column(columnDefinition = "{prepare:true}")
	protected ByteArrayBlob args;
	@Column
	protected String argsMD5;
	@Column
	protected String createTm;
	@Column
	protected String lastStatusTm;

	@OneToMany
	@JoinColumn(name = "xid")
	protected List<TccTerminatorPO> terminators;

	public TccTransactionPO()
	{
	}

	public TccTransactionPO(String xid)
	{
		this.xid = xid;
	}

	public String getXid()
	{
		return xid;
	}

	public void setXid(String xid)
	{
		this.xid = xid;
	}

	public String getGname()
	{
		return gname;
	}

	public void setGname(String gname)
	{
		this.gname = gname;
	}

	public String getSn()
	{
		return sn;
	}

	public void setSn(String sn)
	{
		this.sn = sn;
	}

	public String getApp()
	{
		return app;
	}

	public void setApp(String app)
	{
		this.app = app;
	}

	public String getJvm()
	{
		return jvm;
	}

	public void setJvm(String jvm)
	{
		this.jvm = jvm;
	}

	public Integer getExSeq()
	{
		return exSeq;
	}

	public void setExSeq(Integer exSeq)
	{
		this.exSeq = exSeq;
	}

	public String getExSn()
	{
		return exSn;
	}

	public void setExSn(String exSn)
	{
		this.exSn = exSn;
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

	public String getCreateTm()
	{
		return createTm;
	}

	public void setCreateTm(String createTm)
	{
		this.createTm = createTm;
	}

	public String getLastStatusTm()
	{
		return lastStatusTm;
	}

	public void setLastStatusTm(String lastStatusTm)
	{
		this.lastStatusTm = lastStatusTm;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
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

	public List<TccTerminatorPO> getTerminators()
	{
		return terminators;
	}

	public void setTerminators(List<TccTerminatorPO> terminators)
	{
		this.terminators = terminators;
	}
}
