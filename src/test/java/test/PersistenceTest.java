package test;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import model.NodeServicePO;
import spc.webos.model.ConfigPO;
import spc.webos.model.TccTerminatorPO;
import spc.webos.model.UserPO;
import spc.webos.persistence.IPersistence;
import spc.webos.persistence.jdbc.blob.ByteArrayBlob;
import spc.webos.util.JsonUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:META-INF/spring/common.xml", "classpath:META-INF/spring/ds.xml" })
public class PersistenceTest {
	@Resource
	IPersistence persistence;

	@Test
	public void test3() throws Exception {
		TccTerminatorPO po = new TccTerminatorPO("00019191", 0);
		po.setArgs(new ByteArrayBlob("args:aaaaaaa".getBytes()));
		persistence.insert(po);
	}

	public void test2() throws Exception {
		ConfigPO config = persistence.find(new ConfigPO("db.version"));
		System.out.println(config);
		UserPO user = new UserPO("ESB");
		user.setName("esb");
		persistence.update(user);
//		Map params = new HashMap();
//		params.put(IPersistence.SELECT_ATTACH_TAIL_KEY, " and xx is null")
//		persistence.get(obj, params);
		// List columns = (List) persistence.execute("common.talbecolumns",
		// null);
		// String ftl = new String(FileUtil
		// .is2bytes(new
		// PersistenceTest().getClass().getResourceAsStream("mapping.ftl")));
		// Map root = new HashMap();
		// root.put("talbecolumns", columns);
		// System.out.println(SystemUtil.getInstance().freemarker(ftl, root));
		// VOGenarator gen = new VOGenarator();
		// System.out.println(gen.genMapping(null));
	}

	public void test1() throws Exception {
		NodeServicePO nodeService = persistence.find(new NodeServicePO("TLR", "", "CMS.000100000.01"), true);
		// persistence.invokeEnhance(nodeService);
		System.out.println(nodeService.getAsynRepLocation());
		System.out.println(nodeService.getAttr());

		// String json = new Gson().toJson(nodeService);
		String json = JsonUtil.obj2json(nodeService);
		System.out.println("json:" + json);
		// System.out.println(nodeService.getNode());
		// System.out.println("nodes: " + nodeService.getNodes());
		// NodeServiceVO vo = new Gson().fromJson(json, NodeServiceVO.class);
		// System.out.println("json:" + new Gson().toJson(vo));
	}
}
