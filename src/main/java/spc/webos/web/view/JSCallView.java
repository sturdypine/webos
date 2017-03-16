package spc.webos.web.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.web.filter.multipart.MultipartRequestHandler;

public class JSCallView implements View
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected String resultTag; // = "result";
	protected String successTag = "success";
	protected boolean utf8 = false;

	public String getContentType()
	{
		return Common.FILE_JSON_CONTENTTYPE;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		response.setContentType(getContentType());
		Object ret = model.get(Web.SERVICE_RET_KEY);
		if (!StringX.nullity(resultTag) || MultipartRequestHandler.isMultipartRequest(request))
		{ // 如果是文件上传模式，必须按form.submit返回json
			Map<String, Object> retm = new HashMap<>();
			retm.put(resultTag == null ? "result" : resultTag, ret);
			retm.put(successTag, true);
			ret = retm;
		}
		String json = JsonUtil.obj2json(ret);
		log.debug("utf8:{}, json:{}", utf8, json);
		response.getWriter().print(utf8 ? StringX.str2utf8(json) : json);
	}

	public void setResultTag(String resultTag)
	{
		this.resultTag = resultTag;
	}

	public void setSuccessTag(String successTag)
	{
		this.successTag = successTag;
	}

	public void setUtf8(boolean utf8)
	{
		this.utf8 = utf8;
	}
}
