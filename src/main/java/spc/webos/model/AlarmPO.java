package spc.webos.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sys_alarm")
public class AlarmPO implements java.io.Serializable
{
	public static final long serialVersionUID = 20141229L;
	@Id
	@Column
	String id;
	@Column
	String sqlId; //
	@Column
	String ftlId; //
	@Column
	String service; //
	@Column
	String msgFtlId;
	@Column
	String msgQ;
	@Column(columnDefinition = "{prepare:true}")
	String param; //
	@Column(columnDefinition = "{prepare:true}")
	String checkRule; //
	@Column
	Integer checkInterval; //
	@Column
	Long lastCheckSec; //
	@Column
	String lastCheckTm; //
	@Column
	String lastAlarmTm; //
	@Column(columnDefinition = "{prepare:true}")
	String lastAlarmMsg; //
	@Column
	String phone; //
	@Column
	String status; //
	@Column
	String sys;
	@Column(columnDefinition = "{prepare:true}")
	String remark; //

	String userCd;
	String lastUpdTm;
	String verDt;
	String verStatus;
	String actionNm;

	public AlarmPO()
	{
	}

	public AlarmPO(String id)
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

	public String getSqlId()
	{
		return sqlId;
	}

	public void setSqlId(String sqlId)
	{
		this.sqlId = sqlId;
	}

	public String getFtlId()
	{
		return ftlId;
	}

	public void setFtlId(String ftlId)
	{
		this.ftlId = ftlId;
	}

	public String getService()
	{
		return service;
	}

	public void setService(String service)
	{
		this.service = service;
	}

	public String getParam()
	{
		return param;
	}

	public void setParam(String param)
	{
		this.param = param;
	}

	public String getCheckRule()
	{
		return checkRule;
	}

	public void setCheckRule(String checkRule)
	{
		this.checkRule = checkRule;
	}

	public Integer getCheckInterval()
	{
		return checkInterval;
	}

	public void setCheckInterval(Integer checkInterval)
	{
		this.checkInterval = checkInterval;
	}

	public Long getLastCheckSec()
	{
		return lastCheckSec;
	}

	public void setLastCheckSec(Long lastCheckSec)
	{
		this.lastCheckSec = lastCheckSec;
	}

	public String getMsgFtlId()
	{
		return msgFtlId;
	}

	public void setMsgFtlId(String msgFtlId)
	{
		this.msgFtlId = msgFtlId;
	}

	public String getMsgQ()
	{
		return msgQ;
	}

	public void setMsgQ(String msgQ)
	{
		this.msgQ = msgQ;
	}

	public String getLastCheckTm()
	{
		return lastCheckTm;
	}

	public void setLastCheckTm(String lastCheckTm)
	{
		this.lastCheckTm = lastCheckTm;
	}

	public String getLastAlarmTm()
	{
		return lastAlarmTm;
	}

	public void setLastAlarmTm(String lastAlarmTm)
	{
		this.lastAlarmTm = lastAlarmTm;
	}

	public String getLastAlarmMsg()
	{
		return lastAlarmMsg;
	}

	public void setLastAlarmMsg(String lastAlarmMsg)
	{
		this.lastAlarmMsg = lastAlarmMsg;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
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

	public String getUserCd()
	{
		return userCd;
	}

	public void setUserCd(String userCd)
	{
		this.userCd = userCd;
	}

	public String getLastUpdTm()
	{
		return lastUpdTm;
	}

	public void setLastUpdTm(String lastUpdTm)
	{
		this.lastUpdTm = lastUpdTm;
	}

	public String getVerDt()
	{
		return verDt;
	}

	public void setVerDt(String verDt)
	{
		this.verDt = verDt;
	}

	public String getVerStatus()
	{
		return verStatus;
	}

	public void setVerStatus(String verStatus)
	{
		this.verStatus = verStatus;
	}

	public String getActionNm()
	{
		return actionNm;
	}

	public void setActionNm(String actionNm)
	{
		this.actionNm = actionNm;
	}
}
