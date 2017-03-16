package test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import demo.DemoService;
import spc.webos.model.UserPO;
import spc.webos.mq.MQ;
import spc.webos.mq.MQ.Future;
import spc.webos.persistence.IPersistence;
import spc.webos.service.alarm.AlarmEventService;
import spc.webos.service.common.AppRegisterZKService;
import spc.webos.service.common.ExtjsService;
import spc.webos.service.job.MasterSlaveJobService;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import test.sso.service.SSOService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:META-INF/spring/common.xml", "classpath:META-INF/spring/ds.xml",
		"classpath:META-INF/spring/aop.xml", "classpath:META-INF/spring/tcc.xml",
		"classpath:META-INF/spring/shard-ds.xml", "classpath:META-INF/spring/esb-ds.xml" })
public class Spring
{
	MasterSlaveJobService job;
	AppRegisterZKService app;
	@Resource
	IPersistence persistence;
	@Resource
	MQ mq;
	@Resource
	AlarmEventService alarm;
	@Resource
	ExtjsService extjsService;

	@Resource
	DemoService demoService;

	@Resource
	SSOService ssoService;

	@Test
	public void dsTM() throws Exception
	{
		// esbTestService.test();
		ssoService.sso();
	}

	@Test
	public void shardDS() throws Exception
	{
		System.out.println(demoService.sayHello("abcdefg"));
	}

	@Test
	public void tree() throws Exception
	{
		System.out.println(extjsService.getExtTree("sso_menu"));
	}

	@Test
	public void sql() throws Exception
	{
		System.out.println(persistence.query("sso_menu", null));
	}

	@Test
	public void alarm() throws Exception
	{
		System.out.println(alarm.proccessEvent("alarm_test"));
	}

	@Test
	public void mq() throws Exception
	{
		LogUtil.setTraceNo("xxxx", "amq", true);
		Map<String, String> user = new HashMap<>();
		user.put("code", "ESB");
		user.put("name", "bb");
		System.out.println("appCode:" + SpringUtil.APPCODE);
		// Object args = Arrays.asList("spc.webos.model.UserPO", user);
		Map args = new HashMap();
		args.put("clazz", "spc.webos.model.UserPO");
		args.put("map", user);
		for (int i = 0; i < 1; i++)
		{
			String sn = "0000000" + i;
			System.out.println(mq.sync("REQ_" + SpringUtil.APPCODE, "jsc.update",
					Arrays.asList("spc.webos.model.UserPO", user), new Future<Integer>(60000)
					{
					}));
			System.out.println(mq.sync("REQ_" + SpringUtil.APPCODE, "jsc.getUser",
					Arrays.asList("cjs"), new Future<List<UserPO>>(60000)
					{
					}));

			// mq.async("REQ_DCK", "jsc.update$2", args, new
			// Callback<Integer>()
			// {
			// public void accept(Integer r, Map<String, String> s)
			// {
			// System.out.println("status:" + s);
			// System.out.println("r:" + r.intValue());
			// }
			// });

			// mq.snd("REQ_ESB", "jsc.update$2", "jsc.callback", args);
			// mq.snd("REQ_ESB", "jsc.notice", "jsc.callback", null);
			// mq.notice("REQ_DCK", "eacVoucherSummaryJob.execute", null);
		}

		Thread.sleep(10 * 1000l);
	}

	@Test
	public void po() throws Exception
	{
		System.out.println(persistence.insertSQL(new UserPO("chenjs")));
	}

	@Test
	public void blob() throws Exception
	{
		// LogDetailPO po = new LogDetailPO();
		// po.setMsgSn("44444");
		// po.setEsbXML(new ByteArrayBlob("AM123456789".getBytes()));
		// po.setOrigBytes(new ByteArrayBlob("AM123456789".getBytes()));
		// po.setSeq(9l);
		// persistence.insert(po);
		// persistence.update(po);

		// po = persistence.find(po);
		// System.out.println(po.getEsbxml().toString());
	}

	@Test
	public void job() throws Exception
	{
		System.out.println("appCd:" + SpringUtil.APPCODE);
		job.leader();
	}

	@Test
	public void app() throws Exception
	{
		System.out.println(app.getNodeValue("/Job/LL"));
	}

	@Test
	public void testSQL() throws Exception
	{
		Map<String, Object> params = new HashMap<>();
		params.put("code", "report");
		params.put("morder", "0");
		Map<String, Object> p = new HashMap<>();
		p.put("code", "admin");
		params.put("params", p);
		params.put("start", 2000);
		params.put("limit", 2000);
		System.out.println("SQL:" + persistence.execute("sso_user", params));
		// System.out.println("SQL:" + persistence.execute("sso_usertotal",
		// params));
		// System.out.println("SQL:" + persistence.execute("sso_menu", params));
		// AlarmPO po = new AlarmPO("alarm_test2");
		// AlarmPO po = new AlarmPO();
		// po.setParam("['msg 1...']");
		// System.out.println(persistence.update(po));
		// System.out.println("PO:" + JsonUtil.obj2json(persistence.find(po)));
	}

