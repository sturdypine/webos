package spc.webos.service.seq;

import spc.webos.exception.AppException;

public interface DbSeqNoService extends SeqNo
{
	void synDB() throws AppException;
}
