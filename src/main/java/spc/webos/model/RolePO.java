package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * genarated by sturdypine.chen Email: sturdypine@gmail.com description:
 */
@Entity
@Table(name = "sys_role")
public class RolePO implements Serializable
{
	public static final long serialVersionUID = 20100608L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String id; // 主键
	@Column
	String name; //
	String orgId;
	String depId;
	@Column
	String menu; //
	@Column
	String remark; //

	public RolePO()
	{
	}

	public RolePO(String id)
	{
		this.id = id;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.id = null;
		this.name = null;
		this.menu = null;
		this.remark = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof RolePO)) return false;
		RolePO obj = (RolePO) o;
		if (!id.equals(obj.id)) return false;
		if (!name.equals(obj.name)) return false;
		if (!menu.equals(obj.menu)) return false;
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMenu()
	{
		return menu;
	}

	public void setMenu(String menu)
	{
		this.menu = menu;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public void set(RolePO vo)
	{
		this.id = vo.id;
		this.name = vo.name;
		this.menu = vo.menu;
		this.remark = vo.remark;
	}

	public StringBuffer toJson()
	{
		StringBuffer buf = new StringBuffer();
		buf.append('{');
		if (id != null)
		{
			if (buf.length() > 2) buf.append(',');
			buf.append("id:'");
			buf.append(id);
			buf.append('\'');
		}
		if (name != null)
		{
			if (buf.length() > 2) buf.append(',');
			buf.append("name:'");
			buf.append(name);
			buf.append('\'');
		}
		if (menu != null)
		{
			if (buf.length() > 2) buf.append(',');
			buf.append("menu:'");
			buf.append(menu);
			buf.append('\'');
		}
		if (remark != null)
		{
			if (buf.length() > 2) buf.append(',');
			buf.append("remark:'");
			buf.append(remark);
			buf.append('\'');
		}
		buf.append('}');
		return buf;
	}

	public String getOrgId()
	{
		return orgId;
	}

	public void setOrgId(String orgId)
	{
		this.orgId = orgId;
	}

	public String getDepId()
	{
		return depId;
	}

	public void setDepId(String depId)
	{
		this.depId = depId;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer(128);
		buf.append(getClass().getName() + "(serialVersionUID=" + serialVersionUID + "):");
		buf.append(toJson());
		return buf.toString();
	}
}
