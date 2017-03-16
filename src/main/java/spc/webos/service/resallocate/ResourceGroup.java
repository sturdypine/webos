package spc.webos.service.resallocate;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源组，里面存放一个个资源
 * 
 * @author chenjs 2012-01-01
 * 
 */
public class ResourceGroup
{
	public PoolableResource pres; // 具体资源类型, 用于匹配是否符合当前资源组
	public List<PoolableResource> group = new ArrayList<PoolableResource>();

	public ResourceGroup()
	{
	}

	public ResourceGroup(PoolableResource pres)
	{
		this.pres = pres;
	}

	public boolean match(String key)
	{
		return pres.match(key);
	}

	public String group()
	{
		return pres.group();
	}

	public void add(PoolableResource pres)
	{
		group.add(pres);
	}

	public int size()
	{
		return group.size();
	}

	public PoolableResource get(int index)
	{
		return group.get(index);
	}
}
