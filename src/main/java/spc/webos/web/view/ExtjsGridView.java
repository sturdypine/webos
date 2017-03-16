package spc.webos.web.view;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.service.common.PagingResult;
import spc.webos.util.StringX;

/**
 * 用于生成ext grid格式的数据
 * 
 * @author chenjs
 *
 */
public class ExtjsGridView implements View
{
	protected Logger log = LoggerFactory.getLogger(getClass());

	public String getContentType()
	{
		return Common.FILE_JSON_CONTENTTYPE;
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		response.setContentType(getContentType());
		Object r = model.get(Web.EXTGRID_DS_KEY);
		if (r instanceof List) grid(new PagingResult((List) r), request, response, model);
		else grid((PagingResult) r, request, response, model);
	}

	// added by chenjs 2011-12-22 是否要做服务器分页
	protected List paging(List rows, HttpServletRequest request, Map model)
	{
		if (StringX.nullity((String) model.get(Web.REQ_PAGING)) || rows == null || rows.size() == 0)
			return rows;
		String limit = StringX.null2emptystr((String) model.get(Web.REQ_EXTJS_PAGING_LIMIT),
				Web.REQ_PAGING_DEF_LIMIT);
		if (StringX.nullity(limit)) return rows;
		String start = StringX.null2emptystr((String) model.get(Web.REQ_EXTJS_PAGING_START), "0");
		if (rows.size() <= Integer.parseInt(limit))
		{ // 如果目前结果集条数还有limit大则不分页，直接返回
			log.info("Fail to paging: rows size: {} <= imit: {}", rows.size(), limit);
			return rows;
		}
		List nrows = new ArrayList();
		for (int i = Integer.parseInt(start); i < rows.size()
				&& (i < Integer.parseInt(start) + Integer.parseInt(limit)); i++)
			nrows.add(rows.get(i));
		return nrows;
	}

	protected void grid(PagingResult result, HttpServletRequest request,
			HttpServletResponse response, Map model) throws Exception
	{
		List columns = null;
		String column = (String) model.get(Web.REQ_KEY_COLUMN);
		if (!StringX.nullity(column)) columns = StringX.split2list(column, StringX.COMMA);
		PrintWriter pw = response.getWriter();
		// extjs3 需要回调
		String callback = (String) model.get("callback");
		if (!StringX.nullity(callback))
		{
			pw.print(callback);
			pw.print('(');
		}

		String v = StringX.table2extgrid(paging(result.data, request, model), result.total,
				result.start, result.limit, columns,
				"true".equalsIgnoreCase((String) model.get(Web.REQ_KEY_UTF8)));
		pw.print(v);
		if (!StringX.nullity(callback)) pw.print(')');
	}
}
