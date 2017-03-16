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
@Table(name = "sys_retcode")
public class RetCodePO implements Serializable
{
	public static final long serialVersionUID = 20110712L;
	@Id
	@Column
	String retcd;
	@Column(columnDefinition = "{prepare:true}")
	String text; //
	@Column
	String action; //

	public RetCodePO()
	{
	}

	public RetCodePO(String retcd)
	{
		this.retcd = retcd;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.retcd = null;
		this.text = null;
		this.action = null;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof RetCodePO)) return false;
		RetCodePO obj = (RetCodePO) o;
		if (!retcd.equals(obj.retcd)) return false;
		if (!text.equals(obj.text)) return false;
		if (!action.equals(obj.action)) return false;

		return true;
	}

	public int hashCode()
	{
		long hashCode = getClass().hashCode();
		if (retcd != null) hashCode += retcd.hashCode();
		return (int) hashCode;
	}

	public int compareTo(Object o)
	{
		return -1;
	}

	// set all properties to default value...
	public void init()
	{
	}

	public String getRetcd()
	{
		return retcd;
	}

	public void setRetcd(String retcd)
	{
		this.retcd = retcd;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
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
}
