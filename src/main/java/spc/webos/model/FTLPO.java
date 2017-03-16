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
@Table(name = "sys_ftl")
public class FTLPO implements Serializable
{
	public static final long serialVersionUID = 20141213L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String id; // 主键
	@Column(columnDefinition = "{prepare:true}")
	String ftl; //
	@Column(columnDefinition = "{prepare:true}")
	String ftl1; //
	@Column(columnDefinition = "{prepare:true}")
	String ftl2; //
	@Column(columnDefinition = "{prepare:true}")
	String ftl3; //
	@Column
	String sys; //
	@Column(columnDefinition = "{prepare:true}")
	String remark; //

	public FTLPO()
	{
	}

	public FTLPO(String id)
	{
		this.id = id;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.id = null;
		this.ftl = null;
		this.remark = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof FTLPO)) return false;
		FTLPO obj = (FTLPO) o;
		if (!id.equals(obj.id)) return false;
		if (!ftl.equals(obj.ftl)) return false;
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

	public String getFtl()
	{
		return ftl;
	}

	public void setFtl(String ftl)
	{
		this.ftl = ftl;
	}

	public String getFtl1()
	{
		return ftl1;
	}

	public void setFtl1(String ftl1)
	{
		this.ftl1 = ftl1;
	}

	public String getFtl2()
	{
		return ftl2;
	}

	public void setFtl2(String ftl2)
	{
		this.ftl2 = ftl2;
	}

	public String getFtl3()
	{
		return ftl3;
	}

	public void setFtl3(String ftl3)
	{
		this.ftl3 = ftl3;
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

	public void set(FTLPO vo)
	{
		this.id = vo.id;
		this.ftl = vo.ftl;
		this.remark = vo.remark;
	}

	public StringBuffer toJson()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(JsonUtil.obj2json(this));
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
		FTLPO obj = new FTLPO();
		obj.set(this);
		return obj;
	}
}
