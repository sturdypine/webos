package spc.webos.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.cglib.proxy.Enhancer;
import spc.webos.bean.BeanSelfAware;
import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.persistence.jdbc.SlaveJdbcTemplate;
import spc.webos.persistence.jdbc.XJdbcTemplate;
import spc.webos.persistence.jdbc.namedparam.MapSqlParameterSource;
import spc.webos.persistence.jdbc.rowmapper.AbstractRowMapper;
import spc.webos.persistence.jdbc.rowtype.RowList;
import spc.webos.persistence.jdbc.rowtype.RowMap;
import spc.webos.persistence.jdbc.rowtype.RowXMap;
import spc.webos.persistence.loader.POItemLoader;
import spc.webos.persistence.loader.SQLItemXmlLoader;
import spc.webos.persistence.matrix.IMatrix;
import spc.webos.persistence.matrix.ListMatrix;
import spc.webos.service.seq.SeqNo;
import spc.webos.util.FTLUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.util.UUID;

public class Persistence
		implements ApplicationContextAware, ResourceLoaderAware, BeanSelfAware, IPersistence
{
	public boolean contain(String sqlId)
	{
		return getSQLConfig(sqlId) != null;
	}

	public List<Integer> update(List list)
	{
		if (list == null || list.size() == 0) return null;
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++)
			result.add(new Integer(update(list.get(i))));
		return result;
	}

	public String insertSQL(Object po)
	{
		Map params = new HashMap();
		params.put(GEN_SQL, Boolean.TRUE);
		insert(po, params);
		return (String) params.get(LAST_SQL_KEY);
	}

	public String updateSQL(Object po)
	{
		return updateSQL(po, null, null, false, null);
	}

	public String updateSQL(Object po, String[] whereProps, String updateTail, boolean updateNULL,
			Map params)
	{
		if (params == null) params = new HashMap();
		params.put(GEN_SQL, Boolean.TRUE);
		update(po, whereProps, updateTail, updateNULL, params);
		return (String) params.get(LAST_SQL_KEY);
	}

	public int[] batchInsert(List po)
	{ // 批量执行batch SQL语句
		log.info("batchInsert:{}", po.size());
		String[] sql = new String[po.size()];
		for (int i = 0; i < po.size(); i++)
			sql[i] = insertSQL(po.get(i));
		return getJdbcTemplate().batchUpdate(sql);
	}

	public int[] batch(String... sql)
	{
		log.info("batch SQL:{}", sql == null ? 0 : sql.length);
		return getJdbcTemplate().batchUpdate(sql);
	}

	public int[] batch(String sql, Object[] po)
	{
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(
				getJdbcTemplate());
		AbstractSqlParameterSource[] batchArgs = new BeanPropertySqlParameterSource[po.length];
		for (int i = 0; i < po.length; i++)
			batchArgs[i] = new BeanPropertySqlParameterSource(po[i]);
		return namedParameterJdbcTemplate.batchUpdate(sql, batchArgs);
	}

	public List<Integer> insert(List list)
	{
		if (list == null || list.size() == 0) return null;
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++)
			result.add(new Integer(insert(list.get(i))));
		return result;
	}

	public List<Integer> delete(List list)
	{
		if (list == null || list.size() == 0) return null;
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++)
			result.add(new Integer(delete(list.get(i))));
		return result;
	}

	public List query(Map paramMap)
	{
		String sqlID = (String) paramMap.get(QUERY_SQL_ID_KEY);
		return (List) execute(sqlID, paramMap);
	}

	public XJdbcTemplate getJdbcTemplate()
	{
		String jt = IPersistence.CURRENT_JT.get();
		if (StringX.nullity(jt)) return defautlJdbcTemplate;
		return appCxt.getBean(jt, XJdbcTemplate.class);
	}

	/**
	 * 默认为主键作为Where条件
	 * 
	 * @param value
	 * @return
	 */
	public int update(Object obj)
	{
		return update(obj, (String[]) null, false);
	}

	/**
	 * 用指定的属性域作为Where条件
	 * 
	 * @param value
	 * @param whereProperties
	 * @param updateNULL
	 *            是否容许更新null
	 */
	public int update(Object obj, String[] whereProperties, boolean updateNULL)
	{
		Map paramMap = new HashMap();
		if (whereProperties != null && whereProperties.length > 0)
			paramMap.put(ASSIGNED_FIELDS_KEY, whereProperties);
		if (updateNULL) paramMap.put(UPDATE_NULL_KEY, Boolean.TRUE);
		int[] rows = (int[]) excute(obj, obj.getClass(), SQLItem.UPDATE, paramMap);
		return rows == null ? 0 : rows[0];
	}

	public int update(Object obj, String[] whereProperties, String updateTail, boolean updateNULL,
			Map paramMap)
	{
		if (paramMap == null) paramMap = new HashMap();
		paramMap.put(UPDATE_ATTACH_TAIL_KEY, updateTail);
		if (whereProperties != null && whereProperties.length > 0)
			paramMap.put(ASSIGNED_FIELDS_KEY, whereProperties);
		if (updateNULL) paramMap.put(UPDATE_NULL_KEY, Boolean.TRUE);
		int[] rows = (int[]) excute(obj, obj.getClass(), SQLItem.UPDATE, paramMap);
		return rows == null ? 0 : rows[0];
	}

	public <T> List<T> get(T obj)
	{
		return (List<T>) get(obj, (String[]) null, true, false, null);
	}

	public <T> List<T> get(T obj, boolean lazyLoading)
	{
		return (List<T>) get(obj, (String[]) null, lazyLoading, false, null);
	}

	public <T> List<T> get(T obj, String[] assignedProps)
	{
		return (List<T>) get(obj, assignedProps, true, false, null);
	}

	public <T> List<T> get(T obj, Map paramMap)
	{
		return (List<T>) get(obj, (String[]) null, true, false, paramMap);
	}

	public <T> List<T> get(T obj, String[] assignedProperties, boolean lazyLoading,
			boolean forUpdate, Map paramMap)
	{
		if (paramMap == null) paramMap = new HashMap();
		if (assignedProperties != null && assignedProperties.length > 0)
			paramMap.put(ASSIGNED_FIELDS_KEY, assignedProperties);
		List result = (List) excute(obj, obj.getClass(), SQLItem.SELECT, paramMap);
		if (result == null || result.size() == 0) return result;

		// modified by cjs 090712 取消了关联加载
		// 900_20160105 重新启用关联加载
		enhance(result, (BulkBeanItem) voMapping.get(obj.getClass()), paramMap, lazyLoading);
		/*
		 * Cache cache = cacheManager.getCache(value.getClass().getName()); if
		 * (assignedProperties == null && lazyLoading && cache != null) { //
		 * 只有完整的查询信息才缓存 for (int i = 0; i < result.size(); i++) { PO value =
		 * (PO) result.get(i); cache.put(new
		 * net.sf.ehcache.Element(value.getKey(), value)); } }
		 */
		return result;
	}

	public <T> T find(T obj)
	{
		return (T) find(obj, (String[]) null, true, false, null);
	}

	public <T> T find(T obj, boolean lazyLoading)
	{
		return (T) find(obj, (String[]) null, lazyLoading, false, null);
	}

	public <T> T find(T obj, Map paramMap)
	{
		return (T) find(obj, (String[]) null, true, false, paramMap);
	}

	public <T> T find(T obj, String[] assignedProps)
	{
		return (T) find(obj, assignedProps, true, false, null);
	}

	public <T> T find(T obj, String[] assignedProperties, boolean lazyLoading, boolean forUpdate,
			Map paramMap)
	{
		/*
		 * modified by cjs 090712 Serializable key = value.getKey(); if (key !=
		 * null && assignedProperties == null) { // 检查缓存 Cache cache =
		 * cacheManager.getCache(value.getClass().getName());
		 * net.sf.ehcache.Element ele = null; if (cache != null) ele =
		 * cache.get(key); if (ele != null) return (PO) ele.getObjectValue(); }
		 */
		List result = get(obj, assignedProperties, lazyLoading, forUpdate, paramMap);
		if (result == null || result.size() == 0) return null;
		return (T) result.get(0);
	}

	public int insert(Object obj)
	{
		return insert(obj, new HashMap());
	}

	public int insert(Object obj, Map paramMap)
	{
		// SnowflakeUUID uuid = (SnowflakeUUID)
		// this.uuidMap.get(value.getClass()); //
		// 只有增加的时候才采用uuid
		// if (uuid != null) paramMap.put(UUID_KEY, uuid);
		// else paramMap.put(UUID_KEY, defUUID); // 如果没有自定义配置, 则采用默认生产方式
		paramMap.put(UUID_KEY, UUID.getInstance()); // 900, 使用统一的uuid
		int[] rows = (int[]) excute(obj, obj.getClass(), SQLItem.INSERT, paramMap);
		return rows == null ? 0 : rows[0];
	}

	public int delete(Object obj, String[] whereProperties, Map paramMap)
	{
		return delete(obj, whereProperties, null, paramMap);
	}

	public int delete(Object obj)
	{
		int[] rows = (int[]) excute(obj, obj.getClass(), SQLItem.DELETE, new HashMap());
		return rows == null ? 0 : rows[0];
	}

	/**
	 * 通过SQlId, 和参数执行查询结果, 并返回
	 * 
	 * @param sqlID
	 * @param paramMap
	 * @return
	 */
	public Object execute(String sqlId, Map paramMap) throws DataAccessException
	{
		return single(sqlId, paramMap);
	}

	// public Object dquery(String sqlId, Map paramMap) throws
	// DataAccessException
	// {
	// return single(sqlId, paramMap);
	// }
	//
	// public Map dquery(String[] sqlIds, Map paramMap, Map resultMap)
	// {
	// return batch(sqlIds, paramMap, resultMap);
	// }

	public Map query(String[] sqlIds, Map paramMap, Map resultMap)
	{
		return batch(sqlIds, paramMap, resultMap);
	}

	/**
	 * 只执行查询
	 */
	public Object query(String sqlId, Map paramMap) throws DataAccessException
	{
		return single(sqlId, paramMap);
	}

	/**
	 * 适合于报表操作, 把一组SQL执行, 并把结果根据SQL Id放到model中去
	 * 
	 * @param sqlIDs
	 *            需要执行的一组SQL
	 * @param paramMap
	 * @param model
	 */
	public Map execute(List sqlIds, Map paramMap, Map resultMap)
	{
		for (int i = 0; i < sqlIds.size(); i++)
		{
			String sqlId = ((String) ((List) sqlIds).get(i)).replace('.', '_').toLowerCase();
			String id = sqlId;
			int index = id.indexOf('_');
			String key = id.substring(index + 1);
			Object result = execute(id, paramMap);
			if (result != null)
			{
				paramMap.put(key, result);
				if (resultMap != paramMap) resultMap.put(sqlId, result);
			}
		}
		return resultMap;
	}

	public Map execute(String[] sqlIds, Map paramMap, Map resultMap)
	{
		return batch(sqlIds, paramMap, resultMap);
	}

	protected Object single(String sqlId, Map paramMap)
	{
		sqlId = sqlId.replace('.', '_').toLowerCase();
		Object res = null;
		Object item = getSQLConfig(sqlId);
		if (item == null)
		{
			log.warn("cannot find sql id: " + sqlId);
			throw new AppException(AppRetCode.DB_UNDEFINED_SQLID, new Object[] { sqlId });
		}
		if (paramMap == null) paramMap = new HashMap();
		if (item instanceof SQLItem) res = excute(sqlId, (SQLItem) item, paramMap);
		else res = excute(sqlId, (ReportItem) item, paramMap);
		return res;
	}

	protected Map batch(String[] sqlIds, Map paramMap, Map resultMap)
	{
		for (int i = 0; i < sqlIds.length; i++)
		{
			String sqlId = sqlIds[i].replace('.', '_').toLowerCase();
			String id = sqlId;
			int index = id.indexOf('_');
			String key = id.substring(index + 1);
			Object result = execute(id, paramMap);
			if (result != null)
			{
				paramMap.put(key, result);
				if (resultMap != paramMap) resultMap.put(sqlId, result);
			}
		}
		return resultMap;
	}

	/**
	 * 验证vo
	 * 
	 * @param vo
	 * @return
	 */
	public <T> boolean validate(T po, int operator, Map paramMap)
	{
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(po);
		if (constraintViolations.size() == 0) return true;
		throw new ConstraintViolationException("PO(" + po.getClass().getSimpleName() + ") errors:",
				constraintViolations);
	}

	/**
	 * 把所有SQL配置文件配置的SQl放到内存Map中.
	 */
	@PostConstruct
	public void init() throws Exception
	{
		log.info("product:" + product + ", cfgLocation:" + cfgLocation + ", user.dir:"
				+ System.getProperty("user.dir"));
		// cacheManager = CacheManager.create(); // 建造一个缓存管理器
		/*
		 * InputStream is = configLocation.getInputStream(); SAXReader reader =
		 * new SAXReader(false); Document doc = reader.read(is); Element root =
		 * doc.getRootElement();
		 */
		Map sqlMap = new HashMap();
		if (!StringX.nullity(cfgLocation))
		{ // 多模块jar下，可能没有单独的统一配置目录
			String location = cfgLocation + "/sql/";
			log.info("Load sqlocation:" + location);
			// 获取普通SQL语句的配置
			SQLItemXmlLoader.readSqlDir(resourceLoader.getResource(location), sqlMap, product);
		}
		if (scanJar)
		{// 2015-11-16 用于加载多个jar:META-INF/webos/persistence/sql/*.xml
			PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();
			Resource[] reses = pmrpr.getResources(JAR_SQL_PATH);
			for (Resource res : reses)
			{
				log.info("loading sql:{}", res);
				SQLItemXmlLoader.readSqlFile(res.getFilename(), res.getInputStream(), sqlMap, true);
			}
		}
		log.info("SQL items({}):{}", sqlMap.size(), sqlMap.keySet());
		/*
		 * DBList sqls = root.elements("sqlmap"); for (int i = 0; i <
		 * sqls.size(); i++) { Element ele = (Element) sqls.get(i); String
		 * namespace = ele.attributeValue("id"); boolean debug = new
		 * Boolean(ele.attributeValue("debug", "false")) .booleanValue(); String
		 * location = ele.attributeValue("location"); if
		 * (logger.isInfoEnabled()) logger.info(location + ", debug = " +
		 * debug); if (productMode || !debug) // 生产模式 或者是 对此配置文件为非调试模式,
		 * 解析具体模板内容到sqlMap中 sqlMap.put(namespace,
		 * SQLItemXmlLoader.parseXmlFile(resourceLoader
		 * .getResource(location).getInputStream())); else // 调试模式只放定位文件,
		 * 动态解析所执行的SQL文件 sqlMap.put(namespace, location); }
		 */

		// 获取VO到物理表的映射关系
		// logger.info("mapping.size = " + (sqls == null ? 0 : sqls.size()));
		Map voSQLMap = new ConcurrentHashMap(); // 900 使用同步Map
		Map voMapping = new ConcurrentHashMap();
		if (!StringX.nullity(cfgLocation))
		{ // 多模块jar下，可能没有单独的统一配置目录
			String location = cfgLocation + "/mapping/";
			log.info("Load mappingLocation:{}", location);
			POItemLoader.readMappingDir(resourceLoader.getResource(location), voSQLMap, voMapping);
		}
		if (scanJar)
		{// 2015-11-16 加载jar：META-IN/...
			PathMatchingResourcePatternResolver pmrpr = new PathMatchingResourcePatternResolver();
			Resource[] reses = pmrpr.getResources(JAR_VO_PATH);
			for (Resource res : reses)
			{
				log.info("loading po:{}", res);
				POItemLoader.readClassXML(res.getInputStream(), voSQLMap, voMapping);
			}
		}
		log.info("PO items({}):{}, mapping:{}", voSQLMap.size(), voSQLMap.keySet(),
				voMapping.keySet());

		// asign value
		this.sqlMap = new ConcurrentHashMap(sqlMap);
		this.voSQLMap = voSQLMap;
		this.voMapping = voMapping;
	}

	public void addSqlMap(Map sqls)
	{
		if (sqls == null || sqls.isEmpty()) return;
		log.info("refresh SQL id:{}", sqls.keySet());
		sqls.forEach((k, v) -> { // 容许数据库的模块配置和文件系统配置相同，数据库id会覆盖文件系统
			if (!sqlMap.containsKey(k)) sqlMap.put(k, v);
			else((Map) sqlMap.get(k)).putAll((Map) v);
		});
		// sqlMap.putAll(sqls);
	}

	Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	boolean product = true;
	ApplicationContext appCxt;
	XJdbcTemplate defautlJdbcTemplate; // 默认数据源
	volatile Map sqlMap = new ConcurrentHashMap(); // 系统所有的SQL项的配置
	Map<Class<?>, SQLItem[]> voSQLMap; // 直接对象方式访问的SQL项的配置
	Map<Class<?>, BulkBeanItem> voMapping; // 每个vo属性中关联其他vo属性的配置信息
	protected SeqNo seqno; // 900_20160107, 使用外部接口来实现唯一顺序号
	String cfgLocation; // = "/WEB-INF/env/persistence";
	boolean scanJar = true; // 扫描jar META-INF/webos/persistence/..
	ResourceLoader resourceLoader; // 资源加载类
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public void setApplicationContext(ApplicationContext appCxt)
	{
		this.appCxt = appCxt;
	}

	public void setDefautlJdbcTemplate(XJdbcTemplate defautlJdbcTemplate)
	{
		this.defautlJdbcTemplate = defautlJdbcTemplate;
	}

	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	/**
	 * 采用cglib方法， 对每个关联属性进行增强， 采用动态延时加载， 在需要使用的时候调用
	 * 
	 * @param result
	 * @param bulkBeanItem
	 */
	void enhance(List result, BulkBeanItem bulkBeanItem, Map paramMap, boolean lazyLoading)
	{
		if (bulkBeanItem == null || result == null || result.size() == 0) return;
		BeanWrapperImpl wrapper = !lazyLoading ? new BeanWrapperImpl() : null;
		try
		{
			for (Object rowObj : result)
			{
				List properties = bulkBeanItem.getProperties();
				Object[] values = lazyLoading ? new Object[properties.size()] : null;
				if (!lazyLoading) wrapper.setWrappedInstance(rowObj);
				for (int i = 0; i < properties.size(); i++)
				{
					Object[] bulkBeans = (Object[]) properties.get(i);
					if (lazyLoading)
					{
						values[i] = Enhancer.create((Class) bulkBeans[2], new ResultLazyLoader(this,
								bulkBeans, rowObj, paramMap, lazyLoading));
					}
					else
					{
						wrapper.setPropertyValue((String) bulkBeans[4],
								new ResultLazyLoader(this, bulkBeans, rowObj, paramMap, lazyLoading)
										.loadObject());
					}
				}
				// 900_20160105 如果不是延时加载，则直接激活
				if (lazyLoading) bulkBeanItem.getVoProperties().setPropertyValues(rowObj, values);
			}
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	// public void invokeEnhance(Object po)
	// {
	// BulkBeanItem bbi = (BulkBeanItem) voMapping.get(po.getClass());
	// if (bbi == null) return;
	// String[] getters = bbi.voProperties.getGetters();
	// System.out.println(Arrays.toString(getters));
	// }

	/**
	 * 对一个对象执行select, delete, update, insert四种操作...
	 * 
	 * @param msg
	 * @param clazz
	 *            在含有superclass的继承模式下，需要指明obj具体的类对象。
	 * @param operator
	 * @param paramMap
	 * @return
	 */
	Object excute(Object vo, Class clazz, int operator, Map paramMap)
	{
		log.debug("vo: {}", vo);
		((IPersistence) self).validate(vo, operator, paramMap); // 验证当前vo
		SQLItem[] items = (SQLItem[]) voSQLMap.get(clazz);
		if (items == null)
		{ // 900 通过注解动态生成mapping关系
			POItemLoader.readPO(clazz, voSQLMap, voMapping);
			items = (SQLItem[]) voSQLMap.get(clazz);
		}
		SQLItem item = items[operator];
		String sqlId = clazz.toString().toLowerCase().replace(' ', '_').replace('.', '_') + '_'
				+ item.type;
		// 检查是否有权限执行此sql
		// ISessionUserInfo sui = (ISessionUserInfo) ISessionUserInfo.SUI.get();
		// if (sui != null && !sui.containSqlId(sqlId)) throw new
		// AppException(AppRetCode.DB_UNAUTH,
		// new Object[] { sqlId });

		paramMap.put(VO_KEY, vo);
		if (item.type == SQLItem.INSERT)
		{ // 做插入操作
			if (item.sequnce && seqno != null)
				paramMap.put(SEQ_KEY, seqno.nextId("class:" + clazz.getName()));
		}
		return excute(sqlId, item, paramMap);
	}

	/**
	 * 执行报表节点
	 * 
	 * @param sqlID
	 * @param item
	 * @param paramMap
	 * @return
	 */
	Object excute(String sqlID, ReportItem item, Map paramMap)
	{
		String lowerId = sqlID.toLowerCase();
		// 检查是否有权限执行此sql
		// ISessionUserInfo sui = (ISessionUserInfo) ISessionUserInfo.SUI.get();
		// if (sui != null && !sui.containSqlId(lowerId))
		// throw new AppException(AppRetCode.DB_UNAUTH, new Object[] { lowerId
		// });

		// 1. 获取所有的依赖的sql
		execute(item.dependence, paramMap, paramMap);
		for (int i = 0; i < item.dependence.length; i++)
		{
			String id = item.dependence[i];
			paramMap.put(MATRIX_PREFIX + id.replace('.', '_'),
					new ListMatrix((List) paramMap.get(id)));
		}

		// 2. 执行预处理
		if (item.preScripts != null)
		{
			for (int i = 0; i < item.preScripts.size(); i++)
			{
				Script s = (Script) item.preScripts.get(i);
				Object result = paramMap.get(s.target);
				excuteScript(sqlID, result, paramMap, null, item, s);
			}
		}

		// 3 执行main
		List mainResult = new ArrayList();
		IMatrix matrix = new ListMatrix(mainResult);
		paramMap.put(lowerId, mainResult);
		paramMap.put(sqlID, mainResult);
		paramMap.put(MATRIX_PREFIX + lowerId, matrix);
		paramMap.put(MATRIX_PREFIX + lowerId.substring(lowerId.indexOf('.') + 1), matrix);

		// 4 执行post
		if (item.postScripts != null)
		{
			for (int i = 0; i < item.postScripts.size(); i++)
			{
				Script s = (Script) item.postScripts.get(i);
				Object result = paramMap.get(s.target);
				// System.out.println(sqlID+s.target+result);
				excuteScript(sqlID, result, paramMap, null, item, s);
			}
		}
		// 7. 如果是查询结构需要执行行索引,则执行行索引
		if (item.rowIndex != null && mainResult != null)
			makeRowIndex(sqlID, item.rowIndex, (List) mainResult, paramMap);

		return mainResult;
	}

	/**
	 * 给定一个特定的SQLConfig, 执行并返回结果
	 * 
	 * @param sqlID
	 * @param conf
	 * @param paramMap
	 * @return
	 * @throws DataAccessException
	 */
	Object excute(String sqlID, SQLItem item, Map paramMap) throws DataAccessException
	{
		log.debug("sqlitem:{}", item);
		if (paramMap.containsKey(IPersistence.SELECT_ONLY) && !item.isSelect())
		{ // 如果当前环境设置了只能执行select 而当前语句不是select则报权限错误
			throw new AppException(AppRetCode.DB_UNAUTH, new Object[] { sqlID });
		}
		// 检查是否有权限执行此sql
		if (!((IPersistence) self).isAuth(sqlID, item, paramMap))
		{
			log.info("unauthorized sqlId:{}", sqlID);
			throw new AppException(AppRetCode.DB_UNAUTH, new Object[] { sqlID });
		}

		// 执行先决SQL
		if (item.dependence != null)
		{
			for (int i = 0; i < item.dependence.length; i++)
			{
				String sid = item.dependence[i].replace('.', '_').toLowerCase();
				if (!paramMap.containsKey(sid)) execute(sid, paramMap); // 如果先觉条件SQL已经存在
				else if (log.isInfoEnabled())
					log.info("dependence sqlid: " + sid + " has been existed...");
			}
		}
		Object result = null;
		// 获得配置的数据源
		XJdbcTemplate jt = getXJdbcTemplate(sqlID, item, paramMap);
		// 读写分离，读取从机数据
		if (jt instanceof SlaveJdbcTemplate) result = slave(sqlID, item, jt, paramMap);
		else result = master(sqlID, item, jt, paramMap);

		// 7. 如果是查询结构需要执行行索引,则执行行索引
		if (item.isSelect() && item.rowIndex != null && result != null)
			makeRowIndex(sqlID, item.rowIndex, (List) result, paramMap);
		return result;
	}

	protected Object master(String sqlID, SQLItem item, XJdbcTemplate jt, Map paramMap)
	{ // 读写主数据源
		// 1. 执行前置脚本函数
		String sql = processSQL(sqlID, item, item.t, jt, paramMap);
		paramMap.put(SQL_PREFIX + sqlID, sql);
		paramMap.put(LAST_SQL_KEY, sql);
		boolean genSQL = paramMap.containsKey(GEN_SQL);
		if (!genSQL && !item.isSelect()) log.info("SQL({},{}): {}", sqlID, jt.getDbName(), sql);
		if (sql.length() <= 6 || genSQL) return null;
		// 如果当前SQL生成完后是一个空SQL标识, 表示此SQL在此条件下不执行, 一般用于报表批量执行
		// modified by sturdypine. 如果SQL语句的长度小于等于6个字符, 则不执行
		// System.out.println("sql: " + sql);
		/*
		 * modified by cjs 090712 if (item.flushClazzArray != null) { // 清空相关缓存
		 * for (int i = 0; i < item.flushClazzArray.length; i++) { Cache cache =
		 * cacheManager.getCache(item.flushClazzArray[i] .getName()); if (cache
		 * != null) cache.removeAll(); } }
		 */
		Object result = null;
		SQLItem.setCurrentItem(item);
		try
		{
			result = item.isSelect() ? excSelect(sqlID, sql, jt, item, paramMap)
					: (item.prepared
							? (item.procedure ? excProcedure(sql, jt, paramMap)
									: excPrepared(sql, jt, paramMap))
							: excDelUpdIns(sql, jt, item, paramMap));
			paramMap.put(sqlID, result);
		}
		finally
		{
			SQLItem.setCurrentItem(null);
		}
		// 5. 执行后置脚本
		if (item.postScripts != null)
		{
			for (int i = 0; i < item.postScripts.size(); i++)
			{
				Script s = (Script) item.postScripts.get(i);
				excuteScript(sqlID, result, paramMap, jt, item, s);
			}
		}
		return result;
	}

	protected Object slave(String sqlID, SQLItem item, XJdbcTemplate jt, Map paramMap)
	{ // 只读取从数据源
		// 1. 执行前置脚本函数
		String sql = processSQL(sqlID, item, item.t, jt, paramMap);
		paramMap.put(SQL_PREFIX + sqlID, sql);
		paramMap.put(LAST_SQL_KEY, sql);
		// log.info("{}SSQL({},{}): {}", item.prepared ? "P" : "", sqlID,
		// jt.getDbName(), sql);
		SQLItem.setCurrentItem(item);
		Object result = null;
		try
		{
			result = excSelect(sqlID, sql, jt, item, paramMap);
			paramMap.put(sqlID, result);
		}
		finally
		{
			SQLItem.setCurrentItem(null);
		}
		return result;
	}

	protected XJdbcTemplate getXJdbcTemplate(String sqlID, SQLItem item, Map paramMap)
	{
		// 3. 939_20170302, 当前线程环境拦截设置jdbctemplate
		XJdbcTemplate jt = getJdbcTemplate();
		// 如果是多数据源查询则直接修改jt对象
		String jtName = (String) paramMap.get(JT_KEY + sqlID);
		// 1. 优先使用当前参数环境的JT
		if (StringX.nullity(jtName)) jtName = (String) paramMap.get(JT_KEY); // 特定sql的配置2010-09-26
		// 2. 再次使用sql固定配置的JT
		if (StringX.nullity(jtName))
		{
			if (item.isSelect() && !StringX.nullity(item.slave))
			{ // 如果是配置了从数据库且从数据库配置存在
				try
				{
					SlaveJdbcTemplate sjt = appCxt.getBean(item.slave, SlaveJdbcTemplate.class);
					if (sjt != null) return sjt;
				}
				catch (Exception e)
				{
				}
			}
			if (!StringX.nullity(item.jt)) jtName = item.jt;
		}
		if (!StringX.nullity(jtName)) jt = appCxt.getBean(jtName, XJdbcTemplate.class);

		return jt;
	}

	public boolean isAuth(String sqlId, SQLItem item, Map params)
	{
		((IPersistence) self).injection(sqlId, item, params);
		return true; // WebUtil.containSqlId(sqlId, params);
	}

	public boolean injection(String sqlId, SQLItem item, Map params)
	{
		if (item.injection == null) return false;
		for (String name : item.injection)
		{
			Object value = params.get(name);
			if (value == null) continue;
			if (value instanceof String || value instanceof StringBuffer
					|| value instanceof StringBuilder)
			{
				String str = value.toString().toLowerCase();
				if (SQL_INJECTION.matcher(str).find())
				{
					log.info("SQL injection:{}, param:{}, value:{}", sqlId, name, str);
					throw new AppException(AppRetCode.SQL_INJECTION, new Object[] { sqlId, name });
				}
			}
		}
		return false;
	}

	/**
	 * 根据配置的索引，建立行索引
	 * 
	 * @param sqlId
	 * @param rowIndexMF
	 * @param data
	 * @param params
	 */
	void makeRowIndex(String sqlId, String rowIndexMF, List data, Map params)
	{
		MessageFormat mf = null;
		Template t = null;
		Map rowIndex = new HashMap();
		Object[] row = null;
		for (int i = 0; i < data.size(); i++)
		{
			Object r = data.get(i);
			if (r instanceof List)
			{ // row is a list
				if (mf == null) mf = new MessageFormat(rowIndexMF);
				if (row == null) row = ((List) r).toArray();
				else((List) r).toArray(row);
				rowIndex.put(mf.format(row), new Integer(i));
			}
			else
			{ // row is a map
				try
				{
					if (t == null)
						t = new Template("RI", new StringReader(rowIndexMF), new Configuration());
					StringWriter out = new StringWriter();
					t.process((Map) r, out);
					rowIndex.put(out.toString(), new Integer(i));
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		params.put(sqlId + ROW_INDEX_POSTFIX, rowIndex);
	}

	// 执行 Prepared 类型 SQL
	Object excPrepared(String sql, XJdbcTemplate jt, Map params)
	{
		NamedParameterJdbcTemplate npJT = new NamedParameterJdbcTemplate(jt);
		Object vo = params.get(VO_KEY);
		AbstractSqlParameterSource source = (vo == null ? new MapSqlParameterSource(params)
				: new BeanPropertySqlParameterSource(vo));

		int rows = npJT.update(sql, source);
		log.debug("influenced rows: {}", rows);
		return new int[] { rows };
	}

	// 执行带返回参数的存储过程
	Object excProcedure(String sql, XJdbcTemplate jt, Map params)
	{
		CSCallback cscb = new CSCallback(sql, params);
		return jt.execute(cscb.prepare(), cscb);
	}

	// 执行 DELETE_UPDATE_INSERT_SQL 类型 SQL
	Object excDelUpdIns(String sql, XJdbcTemplate jt, SQLItem item, Map paramMap)
	{
		if (item.delim == null || item.delim.length() == 0 || sql.indexOf(item.delim) < 0)
		{
			int rows = jt.update(sql);
			if (log.isDebugEnabled()) log.debug("influenced rows: " + rows);
			return new int[] { rows }; // 返回影响的行数
		}
		// 批量Update, 805 StringUtils变为StringX, 可以切分多行sql
		int[] rows = jt.batchUpdate(StringX.split(sql, item.delim));
		if (log.isDebugEnabled())
			log.debug("influenced rows: " + StringX.join(rows, StringX.COMMA));
		return rows;
	}

	int[] excDelUpdIns(String[] sql, XJdbcTemplate jt)
	{ // 批量更新sql
		int[] rows = jt.batchUpdate(sql);
		if (log.isDebugEnabled())
			log.debug("influenced rows:{}", StringX.join(rows, StringX.COMMA));
		return rows;
	}

	// 执行 Select 类型 SQL
	Object excSelect(String sqlID, String sql, XJdbcTemplate jt, SQLItem item, Map params)
	{
		// 传递字段转换信息
		AbstractRowMapper.COLUMN_CONVERTER.set(item.columnConverters);
		AbstractRowMapper.COLUMN_CASE.set(item.column);

		// 是否配置缓存, 如果有缓存则先检查缓存
		// 用给定的行处理类, 查询结果RESULT_CLASS_PREFIX
		List result = null;
		try
		{
			String resultClass = (String) params.get(RESULT_CLASS_PREFIX + sqlID);
			if (resultClass == null) resultClass = item.resultClass;

			if (item.prepared)
			{
				Object vo = params.get(VO_KEY);
				final AbstractSqlParameterSource source = vo == null
						? new MapSqlParameterSource(params)
						: new BeanPropertySqlParameterSource(vo);
				final Class clazz = jt.getInnerClazz(resultClass);
				final String retClass = resultClass;
				if (jt instanceof SlaveJdbcTemplate)
				{ // 主从部署中的从设备上集群模式读取
					result = ((SlaveJdbcTemplate) jt).forEach((j) -> {
						log.info("PSQL({},{}): {}", sqlID, j.getDbName(), sql);
						if (clazz != null) return new NamedParameterJdbcTemplate(j)
								.queryForList(sql, source, clazz);
						return new NamedParameterJdbcTemplate(j).query(sql, source,
								j.getRowMapper(retClass));
					});
				}
				else
				{
					log.info("PSQL({},{}): {}", sqlID, jt.getDbName(), sql);
					if (clazz != null) result = new NamedParameterJdbcTemplate(jt).queryForList(sql,
							source, clazz);
					else result = new NamedParameterJdbcTemplate(jt).query(sql, source,
							jt.getRowMapper(resultClass));
				}
			}
			else result = jt.query(sqlID, sql, resultClass);

			// CLM_PREFIX, 如果有结果集，并且结果集是多列，则提取此结果集的clm信息
			if (result != null && result.size() > 0)
			{
				Object r = result.get(0);
				String[] clms = null;
				if (r instanceof RowList) clms = ((RowList) r).getColumnName();
				else if (r instanceof RowMap) clms = ((RowMap) r).getColumnName();
				else if (r instanceof RowXMap) clms = ((RowXMap) r).getColumnName();
				if (clms != null) params.put(CLM_PREFIX + sqlID, clms);
			}
		}
		finally
		{
			AbstractRowMapper.COLUMN_CONVERTER.set(null);
			AbstractRowMapper.COLUMN_CASE.set(null);
		}
		// List result = qr.result;
		// if (qr.rsmd != null) paramMap.put(sqlID + "_RSMD", qr.rsmd);
		if (result == null || result.size() == 0)
			return item.firstRowOnly ? null : new LinkedList();
		// 是否唯一行处理
		// boolean firstRowOnly = item.firstRowOnly;
		// Boolean _firstRowOnly = (Boolean) paramMap.get("_FIRST_ROW_ONEY_");
		// if (_firstRowOnly != null) firstRowOnly =
		// _firstRowOnly.booleanValue();
		return item.firstRowOnly ? ((List) result).get(0) : result;
	}

	/**
	 * 通过SQLID返回一个SQL配置项
	 * 
	 * @param sqlID
	 * @return
	 */
	public Object getSQLConfig(String sqlID)
	{
		sqlID = sqlID.toLowerCase(); // 大小写不敏感
		int indexOfDelim = sqlID.indexOf('.');
		if (indexOfDelim < 0) indexOfDelim = sqlID.indexOf('_');
		Map namespaceSQLMap = getSQLMap(sqlID.substring(0, indexOfDelim));
		Object item = namespaceSQLMap.get(sqlID.substring(indexOfDelim + 1));
		return item;
	}

	/**
	 * 通过namespace 找此模块的所有SQL配置文件
	 * 
	 * @param namespace
	 * @return
	 */
	Map getSQLMap(String namespace)
	{
		try
		{
			Object obj = sqlMap.get(namespace);
			if (obj instanceof Map) return (Map) obj;
			Map namespaceSQLMap = new HashMap();
			String sqlLocation = this.cfgLocation + "/sql/";
			InputStream is = resourceLoader.getResource(sqlLocation + (String) obj)
					.getInputStream();
			try
			{
				SQLItemXmlLoader.parseXmlFile(namespace, is, namespaceSQLMap, true);
			}
			finally
			{
				is.close();
			}
			return namespaceSQLMap;
		}
		catch (Exception e)
		{
			log.error("Can not find SQL File for namespace(" + namespace + ")", e);
			throw new RuntimeException(e);
		}
	}

	void excuteScript(String sqlID, Object result, Map paramMap, XJdbcTemplate jt, Object item,
			Script s)
	{
		if (s.type == Script.MATRIX_INNER_EXP && result instanceof List)
		{
			IMatrix matrix = new ListMatrix((List) result);
			paramMap.put(MATRIX_PREFIX + sqlID, matrix);
			paramMap.put(MATRIX_PREFIX + sqlID.substring(sqlID.indexOf('.') + 1), matrix);
			matrixExp(matrix, s, jt, paramMap);
		}
	}

	void matrixExp(IMatrix matrix, Script script, XJdbcTemplate jt, Map paramMap)
	{
		try
		{
			// IMatrix matrix = new ListMatrix(result);
			matrix.process(proccessScript(script, jt, paramMap), ";");
		}
		catch (Exception e)
		{
			log.error("matrixExp:" + script, e);
			throw new RuntimeException("matrixExp script=" + script.script);
		}
	}

	String proccessScript(Script script, XJdbcTemplate jt, Map params) throws IOException
	{
		if (!script.isTemplate) return script.script;
		// if (ctxParam != null) params.put(Common.MODEL_CXT_KEY, ctxParam);
		String id = "script";
		return processSQL(id, null,
				new Template(id, new StringReader(script.script), new Configuration()), jt, params);
	}

	/**
	 * 用Freemarker解析SQL模板语句
	 * 
	 * @param sqlID
	 * @param paramMap
	 * @param out
	 */
	String processSQL(String sqlID, SQLItem item, Template t, XJdbcTemplate jt, Map root)
	{
		root.put(DB_TYPE_KEY, jt.getDbType());
		long s = 0;
		if (log.isDebugEnabled()) s = System.currentTimeMillis();
		StringWriter sw = new StringWriter();
		try
		{
			sw.getBuffer().setLength(0);
			FTLUtil.freemarker(t, root, sw);
			String sql;
			if (item != null && item.pretty) // (sqlID.indexOf(':') > 0)
			{ // VO相关的SQL操作
				// if (sql.endsWith("where 1=1")) sql = sql.substring(0,
				// sql.length() - 9);
				// sql = sql.replaceAll("where 1=1 and", "where");
				sql = StringX.trim(pretty(sw.getBuffer()));
			}
			else sql = StringX.trim(sw.toString());

			if (log.isDebugEnabled())
			{
				long cost = System.currentTimeMillis() - s;
				log.debug("processSQL cost: " + cost + ", " + sqlID);
			}
			return sql;
		}
		catch (Throwable tt)
		{
			log.warn("sql freemarker", tt);
			throw new AppException(AppRetCode.DB_FREEMARKER, new Object[] { sqlID, tt.toString() },
					tt);
		}
	}

	// 美化sql， 去掉where 1=1
	String pretty(StringBuffer sql)
	{
		int idx = sql.indexOf("where 1=1");
		if (idx <= 0) return sql.toString();
		int andIdx = sql.indexOf("and", idx);
		if (andIdx < 0)
		{ // no and, but select_tail
			return sql.substring(0, idx) + sql.substring(idx + 9);
		}
		return sql.substring(0, idx + 5) + sql.substring(andIdx + 3);
	}

	/**
	 * 从配置中获取VO类的配置信息
	 * 
	 * @param clazz
	 */
	/*
	 * synchronized SQLItem[] loadSQLItem(Class clazz) { SQLItem[] items =
	 * (SQLItem[]) voSQLMap.get(clazz); if (items != null) return items; String
	 * clazzName = clazz.getName(); String packageName = clazzName.substring(0,
	 * clazzName.lastIndexOf('.')); String dir = (String)
	 * voSQLMap.get(packageName); try { // 对VO类的配置采用一旦读取就直接不能修改, 这点区别于普通SQL模式
	 * VOSQLItemLoader.readDir(resourceLoader, dir, voSQLMap, voMapping,
	 * voManualSequenceMap, cacheManager); voSQLMap.remove(packageName);
	 * loadSeqenceOfTable(false); } catch (Exception e) { throw new
	 * RuntimeException(e); }
	 * 
	 * return (SQLItem[]) voSQLMap.get(clazz); }
	 */

	// 获取某张表中手动自增记录的自增数据
	// public Long seqenceOfTable(Class clazz)
	// {
	// Sequence seq = (Sequence) voManualSequenceMap.get(clazz);
	// if (seq == null) return null;
	// // 900_20160107, 使用外部接口来实现顺序号
	// return seq.value = seqno.nextId("class:" + clazz.getName());
	// // synchronized (value)
	// // {
	// // value[3] = new Long(((Long) value[3]).longValue() + 1);
	// // return (Long) value[3];
	// // }
	// }

	public void setSeqno(SeqNo seqno)
	{
		this.seqno = seqno;
	}

	// public void setCtxParam(Map ctxParam)
	// {
	// this.ctxParam = ctxParam;
	// }

	public void setCfgLocation(String cfgLocation)
	{
		this.cfgLocation = cfgLocation;
	}

	public String getCfgLocation()
	{
		return cfgLocation;
	}

	public void setProduct(boolean product)
	{
		this.product = product;
	}

	public int update(Object obj, List whereProps, boolean updateNULL)
	{
		return update(obj, whereProps == null ? null
				: (String[]) whereProps.toArray(new String[whereProps.size()]), updateNULL);
	}

	public int update(Object obj, List whereProps, String updateTail, boolean updateNULL,
			Map params)
	{
		return update(obj,
				whereProps == null ? null
						: (String[]) whereProps.toArray(new String[whereProps.size()]),
				updateTail, updateNULL, params);
	}

	public int delete(Object obj, String[] whereProps, String deleteTail, Map params)
	{
		if (params == null) params = new HashMap();
		if (whereProps != null && whereProps.length > 0)
			params.put(ASSIGNED_FIELDS_KEY, whereProps);
		if (!StringX.nullity(deleteTail)) params.put(DELETE_ATTACH_TAIL_KEY, deleteTail);
		int[] rows = (int[]) excute(obj, obj.getClass(), SQLItem.DELETE, params);
		return rows[0];
	}

	public List get(Object obj, List assignedProps, boolean lazyLoading, boolean forUpdate,
			Map params)
	{
		return get(obj,
				assignedProps == null ? null
						: (String[]) assignedProps.toArray(new String[assignedProps.size()]),
				lazyLoading, forUpdate, params);
	}

	public Object find(Object obj, List assignedProps, boolean lazyLoading, boolean forUpdate,
			Map params)
	{
		return find(obj,
				assignedProps == null ? null
						: (String[]) assignedProps.toArray(new String[assignedProps.size()]),
				lazyLoading, forUpdate, params);
	}

	// public Map dquery(List sqlIds, Map params, Map resultMap)
	// {
	// return dquery((String[]) sqlIds.toArray(new String[sqlIds.size()]),
	// params, resultMap);
	// }

	public Map query(List sqlIds, Map params, Map results)
	{
		return query((String[]) sqlIds.toArray(new String[sqlIds.size()]), params, results);
	}

	public boolean isScanJar()
	{
		return scanJar;
	}

	public void setScanJar(boolean scanJar)
	{
		this.scanJar = scanJar;
	}

	@Override
	public void self(Object proxyBean)
	{
		self = proxyBean;
		if (self == null) self = this;
	}

	@Override
	public Object self()
	{
		return self;
	}

	protected Object self = this;
}

/**
 * 支持存储过程多参数返回情况的调用
 * 
 * @author spc
 * 
 */
class CSCallback implements CallableStatementCallback
{
	protected String sql;
	protected Map params;
	protected List inOutParams = new ArrayList();
	public final static String PREFIX = ":{";
	public final static String POSTFIX = "}";
	public final static String PARAM_NAME = "name";
	public final static String PARAM_TYPE = "type";
	public final static String PARAM_OUT = "out";
	public final static String PARAM_CLASS = "class";
	public final static String TYPE_RS = "RS";
	public final static String TYPE_I = "I";
	public final static String TYPE_L = "L";
	public final static String TYPE_D = "D";
	public final static String TYPE_BD = "BD";
	public final static String TYPE_S = "S";

	public CSCallback(String sql, Map params)
	{
		this.sql = sql;
		this.params = params;
	}

	public String prepare()
	{
		int start = sql.indexOf(PREFIX);
		while (start >= 0)
		{
			int end = sql.indexOf(POSTFIX, start);
			inOutParams.add(JsonUtil.json2obj(sql.substring(start + 1, end + 1)));
			sql = sql.substring(0, start) + "?" + sql.substring(end + 1);
			start = sql.indexOf(PREFIX);
		}
		return sql;
	}

	public Object doInCallableStatement(CallableStatement cs) throws SQLException
	{
		for (int i = 0; i < inOutParams.size(); i++)
		{
			Map p = (Map) inOutParams.get(i);
			String name = (String) p.get(PARAM_NAME);
			String type = StringX.null2emptystr((String) p.get(PARAM_TYPE));
			Boolean out = (Boolean) p.get(PARAM_OUT);
			if (out == null || !out.booleanValue())
			{ // 输入参数
				if (type.equalsIgnoreCase(TYPE_I))
					cs.setInt(i + 1, Integer.parseInt((String) params.get(name)));
				else if (type.equalsIgnoreCase(TYPE_L))
					cs.setLong(i + 1, Long.parseLong((String) params.get(name)));
				else if (type.equalsIgnoreCase(TYPE_D))
					cs.setDouble(i + 1, Double.parseDouble((String) params.get(name)));
				else if (type.equalsIgnoreCase(TYPE_BD))
					cs.setBigDecimal(i + 1, new BigDecimal((String) params.get(name)));
				else cs.setString(i + 1, (String) params.get(name));
			}
			else
			{
				if (type.equalsIgnoreCase(TYPE_I)) cs.registerOutParameter(i + 1, Types.INTEGER);
				else if (type.equalsIgnoreCase(TYPE_L))
					cs.registerOutParameter(i + 1, Types.BIGINT);
				else if (type.equalsIgnoreCase(TYPE_D))
					cs.registerOutParameter(i + 1, Types.DOUBLE);
				else if (type.equalsIgnoreCase(TYPE_BD))
					cs.registerOutParameter(i + 1, Types.DECIMAL);
				else if (type.equalsIgnoreCase(TYPE_RS))
					cs.registerOutParameter(i + 1, Types.JAVA_OBJECT);
				else cs.registerOutParameter(i + 1, Types.VARCHAR);
			}
		}
		cs.execute();
		Map result = new HashMap();
		for (int i = 0; i < inOutParams.size(); i++)
		{
			Map p = (Map) inOutParams.get(i);
			String name = (String) p.get(PARAM_NAME);
			String type = StringX.null2emptystr((String) p.get(PARAM_TYPE));
			Boolean out = (Boolean) p.get(PARAM_OUT);
			if (out != null && out.booleanValue())
			{
				String value = null;
				if (type.equalsIgnoreCase(TYPE_I)) value = String.valueOf(cs.getInt(i + 1));
				else if (type.equalsIgnoreCase(TYPE_L)) value = String.valueOf(cs.getLong(i + 1));
				else if (type.equalsIgnoreCase(TYPE_D)) value = String.valueOf(cs.getDouble(i + 1));
				else if (type.equalsIgnoreCase(TYPE_BD))
					value = cs.getBigDecimal(i + 1).toPlainString();
				else if (type.equalsIgnoreCase(TYPE_RS))
				{
					// String clazz = StringX.null2emptystr((String)
					// p.get(PARAM_CLASS), "list");
					// ResultSet rs = (ResultSet) cs.getObject(i + 1);
					// // while(rs.isLast()) rs.n
					// rs.close();
				}
				else value = cs.getString(i + 1);
				result.put(name, value);
			}
		}
		return result;
	}
}
