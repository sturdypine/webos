package spc.webos.util.bytes;

/**
 * 头长度生成模式
 * 
 * @author chenjs
 * 
 */
public interface IHeadLength
{
	byte[] lenBytes(int len);

	int length(byte[] lenBytes);

	int getHdrLen();
}
