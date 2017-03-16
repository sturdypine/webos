package spc.webos.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_user")
public class UserPO implements Serializable
{
	public static final long serialVersionUID = 20100608L;
	// 和物理表对应字段的属性
	@Id
	@Column
	String code; // 主键
	String realCode; //
	@Column
	String name; //
	@Column
	String pwd; //
	@Column
	String pwdSalt; //
	@Column
	String roleId; //
	@Column
	String orgId;

	@Column
	String ipAddress; //
	@Column
	String email; //
	@Column
	String autoruns;

	public UserPO()
	{
	}

	public UserPO(String code)
	{
		this.code = code;
	}

	// set all properties to NULL
	public void setNULL()
	{
		this.code = null;
		this.name = null;
		this.pwd = null;
		this.roleId = null;
		this.ipAddress = null;
		this.email = null;
		this.autoruns = null;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPwd()
	{
		return pwd;
	}

	public void setPwd(String pwd)
	{
		this.pwd = pwd;
	}

	public String getPwdSalt()
	{
		return pwdSalt;
	}

	public void setPwdSalt(String pwdSalt)
	{
		this.pwdSalt = pwdSalt;
	}

	public String getRoleId()
	{
		return roleId;
	}

	public void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getAutoruns()
	{
		return autoruns;
	}

	public void setAutoruns(String autoruns)
	{
		this.autoruns = autoruns;
	}

	public String getOrgId()
	{
		return orgId;
	}

	public void setOrgId(String orgId)
	{
		this.orgId = orgId;
	}

	public String getRealCode()
	{
		return realCode;
	}

	public void setRealCode(String realCode)
	{
		this.realCode = realCode;
	}
}
