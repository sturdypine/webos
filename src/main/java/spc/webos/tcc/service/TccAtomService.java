package spc.webos.tcc.service;

/**
 * TCC原子服务基类
 * 
 * @author chenjs
 *
 */
public interface TccAtomService
{
	int doTring(TCC tcc);

	int doTried(TCC tcc);

	int doConfirm(TCC tcc);

	int doCancel(TCC tcc);

	int insertCancel(TCC tcc);
}
