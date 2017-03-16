package spc.webos.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;

import spc.webos.constant.Common;
import spc.webos.exception.Status;
import spc.webos.util.JsonUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public abstract class ZKService extends BaseService
{
	@Value("${app.zk.host?}")
	protected String zkHost;
	@Value("${app.zk.cnnTimeout?30000}")
	protected int zkCnnTimeout = 30000;
	@Value("${app.zk.retryInterval?60000}")
	protected int zkRetryInterval = 60000;
	protected String zkPath;
	protected boolean concurrent = true; // 是否容许并发执行两个集群任务
	protected int awaitTimeoutSec = 1; // 非并发任务情况下， 争抢执行权的等待时间
	protected volatile boolean running; // 是否任务正在运行

	public boolean syn(boolean concurrent, Consumer<LeaderLatch> exe) throws Exception
	{
		log.info("latch:{}, concurrent:{}, zkHost:{}, zkPath:{}", name, concurrent, zkHost, zkPath);
		Map<String, Object> value = new HashMap<>();
		try (CuratorFramework zk = zk())
		{
			if (zk.checkExists().forPath(zkPath) == null)
			{
				try
				{
					value.put("app", SpringUtil.APPCODE);
					value.put("jvm", SpringUtil.JVM);
					value.put("name", name);
					value.put("zkPath", zkPath);
					if (!StringX.nullity(remark)) value.put("remark", remark);
					value.put("concurrent", concurrent);
					zk.create().creatingParentsIfNeeded().forPath(zkPath,
							JsonUtil.obj2json(value).getBytes(Common.CHARSET_UTF8));
				}
				catch (Exception e)
				{ // 同时创建会报错
					if (zk.checkExists().forPath(zkPath) == null) throw e;
				}
			}
			boolean suc = false;
			try (LeaderLatch latch = new LeaderLatch(zk, zkPath))
			{
				latch.start();
				if (concurrent) latch.await();
				else latch.await(awaitTimeoutSec, TimeUnit.SECONDS);
				if (!latch.hasLeadership())
				{
					log.info("await timeout: {} seconds", awaitTimeoutSec);
					return suc;
				}
				log.info("{} start, I'm leader...", name);
				value = (Map<String, Object>) JsonUtil.gson2obj(
						new String(zk.getData().forPath(zkPath).clone(), Common.CHARSET_UTF8));
				if (!isExe(value))
				{
					log.info("exe is false: {}", value);
					return false;
				}
				running = true;
				setStartValue(zk, value);
				exe.accept(latch);
				log.info("{} success to finish...", name);
				suc = true;
				setEndValue(zk, value, null);
			}
			catch (Throwable t)
			{
				log.info("{} fail to finish, ex:{}", name, t.toString());
				setEndValue(zk, value, t);
			}
			finally
			{
				running = false;
			}
			return suc;
		}
	}

	// 获得执行权后是否真正执行，集群任务下可能有一个执行了，另一个在某段时间内就不需要再执行了
	protected boolean isExe(Map<String, Object> value)
	{
		return true;
	}

	protected void setStartValue(CuratorFramework zk, Map<String, Object> value)
	{
		try
		{
			value.put("startTm",
					FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date()));
			value.remove("endTm");
			value.put("app", SpringUtil.APPCODE);
			value.put("jvm", SpringUtil.JVM);
			value.put("status", spc.webos.exception.Status.STATUS_UNDERWAY);
			value.remove("ex");
			if (!StringX.nullity(remark)) value.put("remark", remark);
			zk.setData().forPath(zkPath, JsonUtil.obj2json(value).getBytes(Common.CHARSET_UTF8));
		}
		catch (Exception e)
		{
			log.info("fail to setValue:" + e);
		}
	}

	protected void setEndValue(CuratorFramework zk, Map<String, Object> value, Throwable ex)
	{
		try
		{
			value.put("endTm",
					FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date()));
			value.put("endMillis", String.valueOf(System.currentTimeMillis()));
			if (ex == null)
			{
				value.put("status", Status.STATUS_SUCCESS);
			}
			else
			{
				value.put("status", Status.STATUS_FAIL);
				value.put("ex", JsonUtil.obj2json(SpringUtil.ex2status("", ex)));
			}
			zk.setData().forPath(zkPath, JsonUtil.obj2json(value).getBytes(Common.CHARSET_UTF8));
		}
		catch (Exception e)
		{
			log.info("fail to setValue:" + e);
		}
	}

	public List<Map<String, Object>> getNodeValue(String path)
	{
		List<Map<String, Object>> list = new ArrayList<>();
		try (CuratorFramework zkcf = zk();)
		{
			zkcf.getChildren().forPath(path).forEach((node) -> {
				try
				{
					String value = new String(zkcf.getData().forPath(path + "/" + node).clone(),
							Common.CHARSET_UTF8);
					if (StringX.nullity(value)) list.addAll(getNodeValue(path + "/" + node));
					else list.add((Map<String, Object>) JsonUtil.gson2obj(value));
				}
				catch (Exception e)
				{
				}
			});
		}
		catch (Exception e)
		{
		}
		return list;
	}

	public boolean select(boolean concurrent, Consumer<CuratorFramework> exe) throws Exception
	{
		log.info("select:{}, concurrent:{}, zkHost:{}, zkPath:{}", name, concurrent, zkHost,
				zkPath);
		CuratorFramework zk = zk();
		if (zk.checkExists().forPath(zkPath) == null)
			zk.create().creatingParentsIfNeeded().forPath(zkPath, "Mutex".getBytes());
		StringBuilder suc = new StringBuilder();
		final LeaderSelector leader = new LeaderSelector(zk, zkPath, new LeaderSelectorListener()
		{
			public void takeLeadership(CuratorFramework client) throws Exception
			{
				log.info("{}  start, I'm leader...", name);
				try
				{
					running = true;
					log.info("{} start, I'm leader...", name);
					exe.accept(zk);
					log.info("{} success to finish...", name);
				}
				catch (Exception e)
				{
					suc.append(false);
					log.info(name + " job fail", e);
				}
				finally
				{
					// leader.close(); // 关闭线程
				}
			}

			public void stateChanged(CuratorFramework client, ConnectionState newState)
			{
			}
		});
		leader.autoRequeue();
		leader.start();
		return suc.length() == 0;
	}

	protected CuratorFramework zk()
	{
		CuratorFramework zkcf = CuratorFrameworkFactory.builder().connectString(zkHost)
				.connectionTimeoutMs(zkCnnTimeout).sessionTimeoutMs(Integer.MAX_VALUE)
				.canBeReadOnly(false).retryPolicy(new ExponentialBackoffRetry(zkRetryInterval, 29))
				.defaultData(null).build();
		zkcf.start();
		return zkcf;
	}

	public void setZkHost(String zkHost)
	{
		this.zkHost = zkHost;
	}

	public void setZkPath(String zkPath)
	{
		this.zkPath = zkPath;
	}

	public void setZkCnnTimeout(int zkCnnTimeout)
	{
		this.zkCnnTimeout = zkCnnTimeout;
	}

	public void setZkRetryInterval(int zkRetryInterval)
	{
		this.zkRetryInterval = zkRetryInterval;
	}

	public void setConcurrent(boolean concurrent)
	{
		this.concurrent = concurrent;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setAwaitTimeoutSec(int awaitTimeoutSec)
	{
		this.awaitTimeoutSec = awaitTimeoutSec;
	}
}
