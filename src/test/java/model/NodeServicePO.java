package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * genarated by sturdypine.chen Email: sturdypine@gmail.com description:
 */
@Entity
@Table(name = "esb_nodeservice")
public class NodeServicePO implements java.io.Serializable {
	public static final long serialVersionUID = 20160120L;
	// 和物理表对应字段的属性
	@Id
	@Column
	protected String appCd; // 主键
	@Id
	@Column
	protected String mbrCd; // 主键
	@Id
	@Column
	protected String msgCd; // 主键
	@Column
	protected String attr; //
	@Column
	protected String qname; //
	@Column
	protected String routeRule; //
	@Column
	protected String routeBeanName; //
	@Column
	protected String location; //
	@Column
	protected String asynRepQName; //
	@Column
	protected String asynRepLocation; //
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
	@ManyToOne
	@JoinColumn(name = "appCd", referencedColumnName = "appCd")
	protected NodePO node;
	@OneToMany
	@JoinColumn(name = "appCd")
	protected List<NodePO> nodes;

	// 和此VO相关联的其他简单Sql属性
	// Note: 如果关联的Sql对象为String, Inegter...等Java final class时， 只能使用Object对象，
	// 访问时候只能通过
	// Object的toString()方法来使用。

	public NodeServicePO() {
	}

	public NodeServicePO(String appCd, String mbrCd, String msgCd) {
		this.appCd = appCd;
		this.mbrCd = mbrCd;
		this.msgCd = msgCd;
	}

	public String primary() {
		String delim = "#";
		StringBuffer buf = new StringBuffer();
		buf.append(this.appCd);
		buf.append(delim + this.mbrCd);
		buf.append(delim + this.msgCd);
		return buf.toString();
	}

	public String table() {
		return "esb_nodeservice";
	}

	public String[] blobFields() {
		return null;
	}

	// set all properties to NULL
	public void setNULL() {
		this.appCd = null;
		this.mbrCd = null;
		this.msgCd = null;
		this.attr = null;
		this.qname = null;
		this.routeRule = null;
		this.routeBeanName = null;
		this.location = null;
		this.asynRepQName = null;
		this.asynRepLocation = null;
		this.userCd = null;
		this.lastUpdTm = null;
		this.verDt = null;
		this.verStatus = null;
		this.actionNm = null;
		this.node = null;
		this.nodes = null;
	}

	public void setRefNull() {
		this.node = null;
		this.nodes = null;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof NodeServicePO))
			return false;
		NodeServicePO obj = (NodeServicePO) o;
		if (!appCd.equals(obj.appCd))
			return false;
		if (!mbrCd.equals(obj.mbrCd))
			return false;
		if (!msgCd.equals(obj.msgCd))
			return false;
		if (!attr.equals(obj.attr))
			return false;
		if (!qname.equals(obj.qname))
			return false;
		if (!routeRule.equals(obj.routeRule))
			return false;
		if (!routeBeanName.equals(obj.routeBeanName))
			return false;
		if (!location.equals(obj.location))
			return false;
		if (!asynRepQName.equals(obj.asynRepQName))
			return false;
		if (!asynRepLocation.equals(obj.asynRepLocation))
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

	public String getMsgCd() {
		return msgCd;
	}

	public void setMsgCd(String msgCd) {
		this.msgCd = msgCd;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String getQname() {
		return qname;
	}

	public void setQname(String qname) {
		this.qname = qname;
	}

	public String getRouteRule() {
		return routeRule;
	}

	public void setRouteRule(String routeRule) {
		this.routeRule = routeRule;
	}

	public String getRouteBeanName() {
		return routeBeanName;
	}

	public void setRouteBeanName(String routeBeanName) {
		this.routeBeanName = routeBeanName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAsynRepQName() {
		return asynRepQName;
	}

	public void setAsynRepQName(String asynRepQName) {
		this.asynRepQName = asynRepQName;
	}

	public String getAsynRepLocation() {
		return asynRepLocation;
	}

	public void setAsynRepLocation(String asynRepLocation) {
		this.asynRepLocation = asynRepLocation;
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

	// Note: 访问时候只能通过Object的toString()方法来使用。
	public NodePO getNode() {
		return node;
	}

	public void setNode(NodePO node) {
		this.node = node;
	}

	// Note: 访问时候只能通过Object的toString()方法来使用。
	public List<NodePO> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodePO> nodes) {
		this.nodes = nodes;
	}

	public void set(NodeServicePO vo) {
		this.appCd = vo.appCd;
		this.mbrCd = vo.mbrCd;
		this.msgCd = vo.msgCd;
		this.attr = vo.attr;
		this.qname = vo.qname;
		this.routeRule = vo.routeRule;
		this.routeBeanName = vo.routeBeanName;
		this.location = vo.location;
		this.asynRepQName = vo.asynRepQName;
		this.asynRepLocation = vo.asynRepLocation;
		this.userCd = vo.userCd;
		this.lastUpdTm = vo.lastUpdTm;
		this.verDt = vo.verDt;
		this.verStatus = vo.verStatus;
		this.actionNm = vo.actionNm;
		this.node = vo.node;
		this.nodes = vo.nodes;
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
		NodeServicePO obj = new NodeServicePO();
		obj.set(this);
		return obj;
	}
}
