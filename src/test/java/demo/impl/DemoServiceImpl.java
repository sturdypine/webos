package demo.impl;

import java.util.List;

import demo.DemoService;
import demo.ICallback;
import demo.ValidationParameter;
import spc.webos.advice.log.LogTrace;
import spc.webos.advice.log.LogTrace.LogTraceNoType;
import spc.webos.advice.log.LogTrace.LTPath;
import spc.webos.persistence.jdbc.datasource.DataSource;
import spc.webos.persistence.jdbc.datasource.DataSource.ColumnPath;
import spc.webos.service.BaseService;

public class DemoServiceImpl extends BaseService implements DemoService {
	@LogTrace
	@DataSource(rule = "t_test", jt = true)
	@Override
	public String sayHello(@LTPath @ColumnPath String name) {
		// try {
		// Thread.sleep(200l);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		return "Hello " + name;
	}

	public String sayHello(String name, int age) {
		return "Hello " + name + ":" + age;
	}

	public ValidationParameter save(@ColumnPath("name") ValidationParameter p) {
		log.info("vp:{}", p.getName());
		return p;
	}

	@LogTrace(value = LogTraceNoType.PARAM, location = "xxyy")
	@DataSource(rule = "t_test")
	public ValidationParameter save(String name, List<ValidationParameter> p) {
		log.info("name:{}, vp:{}", name, p);
		return p.get(0);
	}

	public String sayHello2(String name, ICallback callback) {
		// EchoService echo = (EchoService) callback;
		// for (int i = 0; i < 1; i++) {
		// System.out.println("call:" + i + " :: " +
		// callback.callback("sayHello:" + i + "::" + name));
		// }
		// System.out.println("echo: " + echo.$echo("OK?"));
		return "callback hello " + name;
	}
}
