package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import spc.webos.util.tree.ITreeNodeValue;

@Entity
@Table(name = "sys_menu")
public class MenuPO implements Serializable, ITreeNodeValue
{
	public static final long serialVersionUID = 20160101L;
	@Id
	@Column
	String mid;
	@Column
	String text;
	@Column
	String parentId;
	@Column
	String win;
	@Column
	String js;
	@Column
	String config;
	@Column
	String mmenu;
	@Column
	Integer morder;
	@Column
	String service;
	@Column
	String sqlId;

	public MenuPO()
	{
	}

	public MenuPO(String mid)
	{
		this.mid = mid;
	}

	public String getMid()
	{
		return mid;
	}

	public void setMid(String mid)
	{
		this.mid = mid;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String parentTreeId()
	{
		return parentId;
	}

	public void setParentId(String parentId)
	{
		this.parentId = parentId;
	}

	public String getWin()
	{
		return win;
	}

	public void setWin(String win)
	{
		this.win = win;
	}

	public String getJs()
	{
		return js;
	}

	public void setJs(String js)
	{
		this.js = js;
	}

	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	public String getMmenu()
	{
		return mmenu;
	}

	public void setMmenu(String mmenu)
	{
		this.mmenu = mmenu;
	}

	public Integer getMorder()
	{
		return morder;
	}

	public void setMorder(Integer morder)
	{
		this.morder = morder;
	}

	public String getService()
	{
		return service;
	}

	public void setService(String service)
	{
		this.service = service;
	}

	public String getSqlId()
	{
		return sqlId;
	}

	public void setSqlId(String sqlId)
	{
		this.sqlId = sqlId;
	}

	public String getParentId()
	{
		return parentId;
	}

	@Override
	public String treeText()
	{
		return text;
	}

	@Override
	public boolean treeRoot()
	{
		return mid.equals("00");
	}

	@Override
	public Object treeId()
	{
		return mid;
	}
}
