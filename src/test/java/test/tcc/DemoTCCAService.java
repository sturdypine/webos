package test.tcc;

import spc.webos.tcc.TCCTransactional;
import spc.webos.tcc.TCCTransactional.XidParamPath;

public interface DemoTCCAService {
	@TCCTransactional(doConfirm = false)
	String trySayHello1(@XidParamPath String sn, String name);

	void cancelSayHello1(@XidParamPath String sn, String name);
	
	
	

	@TCCTransactional
	String trySayHello2(@XidParamPath String sn, String name);

	void confirmSayHello2(@XidParamPath String sn, String name);

	void cancelSayHello2(@XidParamPath String sn, String name);

	void test();
}
