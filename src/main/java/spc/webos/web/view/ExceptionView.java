package spc.webos.web.view;

import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import spc.webos.constant.AppRetCode;
import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.exception.AppException;
import spc.webos.util.FTLUtil;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.util.WebUtil;

// import
// org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class ExceptionView implements View
{
	protected String contentType = Common.FILE_HTML_CONTENTTYPE;
	protected String errPage = "main/ftl/error";
	protected int status = 0; // http response status code
	// public final static String ERR_CLASS_KEY = "ERR_CLAZZ";
	protected Logger log = LoggerFactory.getLogger(getClass());

	public ExceptionView()
	{
	}

	public ExceptionView(String errPage, String contentType, int status)
	{
		this.errPage = errPage;
		this.contentType = contentType;
		this.status = status;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
	{
		Exception ex = (Exception) model.get(Common.MODEL_EXCEPTION);
		if (ex != null) log.debug("Ex, status:" + status, ex);
		String code, loc, uri;

		if (ex instanceof AppException)
		{
			AppException ae = (AppException) ex;
			model.put(Web.REQ_KEY_EX_CODE, code = ae.getCode());
			model.put(Web.REQ_KEY_EX_LOC, loc = ae.getLocation());
			String msg = msg = ae.getDesc();
			try
			{
				if (StringX.nullity(msg)) msg = SpringUtil.APPCXT
						.getMessage(SpringUtil.RETCD_PATH + ae.getCode(), ae.getArgs(), null);
			}
			catch (Exception e)
			{
				log.warn("Fail to get error desc for " + ae.getCode(), e);
			}
			model.put(Web.REQ_KEY_EX_MSG, msg);
		}
		else
		{
			model.put(Web.REQ_KEY_EX_CODE, code = AppRetCode.CMMN_UNDEF_EX);
			model.put(Web.REQ_KEY_EX_MSG, ex.toString());
			StackTraceElement[] stack = ex.getCause() != null ? ex.getCause().getStackTrace()
					: ex.getStackTrace();
			loc = stack[0].getClassName() + '.' + stack[0].getMethodName() + ':'
					+ stack[0].getLineNumber();
			model.put(Web.REQ_KEY_EX_LOC, loc);
		}
		model.put(Web.REQ_KEY_EX_TRC, LogUtil.getTraceNo());
		model.put("uri", uri = request.getRequestURI());
		model.put("queryString", StringX.null2emptystr(request.getQueryString()));
		if (status > 0) response.setStatus(status);
		request.setAttribute(Web.RESP_ATTR_ERROR_KEY, Boolean.TRUE); // ∑¿÷πª∫¥Ê
		log.info("uri:{}, status:{}, content:{}, page:{}, code:{}, loc:{}, ex:{}", uri, status,
				contentType, errPage, code, loc, StringX.null2emptystr(ex));
		try
		{
			WebUtil.request2map(request, model);
			response.setContentType(contentType);
			response.setHeader("Cache-Control", "no-cache");
			StringWriter sw = new StringWriter();
			FTLUtil.ftl(errPage, model, sw);
			if (log.isDebugEnabled()) log.debug("ex response:{}", StringX.utf82str(sw.toString()));
			response.getWriter().write(StringX.str2utf8(sw.toString()));
			response.getWriter().flush();
		}
		catch (Exception e)
		{
			log.error("errPage:" + errPage, e);
		}
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public void setErrPage(String errPage)
	{
		this.errPage = errPage;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
}
