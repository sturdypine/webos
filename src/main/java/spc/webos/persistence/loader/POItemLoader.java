package spc.webos.persistence.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.cglib.beans.BulkBean;
import spc.webos.persistence.BulkBeanItem;
import spc.webos.persistence.IPersistence;
import spc.webos.persistence.SQLItem;
import spc.webos.persistence.Script;
import spc.webos.util.FTLUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

/**
 * 对象模式SqlItem配置项加载
 * 
 * @author Hate
 * 
 */
public class POItemLoader
{
	/**
	 * 读取一个目录下的所有文件
	 * 
	 * @param voSQLMap
	 *            直接对象方式访问的SQL项的配置
	 * @param voMapping
	 *            每个vo属性中关联其他vo属性的配置信息
	 * @throws Exception
	 */
	public static void readMappingDir(Resource path, Map voSQLMap, Map voMapping) throws Exception
	{
		File file = path.getFile();
		if (!file.exists()) log.warn("cannot load mapping file dir: " + file.getAbsolutePath());
		File[] files = file.listFiles();
		for (int i = 0; files != null && i < files.length; i++)
		{
			try
			{
				if (files[i].getName().endsWith("xml"))
				{
					readClassXML(new FileInputStream(files[i]), voSQLMap, voMapping);
					log.info("loaded po file:{}", files[i].getName());
				}
			}
			catch (Exception e)
			{
				System.err.println("load file failure:" + files[i].getName());
				throw e;
			}
		}
	}

	/**
	 * 读取一个配置文件中所有的Class说明
	 * 
	 * @param location
	 * @param voSQLMap
	 * @throws Exception
	 */
	public static void readClassXML(InputStream location, Map<Class<?>, SQLItem[]> voSQLMap,
			Map<Class<?>, BulkBeanItem> voMapping) throws Exception
	{
		SAXReader reader = new SAXReader(false);
		Document doc = reader.read(location);
		Element root = doc.getRootElement();
		String slave = root.attributeValue("slave");
		String ds = root.attributeValue("ds");
		if (StringX.nullity(ds)) ds = root.attributeValue("jt");
		List classes = root.elements("class");
		for (int i = 0; i < classes.size(); i++)
		{
			Element classElement = (Element) classes.get(i);
			ClassDesc classDesc = new ClassDesc();

			classDesc.ds = ds;
			classDesc.slave = slave;
			
			readProperty(classDesc, classElement); // 读取一个VO类的配置
			try
			{ // modified by chenjs 2011-12-01 如果类不存在则日志警告并跳过配置信息
				Class clazz = Class.forName(classDesc.getName(), false,
						Thread.currentThread().getContextClassLoader());

				// process... 生成SQL模板
				voSQLMap.put(clazz, process(classDesc));

				// 读取Vo中与其他Vo关联的属性名
				classDesc.setVoProperties(new ArrayList());
				readResultProperty(classDesc, classElement, "many-to-one"); // 读取many-to-one模式关联的VO属性
				readResultProperty(classDesc, classElement, "one-to-many"); // 读取one-to-many模式关联的VO属性
				readResultProperty(classDesc, classElement, "result"); // 读取result属性
				if (classDesc.getVoProperties().size() > 0)
					voMapping.put(clazz, convert(classDesc));
			}
			catch (NoClassDefFoundError | ClassNotFoundException cnfe)
			{
				log.warn("NoClassDefFoundError | ClassNotFoundException: {}, {}",
						classDesc.getName(), cnfe.toString());
				continue;
			}
		}
		location.close();
	}

