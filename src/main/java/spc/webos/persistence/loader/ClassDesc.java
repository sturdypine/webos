package spc.webos.persistence.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * VO类描述信息
 * 
 * @author Hate
 * 
 */
public class ClassDesc
{
	public String name; // 类名: 包含package名
	public String table; // 对应的数据库物理表名
	public String parent; // 父类
	boolean sequence = false; // 900, 此表是否需要有手工唯一键值
	public String ds; // 对应的数据源，在spring context中就是一个jdbcTemplate的名字
	public String slave; //
//	public boolean cache; // 是否缓存此表记录
	public boolean prepare; // 是否需要Prepare方式执行
	public String remark; // 中文描述信息
	public List properties; // 直接物理表中属性列表
	public List voProperties; // 关联的VO对象属性
	public String declare; // 预定义属性
	public int insertMode; // 0 自然模式, 1 用select模式
	public String deletePreFn;
	public String deletePostFn;
	public String insertPreFn;
	public String insertPostFn;
	public String selectPostFn;
	public String selectPreFn;
	public String updatePreFn;
	public String updatePostFn;
	public List staticFields = new ArrayList();
	public Map columnConverter; // 字段转换

	public List getStaticFields()
	{
		return staticFields;
	}

	public void setStaticFields(List staticFields)
	{
		this.staticFields = staticFields;
	}

	public String getInsertPreFn()
	{
		return insertPreFn;
	}

	public void setInsertPreFn(String insertPreFn)
	{
		this.insertPreFn = insertPreFn;
	}

	public String getInsertPostFn()
	{
		return insertPostFn;
	}

	public void setInsertPostFn(String insertPostFn)
	{
		this.insertPostFn = insertPostFn;
	}

	public String getSelectPostFn()
	{
		return selectPostFn;
	}

	public void setSelectPostFn(String selectPostFn)
	{
		this.selectPostFn = selectPostFn;
	}

	public String getSelectPreFn()
	{
		return selectPreFn;
	}

	public void setSelectPreFn(String selectPreFn)
	{
		this.selectPreFn = selectPreFn;
	}

	public String getUpdatePreFn()
	{
		return updatePreFn;
	}

	public void setUpdatePreFn(String updatePreFn)
	{
		this.updatePreFn = updatePreFn;
	}

	public String getUpdatePostFn()
	{
		return updatePostFn;
	}

	public void setUpdatePostFn(String updatePostFn)
	{
		this.updatePostFn = updatePostFn;
	}

	public int getInsertMode()
	{
		return insertMode;
	}

	public void setInsertMode(int insertMode)
	{
		this.insertMode = insertMode;
	}

	public String getDeclare()
	{
		return declare;
	}

	public void setDeclare(String declare)
	{
		this.declare = declare;
	}

	public List getVoProperties()
	{
		return voProperties;
	}

	public void setVoProperties(List voProperties)
	{
		this.voProperties = voProperties;
	}

	public List getProperties()
	{
		return properties;
	}

	public void setProperties(List properties)
	{
		this.properties = properties;
	}

	public String getDataSource()
	{
		return ds;
	}

	public void setDataSource(String dataSource)
	{
		this.ds = dataSource;
	}

	public String getRemark()
	{
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent(String parent)
	{
		this.parent = parent;
	}

//	public boolean isCache()
//	{
//		return cache;
//	}
//
//	public void setCache(boolean cache)
//	{
//		this.cache = cache;
//	}

	public boolean isPrepare()
	{
		return prepare;
	}

	public void setPrepare(boolean prepare)
	{
		this.prepare = prepare;
	}

	public String getDeletePreFn()
	{
		return deletePreFn;
	}

	public void setDeletePreFn(String deletePreFn)
	{
		this.deletePreFn = deletePreFn;
	}

	public String getDeletePostFn()
	{
		return deletePostFn;
	}

	public void setDeletePostFn(String deletePostFn)
	{
		this.deletePostFn = deletePostFn;
	}

	public Map getColumnConverter()
	{
		return columnConverter;
	}

	public void setColumnConverter(Map columnConverter)
	{
		this.columnConverter = columnConverter;
	}
}
