package spc.webos.service.timeout;

import java.util.List;

/**
 * 超时信息发现类，具体实现可能是MQ，DB，内存等模式
 * 
 * @author spc
 * 
 */
public interface TimeoutService
{
	// 找到超时数据
	List find() throws Exception;

	// 移除超时信息，返回为true表示移除成功，可做超时处理，false表示移除失败，可能是已经被移除
	boolean remove(Timeout timeout) throws Exception;

	// 增加超时信息
	boolean add(Timeout timeout) throws Exception;
}
