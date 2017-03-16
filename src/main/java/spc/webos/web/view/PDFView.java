package spc.webos.web.view;

import java.io.ByteArrayOutputStream;
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
import spc.webos.util.PDFUtil;
import spc.webos.util.StringX;

public class PDFView implements View
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	@Resource
	protected ExceptionView exView;

	public String getContentType()
	{
		return Common.FILE_PDF_CONTENTTYPE;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		response.setContentType(getContentType());
		String fileName = (String) model.get(Web.REQ_KEY_DOWNLOAD_FILE_NAME);
		if (!StringX.nullity(fileName))
			response.setHeader(Common.REQ_HEADER_KEY_1, "attachment; filename="
					+ new String(StringX.utf82str(fileName).getBytes(), Common.CHARSET_ISO));

		String id = (String) model.get(Web.REQ_KEY_TEMPLATE_ID); // 采用的模板ID
		String template = (String) model.get(Web.REQ_KEY_VIEW_NAME_SKEY); // 采用的page

		log.info("pdf id:{}, page:{}, file:{}", id, template, fileName);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos))
		{
			FTLUtil.ftl(FTLUtil.getInstance().cacheContain(id) ? id : template, model, osw);
			PDFUtil.createPdf(response.getOutputStream(), baos.toByteArray());
		}
		catch (Exception e)
		{ // 如果在处理html, pdf有异常，则调用异常处理view
			model.put(Common.MODEL_EXCEPTION, e);
			exView.render(model, request, response);
		}
	}

	public void setExView(ExceptionView exView)
	{
		this.exView = exView;
	}
}
