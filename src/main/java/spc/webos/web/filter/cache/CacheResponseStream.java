package spc.webos.web.filter.cache;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class CacheResponseStream extends ServletOutputStream
{
	protected boolean closed = false;
	protected HttpServletResponse response = null;
	protected ServletOutputStream output = null;
	protected OutputStream cache = null;

	public CacheResponseStream(HttpServletResponse response, OutputStream cache)
			throws IOException
	{
		super();
		output = response.getOutputStream();
		closed = false;
		this.response = response;
		this.cache = cache;
	}

	public void close() throws IOException
	{
		if (closed) throw new IOException(
				"This output stream has already been closed");
		output.close();
		cache.close();
		closed = true;
	}

	public void flush() throws IOException
	{
		if (closed) { throw new IOException(
				"Cannot flush a closed output stream"); }
		output.flush();
		cache.flush();
	}

	public void write(int b) throws IOException
	{
		if (closed) throw new IOException(
				"Cannot write to a closed output stream");
		output.write((byte) b);
		cache.write((byte) b);
	}

	public void write(byte b[]) throws IOException
	{
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		if (closed) throw new IOException(
				"Cannot write to a closed output stream");
		output.write(b, off, len);
		cache.write(b, off, len);
	}

	public boolean closed()
	{
		return (this.closed);
	}

	public void reset()
	{
	}
}
