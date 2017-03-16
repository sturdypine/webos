package spc.webos.persistence.loader;

/**
 * VO中关联到其他VO的属性的配置信息
 * 
 * @author Hate
 * 
 */
public class ResultPropertyDesc
{
	public String name; // 属性名
	public String javaType; // VO类名称
	public String[] getter; // getter属性名称
	public String[] setter; // setter属性名称
	public Class[] clazzArray; // 关联属性的数据类型
	public boolean manyToOne; // true表示manyToOne. false表示oneToMany
	public String select; // 如果此属性不是和某vo关联， 而是一个查询语句的结果，select则表示此查询语句的Id
	public String remark; // 注释

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String[] getGetter()
	{
		return getter;
	}

	public void setGetter(String[] getter)
	{
		this.getter = getter;
	}

	public String getJavaType()
	{
		return javaType;
	}

	public void setJavaType(String javaType)
	{
		this.javaType = javaType;
	}

	public boolean isManyToOne()
	{
		return manyToOne;
	}

	public void setManyToOne(boolean manyToOne)
	{
		this.manyToOne = manyToOne;
	}

	public String[] getSetter()
	{
		return setter;
	}

	public void setSetter(String[] setter)
	{
		this.setter = setter;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Class[] getClazzArray()
	{
		return clazzArray;
	}

	public void setClazzArray(Class[] clazzArray)
	{
		this.clazzArray = clazzArray;
	}

	public String getSelect()
	{
		return select;
	}

	public void setSelect(String select)
	{
		this.select = select;
	}
}
