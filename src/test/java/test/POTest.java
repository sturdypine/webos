package test;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Endpoint;

import org.junit.Test;

import com.google.gson.Gson;

import demo.impl.DemoServiceImpl;
import model.NodeServicePO;
import spc.webos.model.ConfigPO;
import spc.webos.persistence.loader.ClassDesc;
import spc.webos.persistence.loader.POItemLoader;
import spc.webos.util.JsonUtil;

public class POTest {
	@Test
	public void ws() throws Exception {
		Endpoint.publish("http://localhost:9090/ws", new DemoServiceImpl());
		Thread.sleep(10000000l);
	}

	public static void main(String[] args) throws Exception {
		String json = "{'code':'status.refresh.common.login','val':'20161105',SEQ:'1'}";
		Gson gson = new Gson();
		ConfigPO conf = gson.fromJson(json, ConfigPO.class);
		System.out.println(conf.getSeq());

		Map<String, Object> request = new HashMap<>();
		request.put("orderNo", "xxxx");
		Map<String, Object> m = new HashMap<String, Object>() {
			public Object get(Object key) {
				return super.get(key.toString().toLowerCase());
			}
		};
		request.forEach((k, v) -> {
			m.put(k.toString().toLowerCase(), v);
			System.out.println("get:"+m.get("orderNo")+","+m);
		});
		System.out.println(m.get("orderNo"));

		// ClassDesc clazzDesc = new ClassDesc();
		// POItemLoader.readPO(clazzDesc, NodeServicePO.class);
		// System.out.println(JsonUtil.obj2json(clazzDesc.getVoProperties()));
		// System.out.println(JsonUtil.obj2json(clazzDesc));
	}
}
