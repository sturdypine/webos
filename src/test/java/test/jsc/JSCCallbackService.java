package test.jsc;

import java.util.List;
import java.util.Map;

import spc.webos.exception.AppException;
import spc.webos.model.UserPO;

public interface JSCCallbackService
{
	int notice();
	
	int update(String clazz, Map<String, Object> map) throws AppException;
	
	List<UserPO> getUser(String code) throws AppException;

	void callback(Integer num, Map<String, String> status);
	
//	void callback(Map<String, String> status);
}
