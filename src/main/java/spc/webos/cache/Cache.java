package spc.webos.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import spc.webos.service.Status;

/**
 * @author spc
 */
public interface Cache<K, V> extends Status
{
	public final static Map<String, Cache<?, ?>> CACHES = new HashMap<>();

	void setName(String name);

	Collection<K> getKeys();

	int size();

	// V getMessage(String key) throws Exception; // for 国际化

	// 从cache中获取并删除
	V poll(K key) throws Exception;

	V poll(K key, WaitWithTime wwt) throws Exception;

	V poll(K key, long timeout) throws Exception;

	V get(K key) throws Exception;

	/**
	 * @param key
	 * @param o
	 * @return true means sucess.. false means failure..
	 */
	V put(K key, V o);

	/**
	 * 清空缓存所有信息
	 */
	void removeAll();

	/**
	 * 清除缓存中某一关键字的信息
	 * 
	 * @param key
	 */
	V remove(K o);

	/**
	 * 获得缓存的名字
	 * 
	 * @return
	 */
	String getName();

	V put(K key, V obj, int expireSeconds);

	void evictExpiredElements(); // 700 2012-05-25
}
