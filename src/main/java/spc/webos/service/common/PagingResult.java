package spc.webos.service.common;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果数据， 用于前端表格分页显示
 * 
 * @author chenjs
 *
 */
public class PagingResult<T> implements Serializable
{
	private static final long serialVersionUID = 20161031L;
	public boolean success = true; // 为了标准支持extjs grid数据
	public Integer total;
	public Integer start;
	public Integer limit;
	public List<T> data;

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
		if (limit >= 0) this.limit = limit;
	}

	public PagingResult(List<T> data)
	{
		this.total = data == null ? 0 : data.size();
		this.data = data;
	}

	public Integer getTotal()
	{
		return total;
	}

	public void setTotal(Integer total)
	{
		this.total = total;
	}

	public Integer getStart()
	{
		return start;
	}

	public void setStart(Integer start)
	{
		this.start = start;
	}

	public Integer getLimit()
	{
		return limit;
	}

	public void setLimit(Integer limit)
	{
		this.limit = limit;
	}

	public List<T> getData()
	{
		return data;
	}

	public void setData(List<T> data)
	{
		this.data = data;
	}

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}
}
