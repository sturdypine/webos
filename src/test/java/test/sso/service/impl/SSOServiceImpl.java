package test.sso.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.model.UserPO;
import spc.webos.service.BaseService;
import test.sso.service.SSOService;

@Service
public class SSOServiceImpl extends BaseService implements SSOService {

	@Override
	@Transactional(value = "default", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void sso() throws Exception {
		((SSOService) self).sso1();
		((SSOService) self).sso2();
	}

	@Override
	@Transactional(value = "default", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void sso1() throws Exception {
		UserPO user = new UserPO("admin");
		user.setName("admin1");
		persistence.update(user);
	}

	@Override
	@Transactional(value = "default", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void sso2() throws Exception {
		UserPO user = new UserPO("esb");
		user.setName("esb1");
		persistence.update(user);
		throw new Exception("sso");
	}
}
