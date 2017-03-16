package test.jsc.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.exception.AppException;
import spc.webos.model.UserPO;
import spc.webos.service.BaseService;
import test.jsc.JSCCallbackService;

public class JSCCallbackServiceImpl extends BaseService implements JSCCallbackService
{
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int notice()
	{
		UserPO user = new UserPO("ECIF");
		user.setName("ECIF");
		// int rows = persistence.update(user);
		// if (rows > 0) throw new RuntimeException("EX");

		return 100;
	}

	public List<UserPO> getUser(String code) throws AppException
	{
		List<UserPO> list = new ArrayList<>();
		UserPO po = new UserPO(code);
		po.setName("chenjs");
		list.add(po);
		System.out.println("getUser:: " + list);
		return list;
	}

	public int update(String clazz, Map<String, Object> map) throws AppException
	{
		log.info("c:{}, m:{}, self:{}", clazz, map, self.getClass());

		return ((JSCCallbackService) self).notice();
	}

	@Override
	public void callback(Integer num, Map<String, String> status)
	{
		log.info("callback:: num:{}, status:{}", num, status);
	}

}
