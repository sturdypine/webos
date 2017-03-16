package spc.webos.service.alarm;

import java.util.Map;

import spc.webos.model.AlarmPO;

public interface AlarmEventService
{
	// 处理一个事件
	AlarmPO proccess(AlarmPO event) throws Exception;

	// 通过alarm_event表的id处理一个事件
	String proccessEvent(String id) throws Exception;
	
	Map<String, AlarmPO> proccessEvents(String[] ids) throws Exception;

	// 获取一个发送消息报文
	String getEventMsg(String id) throws Exception;

	// 发送一个警告信息
	String sendEventMsg(String id);
}
