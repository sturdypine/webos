package spc.webos.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import spc.webos.util.JsonUtil;

public class ClusterEndpoint extends AbstractClusterEndpoint
{
	public boolean singleton()
	{
		return singleton;
	}

	public void execute(Executable exe) throws Exception
	{
		int failCount = 0;
		int start = cursor();
		if (retryTimes < 0) retryTimes = endpoints.size(); // 712_20140807
		while (true)
		{
			if (start >= endpoints.size()) start = 0;
			if (execute(((Endpoint) endpoints.get(start)), exe, failCount++)) return;
			start++;
		}
	}

	/**
	 * 获取当前第一个执行实例的坐标
	 * 
	 * @return
	 */
	protected int cursor()
	{
		if (algorithm == 1) return ((int) (cursor++)) % endpoints.size();
		if (algorithm == 2) return (int) (Math.random() * 1000000) % endpoints.size();
		return 0;
	}

	public void init() throws Exception
	{
	}

	public void close() throws Exception
	{
		log.info("destory " + endpoints.size());
		for (int i = 0; i < endpoints.size(); i++)
			((Endpoint) endpoints.get(i)).close();
		;
		endpoints.clear();
	}

	public ClusterEndpoint()
	{
	}

	public ClusterEndpoint(List endpoints)
	{
		this.endpoints = endpoints;
	}

	public ClusterEndpoint(String location) throws Exception
	{
		setLocation(location);
	}

	public static boolean isCluster(String location)
	{
		return location.startsWith("cluster:") || location.indexOf(',') > 0
				|| location.indexOf(';') > 0;
	}

	public void setLocation(String location) throws Exception
	{ // cluster:{alg:1,cursor:0,endpoints:['','']}
		if (location.startsWith("cluster:"))
		{
			Map params = (Map) JsonUtil.json2obj(location.substring(8)); // 去掉cluster:
			if (params.containsKey("singleton")) singleton = (Boolean) params.get("singleton");

			Integer alg = (Integer) params.get("alg");
			if (alg != null) algorithm = alg;
			Integer start = (Integer) params.get("cursor");
			if (start != null) cursor = start;
			List eps = (List) params.get("endpoints");
			for (int i = 0; i < eps.size(); i++)
			{
				Endpoint endpoint = EndpointFactory.getInstance().getEndpoint((String) eps.get(i));
				if (endpoint != null) endpoints.add(endpoint);
			}
		}
		else
		{ // 只是简单的,;分割
			String[] locs = location.split(",|;");
			for (int i = 0; i < locs.length; i++)
			{
				Endpoint endpoint = EndpointFactory.getInstance().getEndpoint(locs[i]);
				if (endpoint != null) endpoints.add(endpoint);
			}
		}
	}

	protected boolean singleton = true;
	protected int algorithm = 1; // 群集算法: 1 延续上一次使用的下一个， 2 随机开始， 0 始终从0开始
	protected long cursor = 0;
	protected List endpoints = new ArrayList();

	public void setEndpoints(List endpoints)
	{
		this.endpoints = endpoints;
	}

	public void setAlgorithm(int algorithm)
	{
		this.algorithm = algorithm;
	}

	public long getCursor()
	{
		return cursor;
	}

	public void setCursor(long cursor)
	{
		this.cursor = cursor;
	}

	public int getAlgorithm()
	{
		return algorithm;
	}

	public List getEndpoints()
	{
		return endpoints;
	}
}
