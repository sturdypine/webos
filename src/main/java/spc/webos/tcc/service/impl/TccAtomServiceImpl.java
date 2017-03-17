package spc.webos.tcc.service.impl;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.tcc.service.TCC;
import spc.webos.tcc.service.TccAtomService;

public class TccAtomServiceImpl extends BaseTccAtomService implements TccAtomService
{
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int doTring(TCC tcc)
	{
		return super.doTring(tcc);
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public int doTried(TCC tcc)
	{
		return super.doTried(tcc);
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public int doConfirm(TCC tcc)
	{
		return super.doConfirm(tcc);
	}

	// 1. 如果某tcc交易发起原交易是因为流水号存在，此时再发起cancel交易则会cancel掉别人的原交易
	// 2. 如果原交易晚于cancel交易到达，需要预先站位
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public int doCancel(TCC tcc)
	{
		return super.doCancel(tcc);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int insertCancel(TCC tcc)
	{
		return super.insertCancel(tcc);
	}

}
