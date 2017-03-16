package spc.webos.persistence.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import spc.webos.constant.Common;
import spc.webos.persistence.IPersistence;
import spc.webos.persistence.Persistence;

public class POGenarator
{
	/**
	 * 读取一个配置文件的所有VO配置信息
	 * 
	 * @param location
	 * @param voList
	 * @throws Exception
	 */
	static void readClassXML(InputStream location, List voList) throws Exception
	{
		SAXReader reader = new SAXReader(false);
		Document doc = reader.read(location);
		Element root = doc.getRootElement();
		List classes = root.elements("class");
		for (int i = 0; i < classes.size(); i++)
		{
			Element classElement = (Element) classes.get(i);
			ClassDesc classDesc = new ClassDesc();
			POItemLoader.readProperty(classDesc, classElement);

			// 读取关联属性
			classDesc.setVoProperties(new ArrayList());
			POItemLoader.readResultProperty(classDesc, classElement, "many-to-one"); // 读取many-to-one模式关联的VO属性
			POItemLoader.readResultProperty(classDesc, classElement, "one-to-many"); // 读取one-to-many模式关联的VO属性
			POItemLoader.readResultProperty(classDesc, classElement, "result"); // 读取result属性

			voList.add(classDesc);
		}
		location.close();
	}

	/**
	 * 读取dir文件目录下所有子配置文件
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	List genVO(File dir) throws Exception
	{
		List voList = new ArrayList();
		File[] files = dir.listFiles();
		// System.out.println("dir="+files+", "+dir);
		for (int i = 0; files != null && i < files.length; i++)
		{
			// System.out.println("name="+files[i].getName());
			if (files[i].getName().endsWith("xml"))
				readClassXML(new FileInputStream(files[i]), voList);
		}
		return voList;
	}

	public int genVO(File dir, File srcDir, boolean valueObject, String json) throws Exception
	{
		List voList = genVO(dir);
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(getClass(), "");
		cfg.setDefaultEncoding(Common.CHARSET_UTF8);
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		Template temp = cfg.getTemplate("vo.ftl");
		Map root = new HashMap();

		for (int i = 0; i < voList.size(); i++)
		{
			ClassDesc classDesc = (ClassDesc) voList.get(i);
			List properties = classDesc.getProperties();
			List voProperties = classDesc.getVoProperties();

			int index = classDesc.name.lastIndexOf('.');
			String packageName = classDesc.name.substring(0, index);
			String clazzName = classDesc.name.substring(index + 1);

			File clazzDir = new File(srcDir, "/" + packageName.replace('.', '/'));
			if (!clazzDir.exists())
			{
				clazzDir.mkdirs();
			}
			File clazzFile = new File(clazzDir, clazzName + ".java");
			FileWriter fw = new FileWriter(clazzFile);

			root.put("json", json != null ? json : "spc.webos.util.JsonUtil.obj2json");
			root.put("valueObject", valueObject);
			root.put("vo", classDesc);
			root.put("package", packageName);
			root.put("clazzName", clazzName);
			root.put("classDesc", classDesc);
			root.put("fields", properties);
			root.put("voProperties", voProperties);
			root.put("currentDt", new Date());
			temp.process(root, fw);
			fw.flush();
			fw.close();
		}

		return voList.size();
	}

	public String genMapping(IPersistence persistence, Map params) throws Exception
	{
		if (params == null) params = new HashMap();
		// IPersistence persistence = Persistence.getInstance();
		List list = (List) persistence.execute("common.talbecolumns", params);
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(getClass(), "");
		cfg.setDefaultEncoding(Common.CHARSET_UTF8);
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		Template temp = cfg.getTemplate("mapping.ftl");
		params.put("talbecolumns", list);
		StringWriter sw = new StringWriter();
		temp.process(params, sw);
		return sw.toString();
	}

	public static void gen(String dir, String srcDir, boolean vo) throws Exception
	{
		POGenarator genarator = new POGenarator();
		genarator.genVO(new File(dir), new File(srcDir), vo, null);
	}

	public static void main(String[] args) throws Exception
	{
		String dir = "/Users/chenjs/ycesb/ibp/account-service/src/main/java/META-INF/persistence/mapping";
		String srcDir = "/Users/chenjs/Downloads";
		if (args != null && args.length == 2)
		{
			dir = args[0];
			srcDir = args[1];
		}
		boolean valueObject = false;
		if (args.length > 2) valueObject = new Boolean(args[2]);
		System.out
				.println("dir = " + dir + ",  srcDir = " + srcDir + ", valueObject:" + valueObject);
		String json = "com.alibaba.dubbo.common.json.JSON.json";
		gen(dir, srcDir, false);
		System.out.println("dir = " + dir + ",  srcDir = " + srcDir);
	}
}
