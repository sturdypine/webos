package spc.webos.exception;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

public class AppException extends RuntimeException
{
	public AppException()
	{
	}

	public AppException(Status status)
	{
		this.code = status.getRetCd(); // chenjs 2012-09-09 增加返回码信息
		this.desc = status.getDesc();
		this.detail = status;
	}

	public AppException(String code, Throwable t)
	{
		this(code, t, null, null, null);
	}

	public AppException(String code, Throwable t, Object[] args)
	{
		this(code, t, args, null, null);
	}

	public AppException(String code, Throwable t, Object[] args, Object detail)
	{
		this(code, t, args, detail, null);
	}

	public AppException(String code, Throwable t, Object[] args, Object detail, String msgFormat)
	{
		super(t);
		this.code = code;
		this.args = args;
		this.detail = detail;
		this.msgFormat = msgFormat;
	}

	public AppException(String code)
	{
		this(code, (Object[]) null, null, null);
	}

	public AppException(String code, String desc)
	{
		this.code = code;
		this.desc = desc;
	}

	public AppException(String code, Object[] args)
	{
		this(code, args, null, null);
	}

	public AppException(String code, Object[] args, String format)
	{
		this(code, args, null, format);
	}

	public AppException(String code, Object[] args, Object detail)
	{
		this(code, args, detail, null);
	}

	public AppException(String code, Object[] args, Object detail, String format)
	{
		super(code);
		this.code = code;
		this.args = args;
		this.detail = detail;
		this.msgFormat = format;
		if (format != null) this.desc = MessageFormat.format(format, args);
	}

	public boolean isSysLevel()
	{
		return code.startsWith("1") || code.startsWith("9");
	}

	public Object getDetail()
	{
		return detail;
	}

	public void setDetail(Object detail)
	{
		this.detail = detail;
	}

	public String toJson()
	{
		Map params = new HashMap();
		params.put("code", code);
		if (getCause() != null) params.put("exception", getCause().toString());
		if (args != null && args.length > 0) params.put("args", args);
		// if (detail != null && !(detail instanceof IMessage)) params
		// .put("detail", detail.toString());
		if (!StringX.nullity(desc)) params.put("desc", desc);
		if (!StringX.nullity(msgFormat)) params.put("msgFormat", msgFormat);
		return JsonUtil.gson(params);
	}

	public String toString()
	{
		return "EX:: code:" + code + ", args:" + Arrays.toString(args) + ", def:" + desc + ", "
				+ super.toString();
	}

	// protected Status status;
	protected String code; // 消息代码, 系统统一建立一个异常代码的静态类，然后配置一个代码对应的消息信息的属性文件
	// String location; // 发生异常的位置, 一般是代码位置

	// String defMsg; // 通过对应的消息代码找不到消息信息后的默认值
	protected Object[] args; // 消息参数，可能消息需要使用MessageFormat
	// protected String defErrMsg; // 默认的错误消息
	protected String desc; // 最终描述信息
	protected String msgFormat; // 可以由异常抛出地方显示提供msgFormat
	protected String action; // 错误推荐处理动作
	protected transient Object detail; // 主要为jsrmi准备. 可以带一个错误对象到客户端

	static final long serialVersionUID = 0;

	public Object[] getArgs()
	{
		return args;
	}

	public void setArgs(Object[] args)
	{
		this.args = args;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getLocation()
	{
		return getLocation(getStackTrace());
	}

	public static String getLocation(StackTraceElement[] stack)
	{
		return stack[0].getClassName() + '.' + stack[0].getMethodName() + ':'
				+ stack[0].getLineNumber();
	}

	public String getMsgFormat()
	{
		return msgFormat;
	}

	public void setMsgFormat(String msgFormat)
	{
		this.msgFormat = msgFormat;
	}

	public String getDesc()
	{
		return desc;
	}

	public void setDesc(String desc)
	{
		this.desc = desc;
	}
}
