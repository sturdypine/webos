package spc.webos.web.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import spc.webos.constant.Common;
import spc.webos.constant.Web;
import spc.webos.persistence.IPersistence;
import spc.webos.util.StringX;
import spc.webos.web.util.WebUtil;

public class PageCtrller implements Controller
{
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String uri = request.getRequestURI();
		String[] pages = StringX.split(uri, "/");

		ModelAndView mv = new ModelAndView();
		Map params = mv.getModel();
		params.putAll(StringX.uri2params(pages, 3));
		WebUtil.request2map(request, params);

		String page = pages[pageIdx] + '/' + viewType + '/' + pages[pageIdx + 1];
		String view = (String) params.get(Web.REQ_KEY_VIEW_TYPE);
		if (StringX.nullity(view))
		{
			log.debug("uri view:{}", page);
			mv.setViewName(page); // 如果没指定view
		}
		else
		{ // pdf, xls, word
			view += viewPostfix;
			params.put(Web.REQ_KEY_VIEW_NAME_SKEY, page);
			params.put(Web.REQ_KEY_TEMPLATE_ID, pages[pageIdx] + '/' + pages[pageIdx + 1]);
			mv.setViewName(view); // 增加一个postfix
			log.debug("asign view:{}, page:{}", view, page);
		}

		// 1. 执行json服务, S.XXX=, S.YYY=
		WebUtil.invokeJsonService(request, params, servicePostfix);

		// 2. 检查是否有批查询
		String batchSQL = StringX.null2emptystr((String) params.get(Web.REQ_KEY_BATCH_SQL),
				(String) params.get(Web.REQ_KEY_BSQL));
		if (!StringX.nullity(batchSQL))
		{
			params.put(IPersistence.SELECT_ONLY, true); // 只能查询，不能修改
			persistence.execute(StringUtils.delimitedListToStringArray(batchSQL, StringX.COMMA),
					params, params);
		}
		return mv;
	}

	protected int pageIdx = 3;
	protected String servicePostfix = "Service";
	protected String viewPostfix = "View"; // spring id中view的统一后缀名
	protected String viewType = Common.TEMPLATE_TYPE_FTL; // ftl, jsp, vm
	// etc...
	// 前端带入的处理报表数据的服务，类似gridds里面的处理表格数据的服务
	@Resource
	protected IPersistence persistence;
	protected Logger log = LoggerFactory.getLogger(getClass());

	public void setPageIdx(int pageIdx)
	{
		this.pageIdx = pageIdx;
	}

	public void setPersistence(IPersistence persistence)
	{
		this.persistence = persistence;
	}

	public void setViewType(String viewType)
	{
		this.viewType = viewType;
	}

	public void setViewPostfix(String viewPostfix)
	{
		this.viewPostfix = viewPostfix;
	}

	public void setServicePostfix(String servicePostfix)
	{
		this.servicePostfix = servicePostfix;
	}
}
