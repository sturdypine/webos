package spc.webos.tcc;

import java.util.Collection;

import spc.webos.exception.AppException;

public interface Repository
{
	String createXid();

	String group();

	Transaction create(Transaction transaction) throws Exception;

	Terminator addTerminator(Transaction transaction, TCCTransactional tcc, String tid, int seq,
			Object target, String clazz, String m, Class[] parameterTypes, Object[] args)
					throws Exception;

	Transaction updateStatus(Transaction transaction);

	Terminator updateStatus(Terminator terminator);

	Transaction delete(Transaction transaction, boolean byFail) throws Exception;

	Transaction find(String xid) throws Exception;

	Collection<String> findAll(int from, int limit) throws AppException;

	Collection<String> findErr(int from, int limit) throws AppException;
}
