package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;

import spc.webos.model.ConfigPO;
import spc.webos.util.JsonUtil;

public class GenPO {
	public static void groovy() {
		ConfigPO po = new ConfigPO();
		po.setModel("aaa");
		String formula = "return (a * b) + po.getModel();";
		Map map = new HashMap();
		map.put("a", 900);
		map.put("b", 10);
		map.put("po", po);
		// GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine();
		// Object value = groovyScriptEngine.executeObject(formula, map);
		// System.out.println(value);
	}

	public static void main(String[] args) throws Exception {

		String driver = "org.apache.derby.jdbc.EmbeddedDriver";// 在derby.jar里面
		String dbName = "derby-boc";
		String dbURL = "jdbc:derby:/Users/chenjs/" + dbName + ";create=true;user=boc;password=boc";// create=true表示当数据库不存在时就创建它
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(dbURL);// 启动嵌入式数据库
		conn.close();
		
		String json = "{Header:{msgCd:'com.brh.sso.service.SsoPersistenceService.update'},Body:{args:[{code:'ESB',name:'xxy'}]}}";
		System.out.println(JsonUtil.gson2obj(json));

		ConfigPO po = new ConfigPO();
		po.setModel("aaa");
		BeanWrapperImpl wrapper = new BeanWrapperImpl(false);
		wrapper.setWrappedInstance(po);
		System.out.println(wrapper.getPropertyValue("bean"));

		// VOGenarator gen = new VOGenarator();
		// String dir =
		// "E:/workspace/ibp/product-service/src/main/java/META-INF/persistence/mapping";
		// String srcDir = "E:/workspace/ibp/product-service/src/main/java";
		// boolean valueObject = false;
		// System.out.println("dir = " + dir + ", srcDir = " + srcDir + ",
		// valueObject:"
		// + valueObject);
		//// String json = "com.alibaba.dubbo.common.json.JSON.json";
		// gen.genVO(new File(dir), new File(srcDir), valueObject, null);
		// System.out.println("dir = " + dir + ", srcDir = " + srcDir);
	}
}
