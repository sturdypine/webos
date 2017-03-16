package spc.webos.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.util.FileUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.filter.multipart.MultipartRequestHandler;
import spc.webos.web.util.WebUtil;

/**
 * 将任何一个容器中的spring服务暴漏为json调用
 * 
 * @version 9.0.0
 * @author chenjs
 * 
 */
public class JSCallCtrller implements Controller
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected String charset = Common.CHARSET_UTF8;

	protected String viewName = "jsCallView";
	protected String servicePostfix = "Service";
	protected String viewPostfix = "View"; // spring id中view的统一后缀名
	protected String argsName = "args";

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		ModelAndView mv = new ModelAndView(viewName);

		if (request.getMethod().equalsIgnoreCase(Web.POST_METHOD)
				&& !MultipartRequestHandler.isMultipartRequest(request))
		{ // post method 并且不是文件上传类型， 参数为请求body
			mv.getModel().put(Web.SERVICE_RET_KEY, call(request, response,
					new String(FileUtil.is2bytes(request.getInputStream(), false), charset)));
		}
		else
		{ // get method, 参数内容为args=???
			String view = request.getParameter(Web.REQ_KEY_VIEW_TYPE);
			if (!StringX.nullity(view)) mv.setViewName(view + viewPostfix); // url里面指定view
			mv.getModel().put(Web.SERVICE_RET_KEY,
					call(request, response, request.getParameter(argsName)));
		}
		log.debug("view:{}", mv.getViewName());
		return mv;
	}

	protected Object call(HttpServletRequest request, HttpServletResponse response, String args)
			throws Exception
	{
		args = StringX.trim(StringX.utf82str(StringX.null2emptystr(args)));
		String[] m = StringX.last2path(request.getRequestURI());
		Object requestArgs = StringX.nullity(args) ? null : JsonUtil.json2obj(args);
		int argNum = WebUtil.getMethodArgNum(m[0], m[1], requestArgs);
		log.info("jsc:{}.{}, argNum:{}, len:{}, args:{}", m[0], m[1], argNum, args.length(),
				requestArgs != null);
		WebUtil.containService(m[0], m[1], argNum);
		return SpringUtil.jsonCall(m[0].endsWith(servicePostfix) ? m[0] : m[0] + servicePostfix,
				m[1], requestArgs, argNum);
	}

	public void setServicePostfix(String servicePostfix)
	{
		this.servicePostfix = servicePostfix;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public void setArgsName(String argsName)
	{
		this.argsName = argsName;
	}

	public void setViewPostfix(String viewPostfix)
	{
		this.viewPostfix = viewPostfix;
	}

	public void setViewName(String viewName)
	{
		this.viewName = viewName;
	}
}
