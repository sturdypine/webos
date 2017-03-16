package spc.webos.endpoint;

import java.io.Serializable;
import java.util.Map;

import spc.webos.exception.Status;
import spc.webos.util.StringX;

public class Executable implements Serializable
{
	private static final long serialVersionUID = 1L;
	public byte[] correlationID;
	public byte[] messageId;
	public int timeout = 60; // seconds
	public byte[] request;
	public Object reqExt; // 请求扩展信息，for AS400批量
	public byte[] response;
	public Object repExt; // 响应扩展信息，for AS400批量
	public String cicsProgram; // for CICS 方式调用
	public long reqTime;
	public long resTime;
	public Status status;
	public Object reqmsg;
	public Object resmsg;
	public boolean withoutReturn;
	// 2012-07-05 表示请求是否发送，如果请求已经发送再出现异常则不能使用集群模式的重新请求，防止重复交易
	public boolean cnnSnd;
	public boolean query; // 2014-08-08 查询类交易，可集群重复发送
	public String reqQName; // 请求队列名
	public String repQName; // 应答队列名
	public String location; // 2012-05-01 增加服务位置参数，用于动态TCP/HTTP
	public Map<String, String> reqHttpHeaders;
	public Map<String, String> repHttpHeaders;
	public int httpStatus; // http应答的状态码
	// public ITCPCallback callback; // 700 2013-08-25

	public Executable()
	{
		this.reqTime = System.currentTimeMillis();
	}

	public Executable(String correlationID, byte[] request)
	{
		this(correlationID, request, -1, null);
	}

	public Executable(String correlationID, byte[] request, int timeout)
	{
		this(correlationID, request, timeout, null);
	}

	public Executable(String correlationID, byte[] request, int timeout, String repQName)
	{
		this.correlationID = correlationID.getBytes();
		this.timeout = timeout;
		this.request = request;
		this.repQName = repQName;
		this.reqTime = System.currentTimeMillis();
	}

	public Executable(String correlationID, byte[] request, int timeout, String reqQName,
			String repQName)
	{
		this.correlationID = correlationID.getBytes();
		this.timeout = timeout;
		this.request = request;
		this.reqQName = reqQName;
		this.repQName = repQName;
		this.reqTime = System.currentTimeMillis();
	}

	public Executable(byte[] request, int timeout)
	{
		this.timeout = timeout;
		this.request = request;
		this.reqTime = System.currentTimeMillis();
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getCorrelationID()
	{
		return correlationID == null ? StringX.EMPTY_STRING : new String(correlationID);
	}

	public void setCorrelationID(String correlationID)
	{
		if (StringX.nullity(correlationID)) return;
		this.correlationID = correlationID.getBytes();
	}
}
