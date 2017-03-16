package spc.webos.web.view;

import java.io.OutputStreamWriter;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;

/**
 * response.setContentType ("application/vnd.ms-excel; charset=gb2312");
 * response.setHeader ("Content-Disposition",
 * "attachment;filename=\"tmp.xls\"");
 * 
 * response.setContentType ("application/msword; charset=gb2312");
 * response.setContentType ( "application/application/vnd.ms-powerpoint;
 * charset=gb2312"); response.setContentType ("application/pdf; charset=gb2312
 * "); response.setContentType ("application/octet-stream; charset=gb2312 ");
 * response.setContentType ("text/html; charset=gb2312");
 * 
 * @author sturdypine.chen
 * 
 */
public class MSView implements View
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected String fileExt = "doc"; // 默认是doc
	@Resource
	protected ExceptionView exView;

	public String getContentType()
	{
		if (isWord()) return Common.FILE_WORD_CONTENTTYPE;
		if (isExcel()) return Common.FILE_EXCEL_CONTENTTYPE;
		if (isPPT()) return Common.FILE_POWERPOINT_CONTENTTYPE;
		return Common.FILE_WORD_CONTENTTYPE;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		response.setContentType(getContentType());

		String id = (String) model.get(Web.REQ_KEY_TEMPLATE_ID); // 采用的模板ID
		String template = (String) model.get(Web.REQ_KEY_VIEW_NAME_SKEY); // 采用的page
		String fileName = StringX.utf82str((String) model.get(Web.REQ_KEY_DOWNLOAD_FILE_NAME));
		if (StringX.nullity(fileName)) fileName = id.replace('/', '_');
		fileName += "." + fileExt;
		log.info("ms id:{}, page:{}, file:{}", id, template, fileName);

		response.setHeader(Common.REQ_HEADER_KEY_1, "attachment; filename="
				+ new String(StringX.utf82str(fileName).getBytes(), Common.CHARSET_ISO));

		try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream()))
		{
			FTLUtil.ftl(FTLUtil.getInstance().cacheContain(id) ? id : template, model, osw);
			osw.flush();
		}
		catch (Exception e)
		{ // 如果在处理html, pdf有异常，则调用异常处理view
			model.put(Common.MODEL_EXCEPTION, e);
			exView.render(model, request, response);
		}
	}

	public boolean isWord()
	{
		return fileExt.toLowerCase().startsWith("doc");
	}

	public boolean isExcel()
	{
		return fileExt.toLowerCase().startsWith("xls");
	}

	public boolean isPPT()
	{
		return fileExt.toLowerCase().startsWith("ppt");
	}

	public void setExView(ExceptionView exView)
	{
		this.exView = exView;
	}

	public void setFileExt(String fileExt)
	{
		this.fileExt = fileExt;
	}
}
