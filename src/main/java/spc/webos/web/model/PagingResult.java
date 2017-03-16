package spc.webos.web.model;

import java.util.List;

/**
 * 分页结果数据， 用于前端表格分页显示
 * 
 * @author chenjs
 *
 */
public class PagingResult<T> extends spc.webos.service.common.PagingResult<T>
{
	private static final long serialVersionUID = 20170217L;

	public PagingResult()
	{
	}

	public PagingResult(int total, List<T> data)
	{
		this.data = data;
		this.total = total > 0 ? total : (data == null ? 0 : data.size());
	}

	public PagingResult(int total, int start, int limit, List<T> data)
	{
		this.data = data;
		this.total = total > 0 ? total : (data == null ? 0 : data.size());
		this.start = start;
		this.limit = limit;
	}

	public PagingResult(List<T> data)
	{
		this.total = data == null ? 0 : data.size();
		this.data = data;
	}
}