	/**
	 * 把一个VO中关联属性的配置信息由List中的ClassVOPropertyDesc格式， 变成List中的BulkBeanItem格式，
	 * 提高Persistence中使用的性能
	 * 
	 * @param classDesc
	 * @return
	 * @throws Exception
	 */
	static BulkBeanItem convert(ClassDesc classDesc) throws Exception
	{
		BulkBeanItem bulkBeanItem = new BulkBeanItem();
		List voProperties = classDesc.getVoProperties();
		// 1. 或许VO的所有关联的VO或者List的属性信息
		Class clazz = Class.forName(classDesc.name, false,
				Thread.currentThread().getContextClassLoader());
		Class[] clazzArray = new Class[voProperties.size()];
		String[] setter = new String[voProperties.size()];
		String[] getter = new String[voProperties.size()];
		for (int i = 0; i < voProperties.size(); i++)
		{
			ResultPropertyDesc propertyDesc = (ResultPropertyDesc) voProperties.get(i);
			clazzArray[i] = clazz.getDeclaredField(propertyDesc.getName()).getType();
			String name = StringUtils.capitalize(propertyDesc.getName());
			setter[i] = "set" + name;
			getter[i] = "get" + name;
		}
		bulkBeanItem.setVoProperties(BulkBean.create(clazz, getter, setter, clazzArray));

		// 每一个关联VO属性 或者是 Sql查询属性
		List prop = new ArrayList(voProperties.size());
		for (int i = 0; i < voProperties.size(); i++)
		{
			Object[] bulkBeans = new Object[5]; // 900_20160115 放入属性名到第5个位置
			prop.add(bulkBeans);
			ResultPropertyDesc propertyDesc = (ResultPropertyDesc) voProperties.get(i);
			bulkBeans[4] = propertyDesc.getName(); // 900_20160115 放入属性名到第5个位置
			bulkBeans[2] = clazz.getDeclaredField(propertyDesc.getName()).getType();
			if (propertyDesc.getSelect() != null)
			{ // 此属性不是关联其他VO属性， 而是一个普通的sql属性
				bulkBeans[0] = propertyDesc.getSelect();
				continue;
			}
			// 只针对关联属性是其他VO时才有用
			bulkBeans[3] = Class.forName(propertyDesc.getJavaType(), false,
					Thread.currentThread().getContextClassLoader());

			// 建立自身VO属性和关联的VO属性之间的外键关系
			// 自身属性
			clazzArray = new Class[propertyDesc.getter.length];
			setter = new String[propertyDesc.getter.length];
			getter = new String[propertyDesc.getter.length];
			for (int j = 0; j < propertyDesc.getter.length; j++)
			{
				clazzArray[j] = clazz.getDeclaredField(propertyDesc.getter[j]).getType();
				String name = StringUtils.capitalize(propertyDesc.getter[j]);
				setter[j] = "set" + name;
				getter[j] = "get" + name;
			}
			bulkBeans[0] = BulkBean.create(clazz, getter, setter, clazzArray);

			// 关联VO的属性
			setter = new String[propertyDesc.getter.length];
			getter = new String[propertyDesc.getter.length];
			for (int j = 0; j < propertyDesc.getter.length; j++)
			{
				String name = StringUtils.capitalize(propertyDesc.setter[j]);
				setter[j] = "set" + name;
				getter[j] = "get" + name;
			}
			bulkBeans[1] = BulkBean.create(
					Class.forName(propertyDesc.getJavaType(), false,
							Thread.currentThread().getContextClassLoader()),
					getter, setter, clazzArray);
		}
		bulkBeanItem.setProperties(prop);
		return bulkBeanItem;
	}

	/**
	 * 获取Vo中关联其他VO属性的配置信息， 也包括读取其他Sql配置作为结果的属性
	 * 
	 */
	static void readResultProperty(ClassDesc classDesc, Element classElement, String type)
			throws Exception
	{
		List property = classElement.elements(type);
		for (int i = 0; i < property.size(); i++)
		{
			Element ele = (Element) property.get(i);
			ResultPropertyDesc propertyDesc = new ResultPropertyDesc();
			propertyDesc.setName(ele.attributeValue("name")); // 900_20160105
																// 从原来配置property统一变为name
			propertyDesc.setJavaType(ele.attributeValue("javaType", "String"));
			propertyDesc.setSelect(ele.attributeValue("select"));
			propertyDesc.setRemark(ele.attributeValue("remark"));
			propertyDesc.setManyToOne(true);
			if (propertyDesc.getSelect() == null)
			{
				propertyDesc.setManyToOne(type.equals("many-to-one"));
				readVOPropertyRelation(propertyDesc, ele);
			}
			classDesc.getVoProperties().add(propertyDesc);
		}
	}

