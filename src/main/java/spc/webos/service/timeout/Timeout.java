package spc.webos.service.timeout;

/**
 * 超时信息接口
 * 
 * @author spc
 * 
 */
public interface Timeout
{
	String getInTime(); // 进入超时观察的时间: "20110909010101001"

	Integer getTimeout(); // 超时时间, 单位毫秒

	String getSn(); // 超时信息唯一流水号
}
