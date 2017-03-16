package spc.webos.persistence;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import freemarker.template.Template;

/**
 * SQL配置项
 * 
 * @author spc
 * 
 */
public class SQLItem implements Serializable
{
	public String sql; // SQL语句的freemarker语法模板
	public Template t; // SQL模板
	// Web环境访问安全控制:0:需要具体授权private, 1:登录授权login，9：公共访问public
	public int auth;
	public boolean sequnce = false; // 900, 是否手工在增加记录时由持久层添加唯一记录
	public boolean pretty; // 900, 是否美化sql 去掉where 1=1
//	public String cache; // application中定义的缓存类名(BeanName), 用于缓存数据, 一般不建议用缓存,
	// 因为没解决在事务情况下回滚导致的缓存失败问题...
	public short type; // Sql 语句的类型。select, delete, update, insert...
	public String resultClass; // 结果行类
	public String rowIndex; // 行索引messageformat, 为查出来的每行数据进行行索引,方便固定报表
	public String[] dependence; // 执行当前SQL前的先决SQL
	public String[] injection; // 938_20170226 防止SQL注入攻击
	public String jt; // 相当于制定数据源BeanName
	public String slave; // 从数据库集群名
	public boolean firstRowOnly; // 是否只提取结果集合的第一行
	public String delim; // 在delete,update情况下可以执行多条sql语句的 分隔符号. 默认;
//	public Class[] flushClazzArray; // 此SQL执行后所影响的表（类）的缓存信息。需要清空这些缓存，flushCaches
//	public String[] preFnodes; // 前置执行的函数数组, flow nodes名
//	public String[] postFnodes; // 后置执行的函数数组, flow nodes名
	public Script preScript; // 前置执行的脚本
	public List postScripts; // 后置执行的脚本
	public Map dict; // 针对数据库中查询的字段用spring ApplicationCxt来转换
	// public Script postFn;
	// public Script postExp; // 内部表达式,r0 = r1+r2;
	public boolean prepared; // 是否需要prepared处理
	public boolean procedure; // 是否带返回的存储过程

	public Map columnConverters; // 配置的列转换
	public Map column; // 由于现在Map行容器模式下对字段名的大小写不敏感，但由于ESB的message报文

	private static ThreadLocal CURRENT_ITEM = new ThreadLocal(); // 当前正在执行的sqlitem
	public final static int AUTH_private = 0;
	public final static int AUTH_login = 1;
	public final static int AUTH_public = 9;

	public static void setCurrentItem(SQLItem item)
	{
		CURRENT_ITEM.set(item);
	}

	public static SQLItem getCurrentItem()
	{
		return (SQLItem) CURRENT_ITEM.get();
	}

	public boolean isSelect()
	{
		return type == SELECT;
	}

	public static final short SELECT = 0;
	public static final short DELETE = 1;
	public static final short UPDATE = 2;
	public static final short INSERT = 3;
	// public static final short PREPARED = 4;
	public static final short CALL = 4;
	// public static final String DEFAULT_DELIM = ";";
	// public static final String IS_EMPTY_SQL = "_IS_EMPTY_SQL_"; // 空sql语句,
	// 此时不执行SQL
	public static final String RESULT_CLASS_LIST = "list";
	public static final String RESULT_CLASS_XMAP = "xmap";
	public static final String RESULT_CLASS_MAP = "map";
	public static final String RESULT_CLASS_JSON = "json";
	public static final String DEFAULT_RESULT_CLASS = RESULT_CLASS_LIST; // 默认的行数据类型
	private static final long serialVersionUID = 1L;

	public String toString()
	{
		return "class=" + resultClass + ", jt=" + jt + ", type=" + type;
	}
}
