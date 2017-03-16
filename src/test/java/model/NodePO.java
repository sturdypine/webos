package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "esb_node")
public class NodePO {
	public static final long serialVersionUID = 20160120L;
	// 和物理表对应字段的属性
	@Id
	@Column
	protected String appCd; // 主键
	@Id
	@Column
	protected String mbrCd; // 主键
	@Column
	protected String name; //
	@Column
	protected String appAttr; //
	@Column
	protected String sigBeanId; //
	@Column
	protected String sigCntBeanId; //
	@Column
	protected String host; //
	@Column
	protected String localPort; //
	@Column
	protected String accessType; //
	@Column
	protected String faBeanId; //
	@Column
	protected String baBeanId; //
	@Column
	protected String publicKey; //
	@Column
	protected String desKey; //
	@Column
	protected String echoMsgCd; //
	@Column
	protected String publishDt; //
	@Column
	protected String effectDt; //
	@Column
	protected String location; //
	@Column
	protected String ext1; //
	@Column
	protected String ext2; //
	@Column
	protected String ext3; //
	@Column
	protected String synResponse2QM; //
	@Column
	protected String authMsgCd; //
	@Column
	protected String qmgr; //
	@Column
	protected String httpURI; //
	@Column
	protected String remark; //
	@Column
	protected Integer locationNo; //
	@Column
	protected Integer slaMaxCon; //
	@Column
	protected Integer slaAvgCost; //
	@Column
	protected Integer slaMaxCost; //
	@Column
	protected Double slaSucRatio; //
	@Column
	protected Integer slaRecovery; //
	@Column
	protected String userCd; //
	@Column
	protected String lastUpdTm; //
	@Column
	protected String verDt; //
	@Column
	protected String verStatus; //
	@Column
	protected String actionNm; //

	// 和此VO相关联的其他VO属性

	// 和此VO相关联的其他简单Sql属性
	// Note: 如果关联的Sql对象为String, Inegter...等Java final class时， 只能使用Object对象，
	// 访问时候只能通过
	// Object的toString()方法来使用。

	public NodePO() {
	}

	public NodePO(String appCd, String mbrCd) {
		this.appCd = appCd;
		this.mbrCd = mbrCd;
	}

	public String primary() {
		String delim = "#";
		StringBuffer buf = new StringBuffer();
		buf.append(this.appCd);
		buf.append(delim + this.mbrCd);
		return buf.toString();
	}

	public String table() {
		return "esb_node";
	}

	public String[] blobFields() {
		return null;
	}

	// set all properties to NULL
	public void setNULL() {
		this.appCd = null;
		this.mbrCd = null;
		this.name = null;
		this.appAttr = null;
		this.sigBeanId = null;
		this.sigCntBeanId = null;
		this.host = null;
		this.localPort = null;
		this.accessType = null;
		this.faBeanId = null;
		this.baBeanId = null;
		this.publicKey = null;
		this.desKey = null;
		this.echoMsgCd = null;
		this.publishDt = null;
		this.effectDt = null;
		this.location = null;
		this.ext1 = null;
		this.ext2 = null;
		this.ext3 = null;
		this.synResponse2QM = null;
		this.authMsgCd = null;
		this.qmgr = null;
		this.httpURI = null;
		this.remark = null;
		this.locationNo = null;
		this.slaMaxCon = null;
		this.slaAvgCost = null;
		this.slaMaxCost = null;
		this.slaSucRatio = null;
		this.slaRecovery = null;
		this.userCd = null;
		this.lastUpdTm = null;
		this.verDt = null;
		this.verStatus = null;
		this.actionNm = null;
	}

