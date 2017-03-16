package spc.webos.util.bytes;

import spc.webos.util.NumberX;
import spc.webos.util.StringX;
import spc.webos.util.charset.EBCDUtil;

public class DefaultHeadLength implements IHeadLength
{
	public int offset(int hlen)
	{
		return containHdrLenSelf ? hlen : hdrLen + hlen;
	}

	public int remain(int len, int hLen)
	{
		if (!containHdrLenSelf) return hLen + hdrLen - len;
		return hLen - len;
	}

	public byte[] lenBytes(int len)
	{
		if (hdrLen <= 0) return null; // 710_20140606 如果没有长度头则返回空
		len += (containHdrLenSelf ? hdrLen : 0); // 是否长度头信息的长度包含长度头字节数
		if (hdrLenBinary) return NumberX.int2bytes(len, hdrLen); // 采用二进制表示模式
		String strlen = StringX.int2str(String.valueOf(len), hdrLen); // 长度固定为10进制的8个字节，不足前面补0
		return len2bcd ? EBCDUtil.gbk2bcd(strlen) : strlen.getBytes();
	}

	public int length(byte[] lenBytes)
	{
		if (hdrLenBinary) return NumberX.bytes2int(lenBytes) - (containHdrLenSelf ? hdrLen : 0);
		return new Integer(len2bcd ? EBCDUtil.bcd2gbk(lenBytes) : new String(lenBytes).trim())
				.intValue() - (containHdrLenSelf ? hdrLen : 0);
	}

	public DefaultHeadLength()
	{
	}

	public DefaultHeadLength(int hdrLen, boolean hdrLenBinary)
	{
		this.hdrLenBinary = hdrLenBinary;
		this.hdrLen = hdrLen;
	}

	public DefaultHeadLength(int hdrLen, boolean hdrLenBinary, boolean containHdrLenSelf,
			boolean len2bcd)
	{
		this.hdrLenBinary = hdrLenBinary;
		this.containHdrLenSelf = containHdrLenSelf;
		this.len2bcd = len2bcd;
		this.hdrLen = hdrLen;
	}

	public int getHdrLen()
	{
		return hdrLen;
	}

	public int hdrLen = 8; // 每次发送的头长度, 如果为<=0表示不需要发送长度标识
	public boolean containHdrLenSelf; // chenjs 2012-11-22 报文头长度是否包含头长度本身,
	// 比如长度头8个字节，内容200个字节，长度头信息为208字节
	public boolean len2bcd; // 发送的长度字段是否需要BCD转码
	public boolean hdrLenBinary; // 头字节的长度信息用二进制表示，而非asc码

	public String toString()
	{
		return "DHL:" + hdrLen + ":" + hdrLenBinary + ":" + len2bcd + ":" + containHdrLenSelf;
	}
}
