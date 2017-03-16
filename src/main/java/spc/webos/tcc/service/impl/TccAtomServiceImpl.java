package spc.webos.tcc.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.service.BaseService;
import spc.webos.tcc.Transaction;
import spc.webos.tcc.service.TCC;
import spc.webos.tcc.service.TccAtomService;

public class TccAtomServiceImpl extends BaseService implements TccAtomService
{
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int doTring(TCC tcc)
	{
		// 所有交易第一次进来必须是状态失败，业务执行成功后再修改状态为成功
		tcc.setStatus(Transaction.STATUS_TRY_FAIL);
		try
		{
			return persistence.insert(tcc);
		}
		catch (DuplicateKeyException dke)
		{ // try因为流水号重复失败，是不能cancel的
			log.warn("tcc sn repeat: " + tcc.getSn(), dke);
			throw new AppException(AppRetCode.REPEAT_SN, new Object[] { tcc.getSn() },
					dke.toString());
		}
	}

	public int doTried(TCC tcc)
	{
		tcc.setStatus(Transaction.STATUS_TRIED);
		return persistence.update(tcc);
	}

	protected <T> List<T> findTried(T po)
	{
		// 通过业务唯一流水号，找到当前状态为tried的流水记录
		((TCC) po).setStatus(Transaction.STATUS_TRIED);
		List<T> tried = persistence.get(po);
		if (tried == null || tried.isEmpty())
		{
			log.info("No tried tcc by:{}", ((TCC) po).getSn());
			throw new AppException(AppRetCode.TCC_XID_NOEXISTS,
					new Object[] { ((TCC) po).getSn() });
		}
		return tried;
	}

	public int doConfirm(TCC tcc)
	{
		// 需要update 状态为确认
		tcc.setConfirmTm(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date()));
		tcc.setStatus(Transaction.STATUS_CONFIRMED);
		return persistence.update(tcc);
	}

	// 1. 如果某tcc交易发起原交易是因为流水号存在，此时再发起cancel交易则会cancel掉别人的原交易
	// 2. 如果原交易晚于cancel交易到达，需要预先站位
	public int doCancel(TCC tcc)
	{
		tcc.setConfirmTm(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date()));
		tcc.setStatus(Transaction.STATUS_CANCELED); // tried/canceled状态都可以反复cancel
		int rows = persistence.update(tcc, (String[]) null,
				"and " + statusColumn + " in(" + Transaction.STATUS_TRIED + ","
						+ Transaction.STATUS_TRY_FAIL + "," + Transaction.STATUS_CANCELED + ","
						+ Transaction.STATUS_CANCEL_FAIL + ")",
				false, null);
		if (rows == 1) return 1; // 正常返回
		if (rows > 1)
		{ // 状态跳转时，必须根据原sn只能改变其中一条记录，0或者多条都说明有问题
			log.info("tcc sn:{}, status:{}, rows:{}", tcc.getSn(), Transaction.STATUS_CANCELED,
					rows);
			throw new AppException(AppRetCode.TCC_STATUS_CHANGE_FAIL,
					new Object[] { tcc.getSn(), Transaction.STATUS_CANCELED, rows });
		}
		// 2. 如果没有记录， 则插入一条记录占位，防止原交易过来，这样原交易过来直接失败
		return ((TccAtomService) self).insertCancel(tcc);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public int insertCancel(TCC tcc)
	{
		log.info("no rows to cancel, insert a blank recode, sn:{}", tcc.getSn());
		return persistence.insert(tcc);
	}

	protected String statusColumn = "tccStatus";

	public void setStatusColumn(String statusColumn)
	{
		this.statusColumn = statusColumn;
	}
}
