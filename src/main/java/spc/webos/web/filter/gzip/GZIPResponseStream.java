package spc.webos.web.filter.gzip;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class GZIPResponseStream extends ServletOutputStream
{
	// protected ByteArrayOutputStream baos = null;
	protected GZIPOutputStream gzipstream = null;
	protected boolean closed = false;
	protected HttpServletResponse response = null;
	protected ServletOutputStream output = null;

	// public OutputStream cache; // 如果服务器缓存的filter调用

	// byte[] b = new byte[6];
	// int last = 0;
	// byte[] chinese = new byte[2];

	public GZIPResponseStream(HttpServletResponse response) throws IOException
	{
		super();
		closed = false;
		this.response = response;
		response.addHeader("Content-Encoding", "gzip");
		output = response.getOutputStream();
		// baos = new ByteArrayOutputStream();
	}

	// 从缓冲获得的压缩内容. 直接写入...
	public void writeGZIP(byte[] b) throws IOException
	{
		/*response.setContentLength(b.length);
		
		int l = b.length / 100;
		int l1 = b.length % 100;
		for (int i = 0; i < l; i++)
		{
			output.write(b, i * 100, 100);
			output.flush();
		}
		System.out.println(l);
		output.write(b,l*100,l1);
		output.flush();
		response.flushBuffer();
		output.close();*/
		output.write(b);

		// closed = true;
		// output.flush();
		// output.close();
	}

	public void close() throws IOException
	{
		// System.out.println("gzip close..." + (gzipstream == null));
		if (closed) return;
		if (gzipstream == null)
		{
			output.close();
		}
		else
		{
			gzipstream.finish();
			gzipstream.close();
			// if (cache != null) cache.close();
		}

		// byte[] bytes = baos.toByteArray();
		// response.addHeader("Content-Length", Integer.toString(bytes.length));
		// output.write(bytes);
		// // output.flush();
		// output.close();
		closed = true;
	}

	public void flush() throws IOException
	{
		// System.out.println("gzip flush..." + (gzipstream == null));
		if (closed) return;
		if (gzipstream == null) output.flush();
		else
		{
			gzipstream.flush();
			// if (cache != null) cache.flush();
		}
	}

	public void write(int b) throws IOException
	{
		// System.out.print(b);
		/*
		 * if (b < 0) { if (last >= 0) { last = b; return; } chinese[0] = (byte)
		 * last; chinese[1] = (byte) b; System.out.println(new
		 * String(chinese,"gb2312")); convert(new String(chinese)); last = 0;
		 * for (int i = 0; i < this.b.length; i++) gzipstream.write(this.b[i]);
		 * return; } else last = 0;
		 */
		if (gzipstream == null) gzipstream = new GZIPOutputStream(output);
		gzipstream.write((byte) b);
		// if (cache != null) cache.write((byte) b);
	}

	public void write(byte b[]) throws IOException
	{
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		// System.out.println("writing...");
		if (gzipstream == null) gzipstream = new GZIPOutputStream(output);
		gzipstream.write(b, off, len);
		// if (cache != null) cache.write(b, off, len);
		// if (b == null) {
		// throw new NullPointerException();
		// } else if ((off < 0) || (off > b.length) || (len < 0) ||
		// ((off + len) > b.length) || ((off + len) < 0)) {
		// throw new IndexOutOfBoundsException();
		// } else if (len == 0) {
		// return;
		// }
		// for (int i = 0 ; i < len ; i++) {
		// write(b[off + i]);
		// }
	}

	/*
	 * void convert(String str) { int len = 0; char c; int i, j; for (i = 0; i <
	 * str.length(); i++) { c = str.charAt(i); if (c > 255) { b[len++] = '\\';
	 * b[len++] = 'u'; j = (c >>> 8); String tmp = Integer.toHexString(j); if
	 * (tmp.length() == 1) { b[len++] = '0'; b[len++] = (byte) tmp.charAt(0); }
	 * else { b[len++] = (byte) tmp.charAt(0); b[len++] = (byte) tmp.charAt(1); }
	 * j = (c & 0xFF); tmp = Integer.toHexString(j); if (tmp.length() == 1) {
	 * b[len++] = '0'; b[len++] = (byte) tmp.charAt(0); } else { b[len++] =
	 * (byte) tmp.charAt(0); b[len++] = (byte) tmp.charAt(1); } } else
	 * System.out.println("Error: GZIPResponseStream convert..."); } }
	 */

	public boolean closed()
	{
		return closed;
	}

	public void reset()
	{
		// noop
	}
}
