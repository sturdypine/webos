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
@Table(name = "sys_config")
public class ConfigPO implements Serializable
{
	public static final long serialVersionUID = 20080121L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String code; // 主键

	@Column
	String val; //
	@Column
	String dtype; //
	@Column
	String status; //
	@Column
	String model; //
	@Column
	String remark; //
	Long seq; //

	public final static String TP_INT = "int";
	public final static String TP_LONG = "long";
	public final static String TP_BOOL = "bool";
	public final static String TP_BOOLEAN = "boolean";
	// public final static String TP_FLOAT = "float";
	public final static String TP_DOUBLE = "double";
	public final static String TP_STRING = "string";

	public final static String MD_ARRAY = "array";
	public final static String MD_SIMPLE = "simple";
	public final static String MD_JSON = "json";

	public ConfigPO()
	{
	}

	public ConfigPO(String code)
	{
		this.code = code;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getVal()
	{
		return val;
	}

	public void setVal(String val)
	{
		this.val = val;
	}

	public String getDtype()
	{
		return dtype;
	}

	public void setDtype(String dtype)
	{
		this.dtype = dtype;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel(String model)
	{
		this.model = model;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public Long getSeq()
	{
		return seq;
	}

	public void setSeq(Long seq)
	{
		this.seq = seq;
	}
}
