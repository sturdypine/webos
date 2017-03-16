package spc.webos.persistence.jdbc;

import org.springframework.dao.NonTransientDataAccessException;

/**
 * see org.springframework.dao.DataIntegrityViolationException
 * 由于数据插入重复键是常用业务判断的异常类型
 * 
 * @author spc
 * 
 */
public class DuplicateRowException extends NonTransientDataAccessException
{
	private static final long serialVersionUID = 1L;

	public DuplicateRowException(String msg)
	{
		super(msg);
	}

	public DuplicateRowException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}