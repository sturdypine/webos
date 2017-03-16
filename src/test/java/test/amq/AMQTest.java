package test.amq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.BytesMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.model.UserPO;
import spc.webos.mq.MQ;
import spc.webos.mq.MQ.Future;
import spc.webos.util.LogUtil;
import spc.webos.util.SpringUtil;
import test.jsc.JSCCallbackService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/test/amq/amq-send.xml", "/test/amq/amq-consumer.xml" })
// @ContextConfiguration("classpath*:META-INF/spring/*.xml")
public class AMQTest
{
	@Resource
	public JmsOperations jms;

	public MQ mq;
	@Resource
	JSCCallbackService jsc;

	@Test
	public void jms() throws Exception
	{
		String seqNb = "000000001";
		String json = "{Header:{sndDt:'20160808', sndTm:'0909009', msgCd:'jsc.getUser', seqNb:'"
				+ seqNb + "',sndAppCd:'EAC'},Body:['cjs']}";

		jms.send("REQ_DOCK", (s) -> {
			BytesMessage msg = s.createBytesMessage();
			msg.setStringProperty(Common.JMS_TRACE_NO, LogUtil.getTraceNo());
			msg.setJMSCorrelationID(seqNb);
			msg.writeBytes(json.getBytes());
			return msg;
		});
	}

	@Test
	public void config() throws Exception
	{
		// jsc.notice();
		System.out.println(AppConfig.getInstance().getProperty("app.api.http.port", 1) + 1);
		List<String> services = AppConfig.getInstance().getProperty("app.login.default.services",
				new ArrayList<String>());
		System.out.println(services);
	}

	@Test
	public void call() throws Exception
	{
		// System.out.println(SpringUtil.getTempfileDir().getAbsolutePath());
		LogUtil.setTraceNo("xxxx", "amq", true);
		Map<String, String> user = new HashMap<>();
		user.put("code", "ESB");
		user.put("name", "bb");
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

	public static void main(String[] args) throws Exception
	{
		// System.out.println(FileUtil.file2base64(new
		// File("/Users/chenjs/Downloads/logo.png")));
		// System.out.println(EndpointFactory.getInstance().getEndpoint("http://129.0.0.1"));

	}

}