	@Test
	public void sqlInjection() throws Exception
	{
		Map<String, Object> params = new HashMap<>();
		// params.put("code", "' or '1'='1");
		params.put("code", "rep or t");
		params.put("morder", "0");
		Map<String, Object> p = new HashMap<>();
		p.put("code", "admin");
		params.put("params", p);
		// params.put("start", 2000);
		// params.put("limit", 2000);
		System.out.println("SQL:" + persistence.execute("sso_user", params));
		// System.out.println("SQL:" + persistence.execute("sso_usertotal",
		// params));
		// System.out.println("SQL:" + persistence.execute("sso_menu", params));
		// AlarmPO po = new AlarmPO("alarm_test2");
		// AlarmPO po = new AlarmPO();
		// po.setParam("['msg 1...']");
		// System.out.println(persistence.update(po));
		// System.out.println("PO:" + JsonUtil.obj2json(persistence.find(po)));
	}

	static void mr1()
	{
		List<String> list = Arrays.asList("a", "b", "a", "c", "d", "c");
		Optional<String> o = list.stream().map((s) -> {
			return s.toUpperCase();
		}).reduce((s1, s2) -> {
			return s1 + "#" + s2;
		});
		System.out.println(o.get());
	}

	static void mr2()
	{
		Stream.of(Arrays.asList(1, 2), Arrays.asList(3, 4)).flatMap(numbers -> numbers.stream())
				.forEach(System.out::print);
	}

	static void mr3()
	{
		List<String> list = Arrays.asList("hello", "hello", "world");
		Map<String, List<String>> collect = list.stream().collect(Collectors.groupingBy(o -> o));
		System.out.println(collect);
		Map<String, Long> counted = list.stream()
				.collect(Collectors.groupingBy(k -> k, Collectors.counting()));
		System.out.println(counted);
	}

	public static void main(String[] args) throws Exception
	{
		// mr1();
		// mr2();
		mr3();
		// Map<String, Object> params = new HashMap<>();
		// params.put("aa", new Long("1111111111111"));
		// params.put("bb", 33333);
		// System.out.println(JsonUtil.obj2json(params));
		// DruidDataSource ds = new DruidDataSource();
		// ds.setDriverClassName(driverClass);
		// ds.getDbType();
		// ds.getDriver();
		// Endpoint endpoint =
		// EndpointFactory.getInstance().getEndpoint("https://boc.bangronghui.com/portal/");
		// Endpoint endpoint =
		// EndpointFactory.getInstance().getEndpoint("https://esb.bangronghui.com/");
		// Endpoint endpoint =
		// EndpointFactory.getInstance().getEndpoint("https://10.211.18.21/");
		// Endpoint endpoint =
		// EndpointFactory.getInstance().getEndpoint("http://10.211.18.10:9111/");
		// Executable exe = new Executable();
		// endpoint.execute(exe);
		// exe.request = "aaaa".getBytes();
		// System.out.println(new String(exe.response));
		// System.out.println(StringX.join(StringX.split("a.b", "."), "/"));
		// Random random = new Random();
		// System.out.println(new Random().nextInt(3000));
		// System.out.println(new Random().nextInt(3000));
		// System.out.println(random.nextInt(5000));
		// System.out.println(random.nextInt(5000));
		// System.out.println(StringX.md5("00000000".getBytes()));
		// System.out.println(StringX.md5("00000000".getBytes()));
		// LogDetailPO po = new LogDetailPO();
		// po.setMsgSn("44444");
		// po.setEsbXML(new ByteArrayBlob("AM123456789".getBytes()));
		// po.setOrigBytes(new ByteArrayBlob("AM123456789".getBytes()));
		// po.setSeq(9L);
		// System.out.println(JsonUtil.obj2json(po));
		//
		// UserPO userVO = new UserPO("chenjs");
		// String json = JsonUtil.obj2json(userVO);
		// userVO = JsonUtil.gson2obj(json, UserPO.class);
		// System.out.println(userVO.getCode() + "\n" + json);
		// Message msg = new Message();
		// msg.setSeqNb("<aaa");
		// System.out.println(msg.toXml(true));
		// String logPoint = "0";
		// MessageAttr attr = new MessageAttr("06");
		// System.out.println("log:" + attr.isLog(logPoint));
		// System.out.println("log:" + attr.isFullLog());
		// System.out.println("uri:" + StringX.uri2params("/js/a/b/c/c1/d/d1",
		// 0));
		/*
		 * SOAPConverter converter = new SOAPConverter(false, null, false,
		 * false); IMessage msg = new Message(); msg.setMsgCd("xxxx");
		 * msg.setInRequest("kkk", "vvv");
		 * msg.setOriginalBytes("123456".getBytes()); byte[] buf =
		 * converter.serialize(msg); System.out.println(new String(buf)); msg =
		 * converter.deserialize(buf);
		 * System.out.println("originalBytes:"+msg.getTransaction().get(
		 * "originalBytes"));
		 * System.out.println("originalBytes:"+msg.getOriginalBytesPlainStr());
		 * System.out.println("originalBytes:"+msg.getOriginalBytes());
		 * System.out.println(new String(new SOAPConverter(false, null, false,
		 * false).serialize(msg)));
		 * System.out.println(ByteArrayBlob.class.isAssignableFrom(Blob.class));
		 * System.out.println(Blob.class.isAssignableFrom(ByteArrayBlob.class));
		 */
	}

}
