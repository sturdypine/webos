package spc.webos.service.resallocate;

import java.util.List;
import java.util.Map;

import spc.webos.exception.AppException;

public interface ResourcePoolService
{
	// 获得资源池的所有资源信息
	List getResources(Map param);

	// 获取指定资源
	List apply(String sn, String key, int matchType, int holdTime, int timeout) throws AppException;

	// 资源池中是否具有某种指定的资源
	boolean contain(String key);

	// 释放当前流水号占用的资源
	boolean release(String sn);

	// 定时为每个资源池回收超时资源
	List recycle();

	// 检查所有资源池的状态
	Map checkStatus(Map param);

	void refresh() throws Exception;
}
