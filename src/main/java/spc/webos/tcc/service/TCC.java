package spc.webos.tcc.service;

/**
 * TCC原子服务流水记录
 * 
 * @author chenjs
 *
 */
public interface TCC
{
	String getSn();

	Integer getStatus();

	void setStatus(Integer status);

	void setTryTm(String tm);

	void setConfirmTm(String tm);

	void setCancelTm(String tm);
}
