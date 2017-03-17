package spc.webos.service.seq.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.service.seq.DbSeqNoService;

/**
 *
 * 
 * @author chenjs
 * 
 */
@Service("dbSeqNoService")
@Transactional
public class DbSeqNoServiceImpl extends BaseDbSeqNoService implements DbSeqNoService
{
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
	public long uuid()
	{
		return nextId(defaultName);
	}

	// 必须是可重复读，事务传播级别是新事务
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
	public long nextId(String name)
	{
		return super.nextId(name);
	}
}
