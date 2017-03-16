package spc.webos.service.timeout.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spc.webos.persistence.PO;
import spc.webos.service.BaseService;
import spc.webos.service.timeout.TimeoutService;
import spc.webos.service.timeout.Timeout;

public class DBTimeoutFinderServiceImpl extends BaseService implements TimeoutService
{
	public List find() throws Exception
	{
		return (List) persistence.execute(findSqlId, null);
	}

	public boolean remove(Timeout timeout) throws Exception
	{
		Map params = new HashMap();
		params.put("timeout", timeout);
		int[] rows = (int[]) persistence.execute(removeSqlId, params);
		return rows[0] > 0;
	}

	public boolean add(Timeout timeout) throws Exception
	{
		if (timeout instanceof PO) return persistence.insert((PO) timeout) > 0;
		Map params = new HashMap();
		params.put("timeout", timeout);
		int[] rows = (int[]) persistence.execute(addSqlId, params);
		return rows[0] > 0;
	}

	protected String findSqlId;
	protected String removeSqlId;
	protected String addSqlId;

	public void setFindSqlId(String findSqlId)
	{
		this.findSqlId = findSqlId;
	}

	public void setRemoveSqlId(String removeSqlId)
	{
		this.removeSqlId = removeSqlId;
	}

	public void setAddSqlId(String addSqlId)
	{
		this.addSqlId = addSqlId;
	}
}