	/**
	 * 获取VO间的属性关系, 相当于表间的外键关系。 即此表(VO)中的那些字段联系着关联表(VO)中的哪些字段的信息
	 * 
	 */
	static void readVOPropertyRelation(ResultPropertyDesc propertyDesc, Element propertyElement)
			throws Exception
	{
		List properties = propertyElement.elements("map");
		if (properties != null && properties.size() > 0)
		{
			propertyDesc.setter = new String[properties.size()];
			propertyDesc.getter = new String[properties.size()];
			for (int i = 0; i < properties.size(); i++)
			{ // 900_20160105 由原来的getter/setter配置修改为 from/to
				Element ele = (Element) properties.get(i);
				propertyDesc.getter[i] = ele.attributeValue("from");
				propertyDesc.setter[i] = ele.attributeValue("to", propertyDesc.getter[i]); // 默认两VO的属性名相同
			}
		}
		else if (!StringX.nullity(propertyElement.attributeValue("map")))
		{
			String[] map = StringX.split(propertyElement.attributeValue("map"), ",");
			propertyDesc.getter = new String[map.length];
			propertyDesc.setter = new String[map.length];
			for (int i = 0; i < map.length; i++)
			{
				int idx = map[i].indexOf(':');
				if (idx > 0)
				{
					propertyDesc.getter[i] = map[i].substring(0, idx).trim();
					propertyDesc.setter[i] = map[i].substring(idx + 1).trim();
				}
				else propertyDesc.getter[i] = propertyDesc.setter[i] = map[i].trim();
			}
		}
		else
		{ // 容许直接使用属性进行设置
			propertyDesc.getter = StringX.split(propertyElement.attributeValue("from"), ",");
			String to = propertyElement.attributeValue("to");
			propertyDesc.setter = StringX.nullity(to) ? propertyDesc.getter
					: StringX.split(to, ",");
		}
	}

