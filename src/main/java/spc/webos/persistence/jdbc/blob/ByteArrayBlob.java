package spc.webos.persistence.jdbc.blob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.util.bytes.BytesUtil;

public class ByteArrayBlob implements Blob, java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	int len;
	byte[] buf;
	int offset;

	static
	{
		JsonUtil.registerStrSerializer(ByteArrayBlob.class);
	}

	public ByteArrayBlob(byte[] buf)
	{
		this.buf = buf;
		this.offset = 0;
		len = buf.length;
	}

	public ByteArrayBlob(byte[] buf, int offset, int len)
	{
		this.buf = buf;
		this.offset = offset;
		this.len = len;
	}

	public long length() throws SQLException
	{
		return len - offset;
	}

	public byte[] getBytes(long pos, int length) throws SQLException
	{
		if (pos == 0 && length == len) return buf;
		return BytesUtil.arraycopy(buf, (int) pos, length);
	}

	public InputStream getBinaryStream() throws SQLException
	{
		return new ByteArrayInputStream(buf, offset, len);
	}

	public long position(byte pattern[], long start) throws SQLException
	{
		return -1;
	}

	public long position(Blob pattern, long start) throws SQLException
	{
		return -1;
	}

	public int setBytes(long pos, byte[] bytes) throws SQLException
	{
		this.buf = bytes;
		return bytes.length;
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
	{
		this.buf = bytes;
		return bytes.length;
	}

	public OutputStream setBinaryStream(long pos) throws SQLException
	{
		return null;
	}

	public void truncate(long len) throws SQLException
	{
	}

	public InputStream getBinaryStream(long pos, long length) throws SQLException
	{
		return new ByteArrayInputStream(buf, (int) pos, (int) length);
	}

	public String base64() throws Exception
	{
		if (buf == null) return StringX.EMPTY_STRING;
		return new String(StringX.encodeBase64(bytes()));
	}

	public byte[] bytes()
	{
		if (buf == null) return null;
		if (offset <= 0) return buf;
		byte[] content = new byte[len];
		System.arraycopy(buf, offset, content, 0, len);
		return content;
	}

	public String toString()
	{
		return StringX.base64(buf);
	}

	@Override
	public void free() throws SQLException
	{
	}
}
