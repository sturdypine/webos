package spc.webos.service.common;

import java.util.List;
import java.util.Map;

public interface AppRegisterZKService
{
	List<Map<String, Object>> getNodeValue(String path) throws Exception;
}
