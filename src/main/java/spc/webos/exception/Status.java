package spc.webos.exception;

import java.io.Serializable;
import java.util.Map;

import spc.webos.util.JsonUtil;
import spc.webos.util.LogUtil;
import spc.webos.util.StringX;

public class Status implements Serializable
{
	private static final long serialVersionUID = 1L;
	public final static String STATUS_UNDERWAY = "U";
	public final static String STATUS_SUCCESS = "S";
	public final static String STATUS_FAIL = "F";

	public static String RETCD = "retCd";
	public static String DESC = "desc";
	public static String LOC = "location";
	public static String NODE = "mbrCd";
	public static String APPCD = "appCd";
	public static String IP = "ip";

	// added by chenjs 2011-10-13 正确返回码首字母范围
	// public static String SUCCESS = "000000"; // added by chenjs 已经停止使用,
	// 改为AppRetCode.SUCCESS();
	public static String SUCCESS_PREFIX = "0";
	public static int SUCCESS_PREFIX_LEN = 1;
	public static int SUCCESS_PREFIX_START = 0;
	
	public boolean success;
	public String retCd;
	public String desc;
	public String location;
	public String mbrCd; // 成员编号
	public String appCd; // 应用编号
	public String ip;
	public String traceNo = LogUtil.getTraceNo(); // 默认构造时使用当前线程环境的日志跟踪号

	public Status()
	{
	}

	public Status(String retCd)
	{
		this.retCd = retCd;
		success = success();
	}

	public Status(Map<String, String> m)
	{
		this.retCd = m.get(RETCD);
		this.desc = m.get(DESC);
		this.location = m.get(LOC);
		this.appCd = m.get(APPCD);
		this.ip = m.get(IP);
		success = success();
	}

	public Status(String retCd, String desc, String loc, String appCd, String ip)
	{
		this.retCd = retCd;
		this.desc = desc;
		this.location = loc;
		this.appCd = appCd;
		this.ip = ip;
		success = success();
	}

	public Status(String retCd, String desc, String loc, String mbrCd, String appCd, String ip)
	{
		this.retCd = retCd;
		this.desc = desc;
		this.location = loc;
		this.mbrCd = mbrCd;
		this.appCd = appCd;
		this.ip = ip;
		success = success();
	}

	public boolean success()
	{
		// modified by chenjs 2011-09-28 返回码支持填写I(info), 代表信息成功类
		return !StringX.nullity(retCd) && SUCCESS_PREFIX.indexOf(RETCD_STATUS(retCd)) >= 0;
	}

	public static boolean isSuccess(String retCd)
	{
		// modified by chenjs 2011-09-28 返回码支持填写I(info), 代表信息成功类
		return !StringX.nullity(retCd) && SUCCESS_PREFIX.indexOf(RETCD_STATUS(retCd)) >= 0;
	}

	public boolean fail()
	{
		return !StringX.nullity(retCd) && SUCCESS_PREFIX.indexOf(RETCD_STATUS(retCd)) < 0;
	}

	public static String RETCD_STATUS(String retCd)
	{
		if (SUCCESS_PREFIX_START >= 0) return retCd.substring(SUCCESS_PREFIX_START,
				SUCCESS_PREFIX_LEN < retCd.length() ? SUCCESS_PREFIX_LEN : retCd.length());
		// 如果是负数则表示从末尾多少为开始
		return retCd.substring(retCd.length() + SUCCESS_PREFIX_START, SUCCESS_PREFIX_LEN);
	}

	public boolean underWay()
	{
		return StringX.nullity(retCd);
	}

	public String getRetCd()
	{
		return retCd;
	}

	public void setRetCd(String code)
	{
		this.retCd = code;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	public String getTraceNo()
	{
		return traceNo;
	}

	public void setTraceNo(String traceNo)
	{
		this.traceNo = traceNo;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String nodeAppCd()
	{
		return StringX.null2emptystr(mbrCd) + appCd;
	}

	public String getAppCd()
	{
		return appCd;
	}

	public void setAppCd(String appCd)
	{
		this.appCd = appCd;
	}

	public String getMbrCd()
	{
		return mbrCd;
	}

	public void setMbrCd(String mbrCd)
	{
		this.mbrCd = mbrCd;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public StringBuffer toJson()
	{
		return new StringBuffer(JsonUtil.obj2json(this));
	}

	public String toString()
	{
		return "retCd:" + retCd + ",desc:" + desc + ",loc:" + location + ",mbr:" + mbrCd + ",app:"
				+ appCd + ",ip:" + ip;
	}
}
