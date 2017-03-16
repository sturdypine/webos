package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * genarated by sturdypine.chen Email: sturdypine@gmail.com description:
 */
@Entity
@Table(name = "sys_seqno")
public class SeqNoPO implements Serializable
{
	public static final long serialVersionUID = 20151205L;
	@Id
	@Column
	String name;
	@Column
	public Long seqNo; //
	@Column
	public Long maxSeqNo; //
	@Column
	public Long recycleNo;
	@Column
	public Integer batchSize; //
	@Version
	@Column
	Integer ver; //
	@Column(columnDefinition = "{prepare:true}")
	String remark; //

	public SeqNoPO()
	{
	}

	public SeqNoPO(String name)
	{
		this.name = name;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.name = null;
		this.seqNo = null;
		this.batchSize = null;
		this.ver = null;
		this.remark = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof SeqNoPO)) return false;
		SeqNoPO obj = (SeqNoPO) o;
		if (!name.equals(obj.name)) return false;
		if (!seqNo.equals(obj.seqNo)) return false;
		if (!batchSize.equals(obj.batchSize)) return false;
		if (!ver.equals(obj.ver)) return false;
		if (!remark.equals(obj.remark)) return false;
		return true;
	}

	public int compareTo(Object o)
	{
		return -1;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Long getSeqNo()
	{
		return seqNo;
	}

	public void setSeqNo(Long seqNo)
	{
		this.seqNo = seqNo;
	}

	public Integer getBatchSize()
	{
		return batchSize;
	}

	public void setBatchSize(Integer batchSize)
	{
		this.batchSize = batchSize;
	}

	public Integer getVer()
	{
		return ver;
	}

	public void setVer(Integer ver)
	{
		this.ver = ver;
	}

	public Long getRecycleNo()
	{
		return recycleNo;
	}

	public void setRecycleNo(Long recycleNo)
	{
		this.recycleNo = recycleNo;
	}

	public Long getMaxSeqNo()
	{
		return maxSeqNo;
	}

	public void setMaxSeqNo(Long maxSeqNo)
	{
		this.maxSeqNo = maxSeqNo;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public void set(SeqNoPO vo)
	{
		this.name = vo.name;
		this.seqNo = vo.seqNo;
		this.batchSize = vo.batchSize;
		this.ver = vo.ver;
		this.remark = vo.remark;
	}

	public StringBuffer toJson()
	{
		StringBuffer buf = new StringBuffer();
		try
		{
			buf.append(spc.webos.util.JsonUtil.obj2json(this));
		}
		catch (Exception e)
		{
		}
		return buf;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer(128);
		buf.append(getClass().getName() + "(serialVersionUID=" + serialVersionUID + "):");
		buf.append(toJson());
		return buf.toString();
	}

	public Object clone()
	{
		SeqNoPO obj = new SeqNoPO();
		obj.set(this);
		return obj;
	}
}
