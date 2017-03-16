package spc.webos.service.seq;

import spc.webos.exception.AppException;

public interface UUID
{
	long uuid() throws AppException;
	
	String format(long uuid);
}
