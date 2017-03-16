package spc.webos.service.common.impl;

import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import spc.webos.constant.Common;
import spc.webos.service.ZKService;
import spc.webos.service.common.AppRegisterZKService;
import spc.webos.util.JsonUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

/**
 * 将当前应有appcode, jvm登记到zk中，防止相同的应用和jvm编号的应用启动
 * 
 * @author chenjs
 *
 */
public class AppRegisterZKServiceImpl extends ZKService implements AppRegisterZKService
{
	protected CuratorFramework zk;
	@Value("${app.register.persistent?true}")
	protected boolean persistent = true;
	@Value("${app.register.check?true}")
	protected boolean check = true;
	@Value("${app.register.path?/App}")
	protected String path = "/App";
	protected String appPath;

	@Override
	public void init() throws Exception
	{
		if (!check || StringX.nullity(zkHost)) return;
		appPath = path + "/" + SpringUtil.APPCODE + "/" + SpringUtil.JVM;
		zk = zk();
		log.info("register app: {}, host:{}, zk:{}, persistent:{}", appPath,
				SpringUtil.LOCAL_HOST_IP, zkHost, persistent);
		Map<String, Object> jvm = SpringUtil.jvm();
		jvm.put("persistent", persistent);
		try
		{
			if (zk.checkExists().forPath(appPath) == null) zk.create().creatingParentsIfNeeded()
					.withMode(persistent ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL)
					.forPath(appPath, JsonUtil.gson(jvm).getBytes(Common.CHARSET_UTF8));
			else
			{
				log.warn("{} exists in zk: {}", appPath, zkHost);
				zk.close();
				throw new Exception(appPath + " exists in zk: " + zkHost);
			}
		}
		finally
		{
			if (persistent)
			{ // 持久登记模式
				if (zk != null) zk.close();
				zk = null;
			}
		}
	}

	@Override
	public void destroy()
	{
		if (!check || StringX.nullity(zkHost)) return;
		log.info("unregister app: {}, host:{}, zk:{}, persistent:{}", appPath,
				SpringUtil.LOCAL_HOST_IP, zkHost, persistent);
		if (zk != null) zk.close();
		if (persistent && !StringX.nullity(appPath) && !StringX.nullity(zkHost))
		{ // 持久模式
			try (CuratorFramework zk = zk())
			{
				zk.delete().forPath(appPath);
			}
			catch (Exception e)
			{
				log.warn("fail to delete:{}, zk:{}, ex:{}", appPath, zkHost, e);
			}
		}
	}

//	@Override
//	public List<Map<String, Object>> getApps()
//	{
//		List<Map<String, Object>> appInfo = new ArrayList<>();
//		try (CuratorFramework zkcf = zk();)
//		{
//			List<String> apps = new ArrayList<>();
//			List<String> jvms = new ArrayList<>();
//			zkcf.getChildren().forPath(path).forEach((app) -> {
//				apps.add(app);
//			});
//			apps.forEach((app) -> {
//				try
//				{
//					zkcf.getChildren().forPath(path + "/" + app).forEach((jvm) -> {
//						jvms.add(path + "/" + app + "/" + jvm);
//					});
//				}
//				catch (Exception e)
//				{
//				}
//			});
//			jvms.forEach((p) -> {
//				try
//				{
//					appInfo.add((Map<String, Object>) JsonUtil.gson2obj(
//							new String(zkcf.getData().forPath(p).clone(), Common.CHARSET_UTF8)));
//				}
//				catch (Exception e)
//				{
//				}
//			});
//		}
//		catch (Exception e)
//		{
//			log.info("fail to get app info:" + path, e);
//		}
//		log.info("apps:{}", appInfo.size());
//		return appInfo;
//	}

	public void setCheck(boolean check)
	{
		this.check = check;
	}

	public boolean isPersistent()
	{
		return persistent;
	}

	public void setPersistent(boolean persistent)
	{
		this.persistent = persistent;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
}
