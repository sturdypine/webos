package test.tcc.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.jbpm.graph.exe.ProcessInstance;
import org.springframework.stereotype.Service;

import spc.webos.bpl.jbpm3.JBPM3Engine;
import spc.webos.bpl.jbpm3.callback.RMQFlowEndCallback;
import spc.webos.service.BaseService;
import spc.webos.service.seq.UUID;
import spc.webos.tcc.TCCTransactional;
import spc.webos.tcc.TCCTransactional.XidParamPath;
import test.tcc.DemoTCCAService;
import test.tcc.DemoTCCService;

@Service("demoTCCService")
public class DemoTCCServiceImpl extends BaseService implements DemoTCCService {
	@Resource
	DemoTCCAService demoTCCAService;
	JBPM3Engine engine;
	@Resource
	UUID uuid;

	@TCCTransactional
	public String call(@XidParamPath String sn, String name) {
		log.info("sn:{}, name:{}", sn, name);
		return "sn:" + sn + ", " + demoTCCAService.trySayHello1(String.valueOf(uuid.uuid()), name) + "::"
				+ demoTCCAService.trySayHello2(String.valueOf(uuid.uuid()), name);
	}

	@TCCTransactional(asyn = true)
	public String atccCall(String xid, String name) throws Exception {
		Map params = new HashMap();
		params.put("name", name);
		ProcessInstance instance = engine.call("tcc", params,
				new RMQFlowEndCallback(null, "123456", null, "JBPM3-TCC", "tagA", new String[][] { { "name" } }));
		return String.valueOf(instance.getId());
	}

	public void setEngine(JBPM3Engine engine) {
		this.engine = engine;
	}
}
