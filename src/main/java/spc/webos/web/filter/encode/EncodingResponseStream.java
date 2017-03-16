package spc.webos.web.filter.encode;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingResponseStream extends ServletOutputStream
{
	protected boolean closed = false;
	protected HttpServletResponse response = null;
	protected ServletOutputStream output = null;
	int len;
	protected Logger log = LoggerFactory.getLogger(getClass());

	public EncodingResponseStream(HttpServletResponse response) throws IOException
	{
		super();
		closed = false;
		len = 0;
		this.response = response;
		this.output = response.getOutputStream();
		// System.out.println(response.getClass()+", "+output);
	}

	public void close() throws IOException
	{
		if (closed) throw new IOException("This output stream has already been closed");
		// System.out.println("close...");
		output.close();
		closed = true;
	}

	public void flush() throws IOException
	{
		if (closed) throw new IOException("Cannot flush a closed output stream");
		// System.out.println("flush...");
		output.flush();
	}

	public void write(int b) throws IOException
	{
		if (closed) throw new IOException("Cannot write to a closed output stream");
		// System.out.println((byte)b);
		if (b > 255)
		{
			log.info(">255=" + b);
			len += 6;
			output.write('\\');
			output.write('u');

			int j = (b >>> 8);
			String tmp = Integer.toHexString(j);
			if (tmp.length() == 1) output.write('0');
			output.write(tmp.getBytes());

			j = (b & 0xFF);
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1) output.write('0');
			output.write(tmp.getBytes());
		}
		else
		{
			len++;
			output.write((byte) b);
		}
	}

	public void write(byte b[]) throws IOException
	{
		log.info("rep:" + new String(b));
		// b = "ฮารว".getBytes();
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		log.debug("writing...");
		if (closed) throw new IOException("Cannot write to a closed output stream");
		if (b == null)
		{
			throw new NullPointerException();
		}
		else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
				|| ((off + len) < 0)) throw new IndexOutOfBoundsException();
		else if (len == 0) { return; }
		for (int i = 0; i < len; i++)
			write(b[off + i]);
		// output.write(b, off, len);
	}

	public boolean closed()
	{
		return this.closed;
	}

	public void reset()
	{
		// noop
	}
}
