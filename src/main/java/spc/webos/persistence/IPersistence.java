
package spc.webos.persistence;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;

public interface IPersistence
{
	void addSqlMap(Map sqls);

	boolean contain(String sqlId);

	int update(Object po);

	/**
	 * 用指定的属性域作为Where条件
	 * 
	 * @param value
	 * @param whereProperties
	 *            用对象中指定的属性作为update语句的where条件，如果为NULL默认表示用主键为where条件
	 * @param updateNULL
	 *            true表示容许把对象中的NULL属性更新到数据库中的对应的字段
	 * 
	 */
	int update(Object po, String[] whereProps, boolean updateNULL);

	int update(Object po, List whereProps, boolean updateNULL);

	int update(Object po, String[] whereProps, String updateTail, boolean updateNULL, Map params);

	int update(Object po, List whereProps, String updateTail, boolean updateNULL, Map params);

	int insert(Object po);

	int insert(Object po, Map params);

	int delete(Object po, String[] whereProps, Map params);

	int delete(Object po, String[] whereProps, String deleteTail, Map params);

	int delete(Object po);

	// <T> List<T> getInSlave(T po);

	<T> List<T> get(T po);

	<T> List<T> get(T po, boolean lazyLoading);

	<T> List<T> get(T po, String[] assignedProps);

	<T> List<T> get(T po, Map params);

	/**
	 * 
	 * @param value
	 *            需要从数据库加载的对象类型，以及对象数据
	 * @param assignedProperties
	 *            指定提取的字段（属性）名，用于提高效率
	 * @param lazyLoading
	 *            如果为false则表示不加载与此对象相关的表关联属性，不提取则效率高
	 * @param paramMap
	 *            带参数查询，可以为NULL
	 * @return
	 */
	<T> List<T> get(T po, String[] assignedProps, boolean lazyLoading, boolean forUpdate,
			Map params);

	<T> List<T> get(T po, List assignedProps, boolean lazyLoading, boolean forUpdate, Map params);

	<T> T find(T po);

	<T> T find(T po, boolean lazyLoading);

	<T> T find(T po, Map params);

	<T> T find(T po, String[] assignedProps);

	<T> T find(T po, String[] assignedProps, boolean lazyLoading, boolean forUpdate, Map params);

	<T> T find(T po, List assignedProps, boolean lazyLoading, boolean forUpdate, Map params);

	Object query(String sqlID, Map params) throws DataAccessException;

	Map query(String[] sqlIds, Map params, Map results);

	Map query(List sqlIds, Map params, Map results);

	/**
	 * 通过SQlId, 和参数执行查询结果, 并返回
	 * 
	 * @param sqlID
	 * @param paramMap
	 * @return
	 */
	Object execute(String sqlId, Map params) throws DataAccessException;

	/**
	 * 适合于报表操作, 把一组SQL执行, 并把结果根据SQL Id放到model中去
	 * 
	 * @param sqlIDs
	 *            需要执行的一组SQL
	 * @param paramMap
	 */
	Map execute(List sqlIds, Map params, Map results);

	Map execute(String[] sqlIds, Map params, Map results);

	/**
	 * 获得当前持久层配置的sql节点配置信息
	 * 
	 * @param sqlId
	 * @return
	 */
	Object getSQLConfig(String sqlId);

	/**
	 * 查询用，用于前台表格直接处理
	 */
	List query(Map params);

	List<Integer> update(List po);

	List<Integer> insert(List po);

	// batch operation
	int[] batchInsert(List po);

	int[] batch(String... sql);

	int[] batch(String sql, Object[] po);

	String insertSQL(Object po);

	String updateSQL(Object po);

	String updateSQL(Object po, String[] whereProps, String updateTail, boolean updateNULL,
			Map params);
	// batch operation end

	List<Integer> delete(List po);

	/**
	 * 验证po
	 * 
	 * @param po
	 * @return
	 */
	<T> boolean validate(T po, int operator, Map paramMap);

	// 是否授权sql
	boolean isAuth(String sqlId, SQLItem item, Map params);

	// 是否sql注入风险
	boolean injection(String sqlId, SQLItem item, Map params);

	// 939_20170302 当前使用的jdbctemplate
	public static ThreadLocal<String> CURRENT_JT = new ThreadLocal<>();

	public static Pattern SQL_INJECTION = Pattern.compile(
			"(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
					+ "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)",
			Pattern.CASE_INSENSITIVE);

	static String JAR_SQL_PATH = "classpath*:META-INF/persistence/sql/**/*.xml";
	static String JAR_VO_PATH = "classpath*:META-INF/persistence/mapping/**/*.xml";

	public static final String SELECT_ONLY = "SEL_ONLY"; // 只能执行select语句
	public static final String GEN_SQL = "_GEN_SQL_"; // 只生成sql,不执行
	public static final String QUERY_SQL_ID_KEY = "SQL_ID";
	public static final String PARAMS_KEY = "params";
	public static final String MATRIX_PREFIX = "M_";
	public static final String RESULT_CLASS_PREFIX = "_RC_";
	public static final String CLM_PREFIX = "_CLM_";
	public static final String SQL_PREFIX = "SQL_";
	public static final String ROW_INDEX_POSTFIX = "_RI";
	public static final String LAST_SQL_KEY = "_SQL_";
	public static final String UUID_KEY = "_UUID_";
	public static final String EX_KEY = "_EX_";
	public static final String VO_KEY = "_VO_";
	public static final String JT_KEY = "_JT_";
	public static final String DS_KEY = "_DS_";
	public static final String SEQ_KEY = "_SEQUENCE_";
	public static final String FILE_ALL_KEY = "_FILE_ALL_";
	public static final String FILE_FIELDS_KEY = "_FILE_FIELDS_";
	public static final String ASSIGNED_FIELDS_KEY = "_ASSIGNED_FIELDS_";
	public static final String DB_TYPE_KEY = "_DB_TYPE_";
	public static final String UPDATE_NULL_KEY = "_UPDATE_NULL_";
	public static final String SELECT_ATTACH_TAIL_KEY = "_SELECT_TAIL_"; // select语句后面的附加语句
	public static final String UPDATE_ATTACH_TAIL_KEY = "_UPDATE_TAIL_"; // update语句后面的附加语句
	public static final String DELETE_ATTACH_TAIL_KEY = "_DELETE_TAIL_"; // delete语句后面的附加语句
	public static final String TABLE_SEQUENCE_SQL_ID = "common.talbeSequence";
}
