package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_js")
public class JsPO implements Serializable
{
	public static final long serialVersionUID = 20141213L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String id; // 主键
	@Column(columnDefinition = "{prepare:true}")
	String js; //
	@Column(columnDefinition = "{prepare:true}")
	String js1; //
	@Column(columnDefinition = "{prepare:true}")
	String js2; //
	@Column(columnDefinition = "{prepare:true}")
	String js3; //
	@Column(columnDefinition = "{prepare:true}")
	String remark; //

	public JsPO()
	{
	}

	public JsPO(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getJs()
	{
		return js;
	}

	public void setJs(String js)
	{
		this.js = js;
	}

	public String getJs1()
	{
		return js1;
	}

	public void setJs1(String js1)
	{
		this.js1 = js1;
	}

	public String getJs2()
	{
		return js2;
	}

	public void setJs2(String js2)
	{
		this.js2 = js2;
	}

	public String getJs3()
	{
		return js3;
	}

	public void setJs3(String js3)
	{
		this.js3 = js3;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}
}
