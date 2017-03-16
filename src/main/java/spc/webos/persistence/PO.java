package spc.webos.persistence;

import java.io.Serializable;

/**
 * @author spc
 */
public interface PO extends Serializable, Cloneable, Comparable
{
	/**
	 * 每个VO构造成一个对应数据都是NULL的数据状态, 如果需要默认值, 则需要调用init方法
	 * 
	 */
	// void init();
	//
	// void close();

//	void destory();

	/**
	 * 清除VO中的属性为Null(对应数据库的NULL)
	 * 
	 */
//	void setNULL();

	// 持久层加载前调用
	void beforeLoad();

	// 持久层加载后调用
	void afterLoad();

	// 如果有手工seq，则增加调用接口
//	void setManualSeq(Long seq);

	/**
	 * 对于联合主键，获取主键字符串
	 * 
	 * @param delim
	 * @return
	 */
//	String primary(); // 900_20160115, JdbcUtil

//	Map getPrimary();

//	String table(); // 900_20160115

//	String[] blobFields();  // 900_20160115, AttachmentCtrller

	/**
	 * 设置外部关联属性为NULL， 一般用在序列化网络传输 设置transient属性后，将不会被序列化
	 */
	// void setOuterFieldNULL();
	/**
	 * VO的Key,
	 * 用于HashMap，缓存等情况，如果表只有一个主键，那么getKey就返回此主键，如果表有多个主键，建议增加一个全表sequence字段
	 * ，那么返回此sequence
	 * 
	 * @return
	 */
//	Serializable getKey();

//	String getKeyName();
}
