package spc.webos.web.filter.gzip;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class GZIPRequestStream extends ServletInputStream
{
	protected GZIPInputStream gzipstream = null;
	protected boolean closed = false;

	public GZIPRequestStream(HttpServletRequest req) throws IOException
	{
		super();
		closed = false;
		gzipstream = new GZIPInputStream(req.getInputStream());
	}

	public int read() throws IOException
	{
		return gzipstream.read();
	}

	public int read(final byte[] b) throws IOException
	{
		return gzipstream.read(b);
	}

	public int read(final byte[] b, final int offset, final int length) throws IOException
	{
		return gzipstream.read(b, offset, length);
	}

	// Leave implementation of readLine() in superclass alone, even if it's not
	// so efficient

	public long skip(final long n) throws IOException
	{
		return gzipstream.skip(n);
	}

	public int available() throws IOException
	{
		return gzipstream.available();
	}

	public void close() throws IOException
	{
		if (!closed)
		{
			gzipstream.close();
			closed = true;
		}
	}

	public synchronized void mark(final int readlimit)
	{
		gzipstream.mark(readlimit);
	}

	public synchronized void reset() throws IOException
	{
		gzipstream.reset();
	}

	public boolean markSupported()
	{
		return gzipstream.markSupported();
	}

//	private void checkClosed()
//	{
//		if (closed)
//		{
//			throw new IllegalStateException("Stream is already closed");
//		}
//	}

	public String toString()
	{
		return "CompressingServletInputStream";
	}
}
