package test.tcc.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import spc.webos.model.TccTransLogPO;
import spc.webos.persistence.jdbc.datasource.DataSource;
import spc.webos.service.BaseService;
import spc.webos.tcc.TCCTransactional;
import test.tcc.DemoTCCAService;

@Service("demoATCCService")
public class DemoTCCAServiceImpl extends BaseService implements DemoTCCAService {
	protected Map<String, Object> json;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@DataSource("tcc")
	@TCCTransactional(tranlog = false)
	public String trySayHello1(String sn, String name) {
		System.out.println("trySayHello1::" + sn + ", " + name);
		// TccTransLogPO po = new TccTransLogPO(sn);
		// po.setTranCode("trySayHello1");
		// po.setTranTm(FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new
		// Date()));
		// doTring(po);
		// .... business code below

		// doTried(new TccTransLogPO(sn));
		return "sn:" + sn + ": hello, " + name;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@DataSource("tcc")
	@TCCTransactional(tranlog = false)
	public void cancelSayHello1(String sn, String name) {
		System.out.println("cancelSayHello1:" + sn + "," + name);
		// doCancel(new TccTransLogPO(sn));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@DataSource("tcc")
	@TCCTransactional(tranlog = false)
	public String trySayHello2(String sn, String name) {
		System.out.println("trySayHello2::" + sn + ", " + name);

		// doTring(po);
		// .... business code below

		// doTried(new TccTransLogPO(sn));
		// throw new RuntimeException("EX:" + "sn:" + sn + ": hello, " + name);
		return "sn:" + sn + ": hello, " + name;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@DataSource("tcc")
	@TCCTransactional(tranlog = false)
	public void confirmSayHello2(String sn, String name) {
		System.out.println("confirmSayHello2:" + sn + "," + name);

		// 需要update 状态为确认
		// doConfirm(new TccTransLogPO(sn));
		// throw new RuntimeException("EX confirmSayHello2:" + "sn:" + sn + ":
		// hello, " + name);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@DataSource("tcc")
	@TCCTransactional(tranlog = false)
	public void cancelSayHello2(String sn, String name) {
		System.out.println("cancelSayHello2:" + sn + "," + name);
		// doCancel(new TccTransLogPO(sn));
		// throw new RuntimeException("EX cancelSayHello2: sn:" + sn);
	}

	public void test() {
		System.out.println("test....");
	}
}
