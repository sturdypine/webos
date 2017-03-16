package spc.webos.service.resallocate;

import spc.webos.util.JsonUtil;

/**
 * 可以放入池分配的资源
 * 
 * @author spc
 * 
 */
public class PoolableResource
{
	public Resource resource; // 具体资源类型
	public int holdTm; // 单位秒
	public long assignTm; // 分配时间
	public boolean available = true; // 是否可用，如果为true 表示可用， false表示已经分配
	public String sn; // 占用此资源的服务流水号
	public String group; // 资源组名
	public String id; // 资源ID

	public PoolableResource()
	{
	}

	public PoolableResource(Resource resource)
	{
		this.resource = resource;
		group = resource.group();
		id = resource.id();
	}

	public boolean match(String key)
	{
		return resource.match(key);
	}

	public String group()
	{
		return resource.group();
	}

	/**
	 * 分配该资源
	 * 
	 * @param sn
	 * @param holdTime
	 * @return
	 */
	public synchronized boolean hold(String sn, int holdTime)
	{
		if (!available) return false;
		available = false;
		this.sn = sn;
		this.holdTm = holdTime;
		assignTm = System.currentTimeMillis();
		return true;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	public void release()
	{
		available = true;
		sn = null;
	}

	public String toString()
	{
		return JsonUtil.obj2json(this);
	}

	public Resource getResource()
	{
		return resource;
	}

	public void setResource(Resource resource)
	{
		this.resource = resource;
	}

	public int getHoldTime()
	{
		return holdTm;
	}

	public void setHoldTime(int holdTime)
	{
		this.holdTm = holdTime;
	}

	public long getAssignTime()
	{
		return assignTm;
	}

	public void setAssignTime(long assignTime)
	{
		this.assignTm = assignTime;
	}

	public String getHoldSn()
	{
		return sn;
	}

	public void setHoldSn(String holdSn)
	{
		this.sn = holdSn;
	}
}
