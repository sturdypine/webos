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
@Table(name = "sys_dict")
public class DictPO implements Serializable
{
	public static final long serialVersionUID = 20120925L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String code; // 主键
	@Id
	@Column
	String dtype; // 主键
	@Column
	String name; //
	@Column
	String dlevel; //
	@Column
	String parentcode; //
	@Column
	Integer dorder; //
	@Column
	String sys; //

	public DictPO()
	{
	}

	public DictPO(String code, String dtype)
	{
		this.code = code;
		this.dtype = dtype;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.code = null;
		this.dtype = null;
		this.name = null;
		this.dlevel = null;
		this.parentcode = null;
		this.dorder = null;
		this.sys = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof DictPO)) return false;
		DictPO obj = (DictPO) o;
		if (!code.equals(obj.code)) return false;
		if (!dtype.equals(obj.dtype)) return false;
		if (!name.equals(obj.name)) return false;
		if (!dlevel.equals(obj.dlevel)) return false;
		if (!parentcode.equals(obj.parentcode)) return false;
		if (!dorder.equals(obj.dorder)) return false;
		if (!sys.equals(obj.sys)) return false;

		return true;
	}

	// 只对主键进行散列
	public int hashCode()
	{
		long hashCode = getClass().hashCode();
		if (code != null) hashCode += code.hashCode();
		if (dtype != null) hashCode += dtype.hashCode();
		return (int) hashCode;
	}

	public int compareTo(Object o)
	{
		return -1;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getDtype()
	{
		return dtype;
	}

	public void setDtype(String dtype)
	{
		this.dtype = dtype;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDlevel()
	{
		return dlevel;
	}

	public void setDlevel(String dlevel)
	{
		this.dlevel = dlevel;
	}

	public String getParentcode()
	{
		return parentcode;
	}

	public void setParentcode(String parentcode)
	{
		this.parentcode = parentcode;
	}

	public Integer getDorder()
	{
		return dorder;
	}

	public void setDorder(Integer dorder)
	{
		this.dorder = dorder;
	}

	public String getSys()
	{
		return sys;
	}

	public void setSys(String sys)
	{
		this.sys = sys;
	}

	public void set(DictPO vo)
	{
		this.code = vo.code;
		this.dtype = vo.dtype;
		this.name = vo.name;
		this.dlevel = vo.dlevel;
		this.parentcode = vo.parentcode;
		this.dorder = vo.dorder;
		this.sys = vo.sys;
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
		DictPO obj = new DictPO();
		obj.set(this);
		return obj;
	}
}
