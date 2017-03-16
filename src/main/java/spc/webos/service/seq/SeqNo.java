package spc.webos.service.seq;

import spc.webos.exception.AppException;

public interface SeqNo extends UUID
{
	long nextId(String name) throws AppException;
}