	public void setRefNull() {
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof NodePO))
			return false;
		NodePO obj = (NodePO) o;
		if (!appCd.equals(obj.appCd))
			return false;
		if (!mbrCd.equals(obj.mbrCd))
			return false;
		if (!name.equals(obj.name))
			return false;
		if (!appAttr.equals(obj.appAttr))
			return false;
		if (!sigBeanId.equals(obj.sigBeanId))
			return false;
		if (!sigCntBeanId.equals(obj.sigCntBeanId))
			return false;
		if (!host.equals(obj.host))
			return false;
		if (!localPort.equals(obj.localPort))
			return false;
		if (!accessType.equals(obj.accessType))
			return false;
		if (!faBeanId.equals(obj.faBeanId))
			return false;
		if (!baBeanId.equals(obj.baBeanId))
			return false;
		if (!publicKey.equals(obj.publicKey))
			return false;
		if (!desKey.equals(obj.desKey))
			return false;
		if (!echoMsgCd.equals(obj.echoMsgCd))
			return false;
		if (!publishDt.equals(obj.publishDt))
			return false;
		if (!effectDt.equals(obj.effectDt))
			return false;
		if (!location.equals(obj.location))
			return false;
		if (!ext1.equals(obj.ext1))
			return false;
		if (!ext2.equals(obj.ext2))
			return false;
		if (!ext3.equals(obj.ext3))
			return false;
		if (!synResponse2QM.equals(obj.synResponse2QM))
			return false;
		if (!authMsgCd.equals(obj.authMsgCd))
			return false;
		if (!qmgr.equals(obj.qmgr))
			return false;
		if (!httpURI.equals(obj.httpURI))
			return false;
		if (!remark.equals(obj.remark))
			return false;
		if (!locationNo.equals(obj.locationNo))
			return false;
		if (!slaMaxCon.equals(obj.slaMaxCon))
			return false;
		if (!slaAvgCost.equals(obj.slaAvgCost))
			return false;
		if (!slaMaxCost.equals(obj.slaMaxCost))
			return false;
		if (!slaSucRatio.equals(obj.slaSucRatio))
			return false;
		if (!slaRecovery.equals(obj.slaRecovery))
			return false;
		if (!userCd.equals(obj.userCd))
			return false;
		if (!lastUpdTm.equals(obj.lastUpdTm))
			return false;
		if (!verDt.equals(obj.verDt))
			return false;
		if (!verStatus.equals(obj.verStatus))
			return false;
		if (!actionNm.equals(obj.actionNm))
			return false;
		return true;
	}

	public int compareTo(Object o) {
		return -1;
	}

	// set all properties to default value...
	public void init() {
	}

	public String getAppCd() {
		return appCd;
	}

	public void setAppCd(String appCd) {
		this.appCd = appCd;
	}

	public String getMbrCd() {
		return mbrCd;
	}

	public void setMbrCd(String mbrCd) {
		this.mbrCd = mbrCd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppAttr() {
		return appAttr;
	}

	public void setAppAttr(String appAttr) {
		this.appAttr = appAttr;
	}

	public String getSigBeanId() {
		return sigBeanId;
	}

	public void setSigBeanId(String sigBeanId) {
		this.sigBeanId = sigBeanId;
	}

	public String getSigCntBeanId() {
		return sigCntBeanId;
	}

	public void setSigCntBeanId(String sigCntBeanId) {
		this.sigCntBeanId = sigCntBeanId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getLocalPort() {
		return localPort;
	}

	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getFaBeanId() {
		return faBeanId;
	}

	public void setFaBeanId(String faBeanId) {
		this.faBeanId = faBeanId;
	}

	public String getBaBeanId() {
		return baBeanId;
	}

	public void setBaBeanId(String baBeanId) {
		this.baBeanId = baBeanId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getDesKey() {
		return desKey;
	}

	public void setDesKey(String desKey) {
		this.desKey = desKey;
	}

	public String getEchoMsgCd() {
		return echoMsgCd;
	}

	public void setEchoMsgCd(String echoMsgCd) {
		this.echoMsgCd = echoMsgCd;
	}

	public String getPublishDt() {
		return publishDt;
	}

	public void setPublishDt(String publishDt) {
		this.publishDt = publishDt;
	}

	public String getEffectDt() {
		return effectDt;
	}

	public void setEffectDt(String effectDt) {
		this.effectDt = effectDt;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getExt1() {
		return ext1;
	}

	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}

	public String getExt2() {
		return ext2;
	}

	public void setExt2(String ext2) {
		this.ext2 = ext2;
	}

	public String getExt3() {
		return ext3;
	}

	public void setExt3(String ext3) {
		this.ext3 = ext3;
	}

	public String getSynResponse2QM() {
		return synResponse2QM;
	}

	public void setSynResponse2QM(String synResponse2QM) {
		this.synResponse2QM = synResponse2QM;
	}

	public String getAuthMsgCd() {
		return authMsgCd;
	}

	public void setAuthMsgCd(String authMsgCd) {
		this.authMsgCd = authMsgCd;
	}

	public String getQmgr() {
		return qmgr;
	}

	public void setQmgr(String qmgr) {
		this.qmgr = qmgr;
	}

	public String getHttpURI() {
		return httpURI;
	}

	public void setHttpURI(String httpURI) {
		this.httpURI = httpURI;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getLocationNo() {
		return locationNo;
	}

	public void setLocationNo(Integer locationNo) {
		this.locationNo = locationNo;
	}

	public Integer getSlaMaxCon() {
		return slaMaxCon;
	}

	public void setSlaMaxCon(Integer slaMaxCon) {
		this.slaMaxCon = slaMaxCon;
	}

	public Integer getSlaAvgCost() {
		return slaAvgCost;
	}

	public void setSlaAvgCost(Integer slaAvgCost) {
		this.slaAvgCost = slaAvgCost;
	}

	public Integer getSlaMaxCost() {
		return slaMaxCost;
	}

	public void setSlaMaxCost(Integer slaMaxCost) {
		this.slaMaxCost = slaMaxCost;
	}

	public Double getSlaSucRatio() {
		return slaSucRatio;
	}

	public void setSlaSucRatio(Double slaSucRatio) {
		this.slaSucRatio = slaSucRatio;
	}

	public Integer getSlaRecovery() {
		return slaRecovery;
	}

	public void setSlaRecovery(Integer slaRecovery) {
		this.slaRecovery = slaRecovery;
	}

	public String getUserCd() {
		return userCd;
	}

	public void setUserCd(String userCd) {
		this.userCd = userCd;
	}

	public String getLastUpdTm() {
		return lastUpdTm;
	}

	public void setLastUpdTm(String lastUpdTm) {
		this.lastUpdTm = lastUpdTm;
	}

	public String getVerDt() {
		return verDt;
	}

	public void setVerDt(String verDt) {
		this.verDt = verDt;
	}

	public String getVerStatus() {
		return verStatus;
	}

	public void setVerStatus(String verStatus) {
		this.verStatus = verStatus;
	}

	public String getActionNm() {
		return actionNm;
	}

	public void setActionNm(String actionNm) {
		this.actionNm = actionNm;
	}

	public void set(NodePO vo) {
		this.appCd = vo.appCd;
		this.mbrCd = vo.mbrCd;
		this.name = vo.name;
		this.appAttr = vo.appAttr;
		this.sigBeanId = vo.sigBeanId;
		this.sigCntBeanId = vo.sigCntBeanId;
		this.host = vo.host;
		this.localPort = vo.localPort;
		this.accessType = vo.accessType;
		this.faBeanId = vo.faBeanId;
		this.baBeanId = vo.baBeanId;
		this.publicKey = vo.publicKey;
		this.desKey = vo.desKey;
		this.echoMsgCd = vo.echoMsgCd;
		this.publishDt = vo.publishDt;
		this.effectDt = vo.effectDt;
		this.location = vo.location;
		this.ext1 = vo.ext1;
		this.ext2 = vo.ext2;
		this.ext3 = vo.ext3;
		this.synResponse2QM = vo.synResponse2QM;
		this.authMsgCd = vo.authMsgCd;
		this.qmgr = vo.qmgr;
		this.httpURI = vo.httpURI;
		this.remark = vo.remark;
		this.locationNo = vo.locationNo;
		this.slaMaxCon = vo.slaMaxCon;
		this.slaAvgCost = vo.slaAvgCost;
		this.slaMaxCost = vo.slaMaxCost;
		this.slaSucRatio = vo.slaSucRatio;
		this.slaRecovery = vo.slaRecovery;
		this.userCd = vo.userCd;
		this.lastUpdTm = vo.lastUpdTm;
		this.verDt = vo.verDt;
		this.verStatus = vo.verStatus;
		this.actionNm = vo.actionNm;
	}

	public StringBuffer toJson() {
		StringBuffer buf = new StringBuffer();
		try {
			buf.append(spc.webos.util.JsonUtil.obj2json(this));
		} catch (Exception e) {
		}
		return buf;
	}

	public void afterLoad() {
	}

	public void beforeLoad() {
	}

	public void destroy() {

	}

	public String toString() {
		StringBuffer buf = new StringBuffer(128);
		buf.append(getClass().getName() + "(serialVersionUID=" + serialVersionUID + "):");
		buf.append(toJson());
		return buf.toString();
	}

	public Object clone() {
		NodePO obj = new NodePO();
		obj.set(this);
		return obj;
	}
}
