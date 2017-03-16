package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

/**
 * genarated by sturdypine.chen Email: sturdypine@gmail.com description:
 */
@Entity
@Table(name = "sys_sql")
public class SQLPO implements Serializable
{
	public static final long serialVersionUID = 20150101L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String mdl; // 主键
	@Id
	@Column
	String id; // 主键
	@Column
	String resultClass; //
	@Column
	Integer type; //
	@Column
	String ds; //
	@Column
	String slave; //
	@Column
	String injection;

	@Column
	String paging; //
	@Column
	String startTag; //
	@Column
	String limitTag; //
	@Column
	String endTag; //

	@Column
	String firstRowOnly; //
	@Column
	String prepared; //
	@Column
	String auth; //
	@Column
	String proc; //
	@Column(columnDefinition = "{prepare:true}")
	String text; //
	@Column(columnDefinition = "{prepare:true}")
	String text1; //
	@Column(columnDefinition = "{prepare:true}")
	String text2; //
	@Column(columnDefinition = "{prepare:true}")
	String text3; //
	@Column
	String sys; //
	@Column(columnDefinition = "{prepare:true}")
	String remark; //

	public SQLPO()
	{
	}

	public SQLPO(String mdl, String id)
	{
		this.mdl = mdl;
		this.id = id;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.mdl = null;
		this.id = null;
		this.resultClass = null;
		this.type = null;
		this.ds = null;
		this.firstRowOnly = null;
		this.prepared = null;
		this.proc = null;
		this.text = null;
		this.remark = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof SQLPO)) return false;
		SQLPO obj = (SQLPO) o;
		if (!mdl.equals(obj.mdl)) return false;
		if (!id.equals(obj.id)) return false;
		if (!resultClass.equals(obj.resultClass)) return false;
		if (!type.equals(obj.type)) return false;
		if (!ds.equals(obj.ds)) return false;
		if (!firstRowOnly.equals(obj.firstRowOnly)) return false;
		if (!prepared.equals(obj.prepared)) return false;
		if (!proc.equals(obj.proc)) return false;
		if (!text.equals(obj.text)) return false;
		if (!remark.equals(obj.remark)) return false;
		return true;
	}

	// 只对主键进行散列
	public int hashCode()
	{
		long hashCode = getClass().hashCode();
		if (mdl != null) hashCode += mdl.hashCode();
		if (id != null) hashCode += id.hashCode();
		return (int) hashCode;
	}

	public int compareTo(Object o)
	{
		return -1;
	}

	public String getMdl()
	{
		return mdl;
	}

	public void setMdl(String mdl)
	{
		this.mdl = mdl;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getResultClass()
	{
		return resultClass;
	}

	public void setResultClass(String resultClass)
	{
		this.resultClass = resultClass;
	}

	public Integer getType()
	{
		return type;
	}

	public void setType(Integer type)
	{
		this.type = type;
	}

	public String getDs()
	{
		return ds;
	}

	public void setDs(String ds)
	{
		this.ds = ds;
	}

	public String getSlave()
	{
		return slave;
	}

	public void setSlave(String slave)
	{
		this.slave = slave;
	}

	public String getInjection()
	{
		return injection;
	}

	public void setInjection(String injection)
	{
		this.injection = injection;
	}

	public String getPaging()
	{
		return paging;
	}

	public void setPaging(String paging)
	{
		this.paging = paging;
	}

	public String getStartTag()
	{
		return startTag;
	}

	public void setStartTag(String startTag)
	{
		this.startTag = startTag;
	}

	public String getLimitTag()
	{
		return limitTag;
	}

	public void setLimitTag(String limitTag)
	{
		this.limitTag = limitTag;
	}

	public String getEndTag()
	{
		return endTag;
	}

	public void setEndTag(String endTag)
	{
		this.endTag = endTag;
	}

	public String getFirstRowOnly()
	{
		return firstRowOnly;
	}

	public void setFirstRowOnly(String firstRowOnly)
	{
		this.firstRowOnly = firstRowOnly;
	}

	public String getPrepared()
	{
		return prepared;
	}

	public void setPrepared(String prepared)
	{
		this.prepared = prepared;
	}

	public String getProc()
	{
		return proc;
	}

	public void setProc(String proc)
	{
		this.proc = proc;
	}

	public String getAuth()
	{
		return auth;
	}

	public void setAuth(String auth)
	{
		this.auth = auth;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getText1()
	{
		return text1;
	}

	public void setText1(String text1)
	{
		this.text1 = text1;
	}

	public String getText2()
	{
		return text2;
	}

	public void setText2(String text2)
	{
		this.text2 = text2;
	}

	public String getText3()
	{
		return text3;
	}

	public void setText3(String text3)
	{
		this.text3 = text3;
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

	public void set(SQLPO vo)
	{
		this.mdl = vo.mdl;
		this.id = vo.id;
		this.resultClass = vo.resultClass;
		this.type = vo.type;
		this.ds = vo.ds;
		this.firstRowOnly = vo.firstRowOnly;
		this.prepared = vo.prepared;
		this.proc = vo.proc;
		this.text = vo.text;
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
		SQLPO obj = new SQLPO();
		obj.set(this);
		return obj;
	}
}
