package spc.webos.util.bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BytesUtil
{
	public static byte[] merge(byte[]... buf) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (byte[] b : buf)
			baos.write(b);
		return baos.toByteArray();
	}

	public static byte[] merge(List<byte[]> slices) throws Exception
	{
		if (slices == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i = 0; i < slices.size(); i++)
			baos.write(slices.get(i));
		return baos.toByteArray();
	}

	public static byte[] arraycopy(byte[] src, int offset, int len)
	{
		byte[] buf = new byte[len];
		System.arraycopy(src, offset, buf, 0, len);
		return buf;
	}

	// 701_20131113
	public static byte[] packSlice(List<byte[]> slices, IHeadLength hdLen) throws IOException
	{
		if (slices == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		packSlice(baos, slices, hdLen);
		return baos.toByteArray();
	}

	public static void packSlice(OutputStream os, List<byte[]> slices, IHeadLength hdLen)
			throws IOException
	{
		if (slices == null) return;
		for (int i = 0; i < slices.size(); i++)
		{
			byte[] buf = slices.get(i);
			os.write(hdLen.lenBytes(buf.length));
			os.write(buf);
		}
	}

	public static List<byte[]> unpackSlice(byte[] buf, IHeadLength hdLen) throws IOException
	{
		if (buf == null) return null;
		ArrayList<byte[]> slices = new ArrayList<byte[]>();
		int offset = 0;
		while (offset < buf.length)
		{
			int len = hdLen.length(arraycopy(buf, offset, hdLen.getHdrLen()));
			slices.add(arraycopy(buf, offset + hdLen.getHdrLen(), len));
			offset += hdLen.getHdrLen() + len;
		}
		return slices;
	}

	public static byte[] packSOP(byte[] buf, IHeadLength hdLen, int sliceSize) throws IOException
	{
		if (buf == null) return buf;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int offset = 0;
		while (offset < buf.length)
		{
			int len = (buf.length - offset > sliceSize) ? sliceSize : buf.length - offset;
			if (baos.size() == 0) baos.write(hdLen.lenBytes(buf.length)); // 第一片使用全报文长度
			else baos.write(hdLen.lenBytes(buf.length - offset));
			baos.write(buf, offset, len);
			offset += len;
		}
		return baos.toByteArray();
	}

	public static byte[] unpackSOP(byte[] buf, IHeadLength hdLen, int sliceSize) throws IOException
	{
		if (buf == null) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int offset = 0;
		while (offset < buf.length)
		{
			int len = buf.length - offset - hdLen.getHdrLen();
			if (len > sliceSize) len = sliceSize;
			baos.write(buf, offset + hdLen.getHdrLen(), len);
			offset += hdLen.getHdrLen() + len;
		}
		return baos.toByteArray();
	}
}
