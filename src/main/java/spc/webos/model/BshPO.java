package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.webos.util.JsonUtil;

/**
 * genarated by sturdypine.chen Email: sturdypine@gmail.com description:
 */
@Entity
@Table(name = "sys_bsh")
public class BshPO implements Serializable
{
	public static final long serialVersionUID = 20150101L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String id; // 主键
	@Column
	String bsh; //
	@Column
	String sys; //
	@Column(columnDefinition = "{prepare:true}")
	String remark; //

	public BshPO()
	{
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.id = null;
		this.bsh = null;
		this.remark = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof BshPO)) return false;
		BshPO obj = (BshPO) o;
		if (!id.equals(obj.id)) return false;
		if (!bsh.equals(obj.bsh)) return false;
		if (!remark.equals(obj.remark)) return false;
		return true;
	}

	// 只对主键进行散列
	public int hashCode()
	{
		long hashCode = getClass().hashCode();
		if (id != null) hashCode += id.hashCode();
		return (int) hashCode;
	}

	public int compareTo(Object o)
	{
		return -1;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getBsh()
	{
		return bsh;
	}

	public void setBsh(String bsh)
	{
		this.bsh = bsh;
	}

	public String getSys()
	{
		return sys;
	}

	public void setSys(String sys)
	{
		this.sys = sys;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public void set(BshPO vo)
	{
		this.id = vo.id;
		this.bsh = vo.bsh;
		this.remark = vo.remark;
	}

	public StringBuffer toJson()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(JsonUtil.obj2json(this));
		return buf;
	}

	public void afterLoad()
	{
		// TODO Auto-generated method stub
	}

	public void beforeLoad()
	{
		// TODO Auto-generated method stub
	}

	// public void setManualSeq(Long seq)
	// {
	//
	// }

	public void destory()
	{

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
		BshPO obj = new BshPO();
		obj.set(this);
		return obj;
	}
}
