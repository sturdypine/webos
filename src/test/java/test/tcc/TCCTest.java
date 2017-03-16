package test.tcc;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.JedisCommands;
import redis.clients.util.Pool;
import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.model.TccTransactionPO;
import spc.webos.service.job.MasterSlaveJobService;
import spc.webos.tcc.TCCTransactional;
import spc.webos.tcc.TccAdvice;
import spc.webos.util.FileUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import test.tcc.impl.DemoTCCAServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:META-INF/spring/common.xml", "classpath:META-INF/spring/ds.xml",
		"classpath:META-INF/spring/aop.xml", "classpath:META-INF/spring/tcc.xml",
		"classpath:META-INF/spring/tcc-test.xml" })
public class TCCTest
{
	@Resource
	DemoTCCService demoTCCService;
	// @Resource
	TccAdvice tccAdvice;
	@Resource
	DemoTCCAService demoTCCAService;
	// @Resource
	protected Pool ssdbPool;

	@Autowired(required = false)
	@Qualifier("leaderLatchMSJobService")
	MasterSlaveJobService job;
	
	@Test
	public void syn() throws Exception
	{
		try
		{
			System.out.println(demoTCCService.call("222222", "chenjs"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Thread.sleep(5000l);
	}

	@Test
	public void aop() throws Exception
	{
		demoTCCAService.confirmSayHello2("0002", "cjs");
	}
	
	@Test
	public void job() throws Exception
	{
		// job.leader();
		// Map<String, Object> def = new HashMap<>();
		Map<String, Object> json = AppConfig.getInstance().getProperty("app.json", new HashMap<>());
		System.out.println(json);
		Thread.sleep(100000000l);
	}

	public void ssdb() throws Exception
	{
		String prefix = "tcc:";
		String group = "test";
		String tccPrefixKey = prefix + group + ":";
		String failTccKey = prefix + group + ":fail";
		String allTccKey = prefix + group + ":all";
		String xid = "test-1609061410284390088";
		String key = tccPrefixKey + xid;
		System.out.println("key:" + key);

		try (Closeable jedis = (Closeable) ssdbPool.getResource())
		{
			if (((JedisCommands) jedis).exists(key))
				throw new AppException(AppRetCode.TCC_XID_REPEAT, new Object[] { xid });

			// ((JedisCommands) jedis).hmset(key, (Map<String, String>)
			// JsonUtil.gson2obj("{a:'1',b:'2'}"));
			System.out.println(((JedisCommands) jedis).hgetAll(key));
			((JedisCommands) jedis).lpush(allTccKey, xid);
			// System.out.println(((JedisCommands) jedis).);
		}
	}

	@Test
	public void repository() throws Exception
	{
		StringX.split("", ",");
		System.out.println(tccAdvice.getRepository().find("test-1609070925113830088"));
		System.out.println(tccAdvice.getRepository().findErr(0, 25));
		// System.out.println(repository.findErr(0, 500));
	}

	@Test
	public void serialize() throws Exception
	{
		TccTransactionPO po = new TccTransactionPO("aaaaa");
		po.setEx("exx::");
		po.setGname("test");
		String base64 = StringX.base64(FileUtil.serialize(po, false));
		System.out.println("base64:" + base64);
		po = (TccTransactionPO) FileUtil.deserialize(StringX.decodeBase64(base64), false);
		System.out.println("po:" + po.getXid() + "," + po.getEx() + ", " + po.getGname());
	}


	@Test
	public void method() throws Exception
	{
		DemoTCCAServiceImpl s = new DemoTCCAServiceImpl();
		Method m = s.getClass().getMethod("trySayHello1",
				new Class[] { String.class, String.class });
		System.out.println(m.toString());

		TCCTransactional tcc = m.getAnnotation(TCCTransactional.class);

		System.out.println("tcc:" + tcc);
		System.out.println("tcc2:" + m.getDeclaredAnnotation(TCCTransactional.class));
	}

	public void asyn() throws Exception
	{

		DemoTCCService tcc = (DemoTCCService) SpringUtil.getInstance().getBean("demoTCC", null);
		try
		{
			System.out.println(tcc.atccCall("123456", "chenjs"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
		// Thread.sleep(100000000l);
	}
}
