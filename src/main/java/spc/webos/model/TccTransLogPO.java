package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.tcc.service.TCC;
import spc.webos.util.StringX;

@Entity
@Table(name = "tcc_translog")
public class TccTransLogPO implements TCC, Serializable
{
	public static final long serialVersionUID = 20160905L;
	// 和物理表对应字段
	@Id
	@Column
	protected String sn;
	@Column
	protected Integer status;
	@Column
	protected String tryTm;
	@Column
	protected String confirmTm;
	@Column
	protected String cancelTm;
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

	public TccTransLogPO()
	{
	}

	public TccTransLogPO(String sn)
	{
		this.sn = sn;
	}

	@Override
	public String getSn()
	{
		return sn;
	}

	public void setSn(String sn)
	{
		this.sn = sn;
	}

	@Override
	public Integer getStatus()
	{
		return status;
	}

	@Override
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

	public String getTryTm()
	{
		return tryTm;
	}

	public void setTryTm(String tryTm)
	{
		this.tryTm = tryTm;
	}

	public String getConfirmTm()
	{
		return confirmTm;
	}

	public void setConfirmTm(String confirmTm)
	{
		this.confirmTm = confirmTm;
	}

	public String getCancelTm()
	{
		return cancelTm;
	}

	public void setCancelTm(String cancelTm)
	{
		this.cancelTm = cancelTm;
	}
}
