package spc.webos.service.resallocate.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.service.BaseService;
import spc.webos.service.resallocate.PoolableResource;
import spc.webos.service.resallocate.Resource;
import spc.webos.service.resallocate.ResourceGroup;
import spc.webos.service.resallocate.ResourcePoolService;
import spc.webos.util.StringX;

public abstract class AbstractResourcePoolServiceImpl extends BaseService
		implements ResourcePoolService
{
	protected List resources = new ArrayList(); // 当前所有资源类型列表, 无分类形式
	protected Map resourcePool = new HashMap(); // 当前资源池索引表
	// 当前已经占有资源的索引， 以流水号为索引, 考虑到回收操作的同步性，采用hashtable
	protected Map assignedPool = new Hashtable();
	public final static int INTERVAL = 20; // 线程休息时间

	public List apply(String sn, String key, int holdTime, int timeout, int matchType)
			throws AppException
	{
		if (log.isDebugEnabled()) log.debug("sn:" + sn + ",key:" + key + ",holdTm:" + holdTime
				+ ",timeout:" + timeout + ",matchType:" + matchType);
		if (!contain(key)) throw new AppException(AppRetCode.RES_ALLOCATE_APPLY_NO);
		List preses = apply(sn, key, holdTime);
		if (timeout < INTERVAL || preses.size() > 0) return preses;
		long start = System.currentTimeMillis();
		while (preses.size() <= 0 && (System.currentTimeMillis() - start < timeout))
		{
			try
			{
				Thread.sleep(INTERVAL);
			}
			catch (Exception e)
			{
				return null;
			}
			preses = apply(sn, key, holdTime);
		}
		return preses;
	}

	public boolean contain(String key)
	{
		if (resourcePool.size() == 0) return false;
		Iterator groups = resourcePool.values().iterator();
		while (groups.hasNext())
		{
			ResourceGroup group = (ResourceGroup) groups.next();
			if (group.match(key)) return true;
		}
		return false;
	}

	protected List apply(String sn, String key, int holdTime)
	{
		ArrayList preses = new ArrayList();
		Iterator groups = resourcePool.values().iterator();
		while (groups.hasNext())
		{
			ResourceGroup group = (ResourceGroup) groups.next();
			// PoolableResource pres = (PoolableResource) group.get(0);
			if (group.match(key))
			{
				if (log.isDebugEnabled())
					log.debug("match key:" + key + ", group: " + group.group());
				PoolableResource npres = apply(group, sn, holdTime);
				if (npres != null) preses.add(npres);
				return preses;
			}
			else if (log.isDebugEnabled())
				log.debug("unmatch key:" + key + ", group: " + group.group());
		}
		return preses;
	}

	protected PoolableResource apply(ResourceGroup group, String sn, int holdTime)
	{
		if (group.size() <= 0) return null; // 如果资源池里面没有资源，则直接返回
		int index = ((int) Math.random() * 10000) % group.size(); // 采用随机数起点，避免顺序查找时由于分配原因导致查找性能变低
		int times = 0;
		while (times < group.size())
		{
			PoolableResource pres = (PoolableResource) group.get(index++);
			if (pres.isAvailable() && pres.hold(sn, holdTime))
			{
				if (register(pres)) return pres; // 登记成功则返回，否则可能是因为同一流水号申请多次导致失败，
				pres.release();
				return null;
			}
			if (index >= group.size()) index = 0;
			times++;
		}
		return null;
	}

	public List getResources(Map param)
	{
		return resources;
	}

	protected synchronized boolean register(PoolableResource pres)
	{
		// 这种情况一般不容易发生，即同一个流水号多次申请，容易导致资源永久泄漏
		if (assignedPool.containsKey(pres.sn))
		{
			log.warn("apply sn: " + pres.sn + " applied & not release!!!");
			return false;
		}
		List preses = (List) assignedPool.get(pres.sn);
		if (preses == null)
		{
			preses = new ArrayList();
			assignedPool.put(pres.sn, preses);
		}
		preses.add(pres);
		return true;
	}

	/**
	 * 用占用流水号来释放申请的资源
	 */
	public boolean release(String sn)
	{
		if (StringX.nullity(sn)) return false;
		List preses = (List) assignedPool.get(sn);
		if (preses == null)
		{
			if (log.isInfoEnabled()) log.info("No sn:" + sn + ", perhaps auto release...");
			return false;
		}
		assignedPool.remove(sn);
		for (int i = 0; i < preses.size(); i++)
		{
			PoolableResource pres = (PoolableResource) preses.get(i);
			if (pres.isAvailable() || !pres.sn.equals(sn))
			{ // 当前资源已经被释放，或者当前资源的占用流水号非当前申请/释放流水号，则不释放
				if (log.isInfoEnabled()) log.info("current res has been allocated, release sn: "
						+ sn + ", hold sn: " + pres.sn);
				return false;
			}
			log.debug("release...");
			pres.release();
		}
		return true;
	}

	/**
	 * 通过Job任务调用的自动回收资源
	 */
	public List recycle()
	{
		List recycleSn = new ArrayList();
		Object[] sns = assignedPool.keySet().toArray();
		if (sns == null || sns.length == 0)
		{
			log.info("no hold sn...");
			return recycleSn;
		}
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < sns.length; i++)
		{
			String sn = (String) sns[i];
			List preses = (List) assignedPool.get(sn);
			PoolableResource pres = (PoolableResource) preses.get(0);
			if (currentTimeMillis - pres.assignTm > pres.holdTm * 1000)
			{ // 452, holdTm单位从毫秒变为秒
				assignedPool.remove(sn);
				for (int j = 0; j < preses.size(); j++)
					((PoolableResource) preses.get(j)).release(); // 释放当前资源为可用
				recycleSn.add(sn);
			}
		}
		if (log.isInfoEnabled())
			log.info("total num of recycle is : " + recycleSn.size() + " of " + sns.length);
		if (recycleSn.size() > 0) log.warn("timeout sn:" + recycleSn);
		return recycleSn;
	}

	public Map checkStatus(Map param)
	{
		String group = StringX.null2emptystr(param.get("group")); // 查看指定组的资源情况
		log.info("check group:" + group);
		Map status = new HashMap();
		Iterator res = resourcePool.values().iterator();
		List resList = new ArrayList();
		while (res.hasNext())
		{
			ResourceGroup resGroup = (ResourceGroup) res.next();
			if (StringX.nullity(group) || group.equals(resGroup.group())) resList.add(resGroup);
		}
		if (resList.size() > 0)
		{
			log.info("res size:" + resList.size());
			status.put("resourcePool", resList);
		}
		else log.info("res is null for " + group);

		Iterator assign = assignedPool.values().iterator();
		List assignList = new ArrayList();
		while (assign.hasNext())
		{
			assignList.add(assign.next());
		}
		if (assignList.size() > 0)
		{
			log.info("assigned pool size:" + assignList.size());
			status.put("assignedPool", assignList);
		}
		else log.info("assigned pool is null for " + group);
		return status;
	}

	public void refresh() throws Exception
	{
		log.info("refresh...");
		if (!needRefresh()) return;

		List resources = loadResource();
		if (resources == null) resources = new ArrayList();
		Map resourcePool = initResPool(resources);

		this.resources = resources;
		this.resourcePool = resourcePool;
		assignedPool = new Hashtable(); // 清空所有已登记信息

		if (log.isInfoEnabled())
			log.info("resources: " + resources.size() + ", resourcePool: " + resourcePool.size());
	}

	protected Map initResPool(List resources) throws Exception
	{
		log.debug("initResPool...");
		if (resources.size() <= 0) log.warn("resources is empty!!!");

		Map resourcePool = new HashMap();
		for (int i = 0; i < resources.size(); i++)
		{
			Resource res = (Resource) resources.get(i);
			PoolableResource pres = new PoolableResource(res);
			ResourceGroup group = (ResourceGroup) resourcePool.get(res.group());
			if (group == null)
			{
				group = new ResourceGroup(pres);
				resourcePool.put(res.group(), group);
			}
			group.add(pres);
		}
		return resourcePool;
	}

	public abstract List loadResource() throws Exception;
}
