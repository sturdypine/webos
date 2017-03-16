package demo.impl;

import java.util.Arrays;

import com.alibaba.dubbo.rpc.RpcContext;

import demo.INotify;

public class Notify implements INotify {

	public void onreturn(Object msg, Object... name) {
		System.out.println("onreturn: " + Arrays.toString(name) + "-" + msg + "::: "
				+ RpcContext.getContext().getAttachment("TraceNO") + "," + RpcContext.getContext().getMethodName());

	}

	public void onthrow(Throwable ex, Object... name) {
		System.out.println("onthrow: " + Arrays.toString(name) + ":" + ex);
	}

}