	/**
	 * 读取一个类的所有描述信息, 如果此类中含有数据库manual sequence字段（只能有一个sequence字段） 返回一个String[]
	 * {0:库的表名， 1：字段名}的对象 CacheManager cacheManager,
	 * 
	 * @param clazzDesc
	 * @param classElement
	 * @throws Exception
	 */
	public static void readProperty(ClassDesc clazzDesc, Element classElement) throws Exception
	{
		List properties = new ArrayList();
		clazzDesc.setProperties(properties);
		clazzDesc.setName(classElement.attributeValue("name"));
		clazzDesc.setTable(classElement.attributeValue("table"));

		String ds = classElement.attributeValue("ds");
		if (StringX.nullity(ds)) ds = classElement.attributeValue("jt");
		if (!StringX.nullity(ds)) clazzDesc.ds = ds;
		
		String slave = classElement.attributeValue("slave");
		if (!StringX.nullity(slave)) clazzDesc.slave = slave;

		clazzDesc.setParent(classElement.attributeValue("parent"));
		clazzDesc.setRemark(classElement.attributeValue("remark"));
		String insertMode = classElement.attributeValue("insertMode");
		if (!StringX.nullity(insertMode)) clazzDesc.setInsertMode(Integer.parseInt(insertMode));

		// 获取static fields
		Element staticFields = classElement.element("static");
		if (staticFields != null)
		{
			List p = staticFields.elements("p");
			for (int i = 0; i < p.size(); i++)
				clazzDesc.getStaticFields().add(((Element) p.get(i)).getText().trim());
		}

		// 获取declare信息
		Element declare = classElement.element("declare");
		if (declare != null) clazzDesc.setDeclare(declare.getText());

		declare = classElement.element("insert-preFn");
		if (declare != null) clazzDesc.setInsertPreFn(declare.getText());
		declare = classElement.element("insert-postFn");
		if (declare != null) clazzDesc.setInsertPostFn(declare.getText());

		declare = classElement.element("update-preFn");
		if (declare != null) clazzDesc.setUpdatePreFn(declare.getText());
		declare = classElement.element("update-postFn");
		if (declare != null) clazzDesc.setUpdatePostFn(declare.getText());

		declare = classElement.element("select-preFn");
		if (declare != null) clazzDesc.setSelectPreFn(declare.getText());
		declare = classElement.element("select-postFn");
		if (declare != null) clazzDesc.setSelectPostFn(declare.getText());

		declare = classElement.element("delete-preFn");
		if (declare != null) clazzDesc.setDeletePreFn(declare.getText());
		declare = classElement.element("delete-postFn");
		if (declare != null) clazzDesc.setDeletePostFn(declare.getText());

		Map columnConverter = new HashMap();
		// 获许ValueObject的属性信息
		List property = classElement.elements("property");
		for (int i = 0; i < property.size(); i++)
		{
			Element ele = (Element) property.get(i);
			ClassPropertyDesc cpd = new ClassPropertyDesc();

			String name = ele.attributeValue("name");
			String column = ele.attributeValue("column");
			if (StringX.nullity(name) && !StringX.nullity(column) && column.indexOf('_') > 0)
			{ // 900, 支持column中间有下划线，然后自动生成java bean属性名
				String[] words = StringX.split(column, "_");
				name = words[0].toLowerCase();
				for (int j = 1; j < words.length; j++)
					name += words[j].substring(0, 1).toUpperCase()
							+ words[j].substring(1).toLowerCase();
				cpd.setName(name);
			}
			else
			{
				cpd.setName(name);
				cpd.setColumn(ele.attributeValue("column", cpd.getName()));
				if (StringX.nullity(cpd.getColumn())) cpd.setColumn(cpd.getName().toUpperCase());
			}

			cpd.version = new Boolean(ele.attributeValue("version", "false")).booleanValue(); // 810,
																								// 乐观锁
			cpd.setUpdatable(new Boolean(ele.attributeValue("updatable", "true")).booleanValue()); // 900
																									// 支持不可修改
			cpd.setPrimary(new Boolean(ele.attributeValue("primary", "false")).booleanValue());
			cpd.setPrepare(new Boolean(ele.attributeValue("prepare", "false")).booleanValue());
			cpd.setUuid(new Boolean(ele.attributeValue("uuid", "false")).booleanValue());
			if (cpd.isUuid()) cpd.setUpdatable(false); // 900_201060107
														// UUID字段不能修改
			String jdbcType = ele.attributeValue("jdbcType");
			if (jdbcType != null) cpd.setJdbcType(jdbcType.toUpperCase());
			cpd.setJavaType(ele.attributeValue("javaType", "String"));
			if (cpd.getJavaType().equalsIgnoreCase("IBlob")) cpd.setPrepare(true);
			String converter = ele.attributeValue("converter", null);
			if (converter != null) columnConverter.put(cpd.getName(), converter);
			if (cpd.getJavaType().equalsIgnoreCase("CompositeNode")) // 如果VO属性为CompositeNode那么必须采用XML转换
			{
				columnConverter.put(cpd.getName(), "XML");
				cpd.setPrepare(true); // 由于xml里面可能包含'所有采用prepare方式入库
			}
			if (cpd.isPrepare()) clazzDesc.setPrepare(true);
			cpd.setDefaultValue(ele.attributeValue("default"));
			String sequence = StringX.null2emptystr(ele.attributeValue("sequence"));
			if (!StringX.nullity(sequence))
			{
				cpd.setJavaType("Long");
				cpd.setSequence(sequence.toUpperCase());
				cpd.setUpdatable(false); // 900_20160107 增长字段默认不容许修改
				// 900_20160725 所有自增字段都必须不是数据库机制
				clazzDesc.sequence = true;
			}
			cpd.setNullValue(ele.attributeValue("nullValue"));
			cpd.setRemark(ele.attributeValue("remark"));
			readExp(cpd, ele); // 读取insert, update, select表达式
			properties.add(cpd);
		}
		if (columnConverter.size() > 0) clazzDesc.setColumnConverter(columnConverter);
	}

