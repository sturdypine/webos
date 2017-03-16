package spc.webos.persistence;

import java.util.List;
import net.sf.cglib.beans.BulkBean;

/**
 * 用于VO属性解析关联VO的配置关系
 * 
 * @author Hate
 * 
 */
public class BulkBeanItem
{
	BulkBean voProperties; // 用于设置一个VO类里面的所有关联属性
	List properties; // 每一个关联的属性的配置信息。 其中每一个对象为一个BulkBean[]

	public List getProperties()
	{
		return properties;
	}

	public void setProperties(List properties)
	{
		this.properties = properties;
	}

	public BulkBean getVoProperties()
	{
		return voProperties;
	}

	public void setVoProperties(BulkBean voProperties)
	{
		this.voProperties = voProperties;
	}
}
