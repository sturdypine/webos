package spc.webos.dubbo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

import spc.webos.util.LogUtil;
import spc.webos.util.StringX;

/**
 * 同时支持dubbo客户端，dubbo服务端设置当前traceNo, 并将此traceNo设置到Log中让所有输出日志均带跟踪号信息
 * 
 * @author chenjs
 *
 */
public class TraceNoFilter implements Filter
{
	public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException
	{
		RpcContext rpc = RpcContext.getContext();
		if (rpc.isConsumerSide())
		{ // 如果当前是消费方环境，则将LogUtil中的追踪信息设置到rpc环境
			log.info("dubbo {} to {}, app:{}", rpc.getMethodName(), rpc.getRemoteAddressString(),
					LogUtil.getAppCd());
			if (LogUtil.getAppCd() != null)
				RpcContext.getContext().setAttachment(APP_CD, LogUtil.getAppCd());
			boolean set = setTraceNoIfEmpty(LogUtil.getTraceNo());
			try
			{
				return invoker.invoke(inv);
			}
			finally
			{ // 谁设置信息，在执行结束后负责清除，dubbo默认会清除当前请求的线程环境
				if (set) rpc.setAttachment(TRACE_NO, null);
			}
		}

		// 如果当前是服务方环境，则检查当前rpc中是否有跟踪号，如果有则设置
		boolean set = LogUtil.setTraceNo(rpc.getAttachment(TRACE_NO),
				"dubbo:" + inv.getMethodName(), true);
		String appCd = rpc.getAttachment(APP_CD);
		LogUtil.setAppCd(appCd);
		log.info("dubbo {} from {}, app:{}", rpc.getMethodName(), rpc.getRemoteAddressString(),
				appCd);
		try
		{
			return invoker.invoke(inv);
		}
		finally
		{ // 如果是当前线程设置了logutil.traceno， 则当前线程执行完后清除线程环境，谁设置谁清除
			if (set) LogUtil.removeTraceNo();
		}
	}

	public static boolean setTraceNoIfEmpty(String traceNo)
	{
		if (StringX.nullity(traceNo) || RpcContext.getContext().getAttachment(TRACE_NO) != null)
			return false;
		RpcContext.getContext().setAttachment(TRACE_NO, traceNo);
		return true;
	}

	Logger log = LoggerFactory.getLogger(getClass());
	public static String TRACE_NO = "TRACE_NO";
	public static String APP_CD = "APP_CD";
}