	public static void readPO(Class po, Map<Class<?>, SQLItem[]> voSQLMap,
			Map<Class<?>, BulkBeanItem> voMapping)
	{
		log.info("PO annotation:{}", po); // 多线程下可能会并发打印
		ClassDesc classDesc = new ClassDesc();
		classDesc.name = po.getName();
		try
		{
			if (!readPO(classDesc, po))
			{
				log.info("PO annotation fail:{}", po);
				return; // 读取一个PO类的配置
			}

			// process... 生成SQL模板
			voSQLMap.put(po, process(classDesc));

			// 是否有关联属性
			if (classDesc.getVoProperties().size() > 0) voMapping.put(po, convert(classDesc));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean readPO(ClassDesc clazzDesc, Class po) throws Exception
	{
		if (po.getAnnotation(Entity.class) == null) return false;
		Table table = (Table) po.getAnnotation(Table.class);
		if (table == null) return false;
		clazzDesc.table = table.name();
		List properties = new ArrayList();
		clazzDesc.setProperties(properties);
		clazzDesc.setVoProperties(new ArrayList());
		Field[] fields = po.getDeclaredFields();
		for (Field f : fields)
		{
			if (f.getAnnotation(Column.class) == null && f.getAnnotation(JoinColumn.class) == null)
				continue;
			ClassPropertyDesc cpd = new ClassPropertyDesc();
			cpd.name = f.getName();
			Annotation[] anns = f.getAnnotations();
			for (Annotation ann : anns)
			{
				if (ann instanceof Id) cpd.primary = true;
				else if (ann instanceof Version) cpd.version = true;
				else if (ann instanceof Column)
				{
					Column col = (Column) ann;
					cpd.column = col.name();
					if (StringX.nullity(cpd.column)) cpd.column = cpd.name;
					cpd.javaType = f.getType().getSimpleName();
					Map<String, Object> attr = new HashMap<>();
					if (!StringX.nullity(col.columnDefinition()))
						attr = (Map<String, Object>) JsonUtil.gson2obj(col.columnDefinition());
					cpd.prepare = attr.containsKey("prepare") && (Boolean) attr.get("prepare");
					if (cpd.prepare) clazzDesc.prepare = true;
					properties.add(cpd);
				}
				else if (ann instanceof JoinColumn)
				{ // 关联属性
					JoinColumn jcol = (JoinColumn) ann;
					ResultPropertyDesc propertyDesc = new ResultPropertyDesc();
					propertyDesc.setName(cpd.name);

					propertyDesc.javaType = f.getGenericType().getTypeName();
					int idx = propertyDesc.javaType.indexOf('<');
					if (idx > 0) propertyDesc.javaType = propertyDesc.javaType.substring(idx + 1,
							propertyDesc.javaType.length() - 1);
					// System.out.println("JoinColumn:" + cpd.name + ",
					// "
					// + f.getGenericType().getTypeName() + ", " +
					// propertyDesc.javaType);

					// 不是配置的ManyToOne，就认为都是OneToMany
					propertyDesc.setManyToOne(f.getAnnotation(ManyToOne.class) != null);

					// JPA只能支持一个字段的关联属性
					propertyDesc.getter = new String[] { jcol.name() };
					propertyDesc.setter = new String[] {
							StringX.nullity(jcol.referencedColumnName()) ? jcol.name()
									: jcol.referencedColumnName() };

					clazzDesc.getVoProperties().add(propertyDesc);
				}
			}
		}
		return true;
	}

	/**
	 * 读取select,update,insert表达式
	 * 
	 * @param classPropertyDesc
	 * @param propertyElement
	 * @throws Exception
	 */
	public static void readExp(ClassPropertyDesc classPropertyDesc, Element propertyElement)
			throws Exception
	{
		List exp = propertyElement.elements("select");
		if (exp != null && exp.size() == 1)
		{
			String selectExp = ((Element) exp.get(0)).getText();
			classPropertyDesc.setSelect(SQLItemXmlLoader.formatSQL(selectExp).trim());
		}
		exp = propertyElement.elements("update");
		if (exp != null && exp.size() == 1)
		{
			String updateExp = ((Element) exp.get(0)).getText();
			classPropertyDesc.setUpdate(SQLItemXmlLoader.formatSQL(updateExp).trim());
		}
		exp = propertyElement.elements("insert");
		if (exp != null && exp.size() == 1)
		{
			String insertExp = ((Element) exp.get(0)).getText();
			classPropertyDesc.setInsert(SQLItemXmlLoader.formatSQL(insertExp).trim());
		}
	}

	/**
	 * 生成SQL模板
	 * 
	 * @param classDesc
	 *            Java类描述
	 * @param properties
	 *            Java类属性描述
	 * @param voSQLMap
	 * @throws Exception
	 */
	public static SQLItem[] process(ClassDesc classDesc) throws Exception
	{
		SQLItem[] items = new SQLItem[4];
		StringWriter out = new StringWriter();
		try (InputStreamReader isr = new InputStreamReader(
				new POItemLoader().getClass().getResourceAsStream("sqlitem.ftl")))
		{
			Template t = new Template("SQL_ITEM", isr, null);
			Map root = new HashMap();
			// 900, 默认使用oracle
			root.put(IPersistence.DB_TYPE_KEY, "ORACLE"); // persistence.getDefautlJdbcTemplate().getDbType());
			root.put("classDesc", classDesc);
			root.put("properties", classDesc.getProperties());

			items[SQLItem.SELECT] = genSQLItem(root, t, out, classDesc, SQLItem.SELECT);
			items[SQLItem.DELETE] = genSQLItem(root, t, out, classDesc, SQLItem.DELETE);
			items[SQLItem.INSERT] = genSQLItem(root, t, out, classDesc, SQLItem.INSERT);
			items[SQLItem.UPDATE] = genSQLItem(root, t, out, classDesc, SQLItem.UPDATE);
			// System.out.println("VOSQL:update:"+items[SQLItem.UPDATE].sql);
			return items;
		}
	}

	static SQLItem genSQLItem(Map root, Template temp, StringWriter out, ClassDesc classDesc,
			short type) throws Exception
	{
		root.put("sqlType", String.valueOf(type));
		out.getBuffer().setLength(0);
		temp.process(root, out);
		out.flush();
		SQLItem item = new SQLItem();
		item.columnConverters = classDesc.getColumnConverter();
		item.resultClass = classDesc.getName();
		item.jt = classDesc.getDataSource();
		item.slave = classDesc.slave;
		if (item.type == SQLItem.INSERT) item.sequnce = classDesc.sequence; // 900,
																			// 增加时如果需要唯一号
		item.sql = SQLItemXmlLoader.formatSQL(out.toString());
		Configuration cfg = new Configuration();
		cfg.setNumberFormat(FTLUtil.numberFormat);
		item.t = new Template(classDesc.getName() + ':' + type, new StringReader(item.sql), cfg);
		item.type = type;
		if (type != SQLItem.INSERT) item.pretty = true; // 900, 去掉where 1=1
		item.prepared = classDesc.isPrepare(); // 937_20170214
		// if (classDesc.isPrepare() && type != SQLItem.SELECT) item.prepared =
		// true;
		// else item.type = type; //
		if (type == SQLItem.SELECT)
		{
			if (classDesc.getSelectPreFn() != null
					&& classDesc.getSelectPreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getSelectPreFn(), false, 0, true);
			if (classDesc.getSelectPostFn() != null
					&& classDesc.getSelectPostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getSelectPostFn(), false, 0, true));
			}
		}
		else if (type == SQLItem.UPDATE)
		{
			if (classDesc.getUpdatePreFn() != null
					&& classDesc.getUpdatePreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getUpdatePreFn(), false, 0, true);
			if (classDesc.getUpdatePostFn() != null
					&& classDesc.getUpdatePostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getUpdatePostFn(), false, 0, true));
			}
		}
		else if (type == SQLItem.INSERT)
		{
			if (classDesc.getInsertPreFn() != null
					&& classDesc.getInsertPreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getInsertPreFn(), false, 0, true);
			if (classDesc.getInsertPostFn() != null
					&& classDesc.getInsertPostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getInsertPostFn(), false, 0, true));
			}
		}
		else if (type == SQLItem.DELETE)
		{
			if (classDesc.getDeletePreFn() != null
					&& classDesc.getDeletePreFn().trim().length() > 0)
				item.preScript = new Script(classDesc.getDeletePreFn(), false, 0, true);
			if (classDesc.getDeletePostFn() != null
					&& classDesc.getDeletePostFn().trim().length() > 0)
			{
				if (item.postScripts == null) item.postScripts = new ArrayList();
				item.postScripts.add(new Script(classDesc.getDeletePostFn(), false, 0, true));
			}
		}
		return item;
	}

	static Logger log = LoggerFactory.getLogger(POItemLoader.class);
	// public static final int OPERATOR_SELECT = 0;
	// public static final int OPERATOR_INSERT = 1;
	// public static final int OPERATOR_UPDATE = 2;
	// public static final int OPERATOR_DELETE = 3;
}
