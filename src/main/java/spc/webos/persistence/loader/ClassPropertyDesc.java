package spc.webos.persistence.loader;

/**
 * VO类属性的描述信息
 * 
 * @author Hate
 * 
 */
public class ClassPropertyDesc
{
	// String table; // 数据库物理表名
	String column; // 数据库表字段名
	String jdbcType; // 数据库字段类型CHAR, NUMERIC
	String javaType; // Java类属性类型
	String name; // Java类属性名
	boolean primary; // 是否主键标志
	boolean uuid; // 是否采用uuid方式生成此字段
	public boolean version; // 是否使用此字段作为update乐观锁，810版本以后提供
	public boolean updatable = true; // 900, 是否支持可修改
	String defaultValue; // 字段默认值
	String nullValue; // 针对数字类型的数据, 当等于特定数时认为是空(NULL)
	String insert; // insert 和 update 输入数据库操作的字段转意
	String update;
	String select; // select输出操作的字段转意
	String remark; // 字典中文解释
	String sequence; // 是否自增并且数据库字段没设置自增标识, 需要手工, auto表示采用数据库自动, manual表示查询手工
	boolean prepare; // 是否需要prepare模式
	public static String SEQUENCE_AUTO = "AUTO"; // 数据库物理表自动增加机制
	public static String SEQUENCE_MANUAL = "MANUAL"; // 手动维持增加机制
	// public static String JDBC_TYPE_NUMERIC = "NUMERIC";
	// public static String JDBC_TYPE_CHAR = "CHAR";
	// public static String JDBC_TYPE_DATE = "DATE";
	// public static String JDBC_TYPE_TIME = "TIME";
	// public static String JDBC_TYPE_TIMESTAMP = "TIMESTAMP";

	public String getSequence()
	{
		return sequence;
	}

	public void setSequence(String sequence)
	{
		this.sequence = sequence;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public String getInsert()
	{
		return insert;
	}

	public void setInsert(String insert)
	{
		this.insert = insert;
	}

	public String getJavaType()
	{
		return javaType;
	}

	public void setJavaType(String javaType)
	{
		this.javaType = javaType;
	}

	public String getJdbcType()
	{
		return jdbcType;
	}

	public void setJdbcType(String jdbcType)
	{
		this.jdbcType = jdbcType;
	}

	public String getNullValue()
	{
		return nullValue;
	}

	public void setNullValue(String nullValue)
	{
		this.nullValue = nullValue;
	}

	public boolean isPrimary()
	{
		return primary;
	}

	public void setPrimary(boolean primary)
	{
		this.primary = primary;
	}

	public boolean getUpdatable()
	{
		return updatable;
	}

	public void setUpdatable(boolean updatable)
	{
		this.updatable = updatable;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getSelect()
	{
		return select;
	}

	public void setSelect(String select)
	{
		this.select = select;
	}

	// public String getTable()
	// {
	// return table;
	// }
	//
	// public void setTable(String table)
	// {
	// this.table = table;
	// }

	public String getUpdate()
	{
		return update;
	}

	public void setUpdate(String update)
	{
		this.update = update;
	}

	public boolean isPrepare()
	{
		return prepare;
	}

	public void setPrepare(boolean prepare)
	{
		this.prepare = prepare;
	}

	public boolean isUuid()
	{
		return uuid;
	}

	public void setUuid(boolean uuid)
	{
		this.uuid = uuid;
	}

	public boolean isVersion()
	{
		return version;
	}

	public void setVersion(boolean version)
	{
		this.version = version;
	}
}
