package spc.webos.util.lzc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LZWOutputStream extends OutputStream
{
	public LZWOutputStream(OutputStream outputstream)
	{
		out = outputstream;
		w_code = -1;
		mask_size = 9;
	}

	public void setNoHeader()
	{
		hash = new LZWHash(0x10000);
	}

	private void writeBuffer(int i) throws IOException
	{
		for (; offset > i; offset -= 8)
		{
			out.write((byte) buffer);
			size++;
			buffer >>>= 8;
		}

	}

	private void writeCode(int i, int j) throws IOException
	{
		int k = (1 << j) - 1;
		buffer |= (k & i) << offset;
		offset += j;
		writeBuffer(8);
	}

	private void writeHeader() throws IOException
	{
		writeCode(31, 8);
		writeCode(157, 8);
		writeCode(144, 8);
	}

	private void compress(int i) throws IOException
	{
		int j = hash.putOrGet(w_code, (byte) i);
		if (j >= 0)
		{
			w_code = (char) j;
		}
		else
		{
			writeCode(w_code, mask_size);
			if (j != -2 && hash.size() > 1 << mask_size) mask_size++;
			w_code = (char) i;
		}
		if (w_code < 256 && hash.size() >= 0x10000)
		{
			writeCode(256, mask_size);
			hash.reset();
			mask_size = 9;
		}
	}

	public void end() throws IOException
	{
		if (hash == null) writeHeader();
		if (w_code >= 0) writeCode(w_code, mask_size);
		writeBuffer(0);
		out.flush();
	}

	public int size()
	{
		return size;
	}

	public void write(int i) throws IOException
	{
		if (hash == null)
		{
			writeHeader();
			hash = new LZWHash(0x10000);
		}
		compress(i & 0xff);
	}

	public void flush() throws IOException
	{
		out.flush();
	}

	public void close() throws IOException
	{
		end();
		out.flush();
	}

	public static void compress(InputStream inputstream, OutputStream outputstream)
			throws IOException
	{
		LZWOutputStream lzwoutputstream = new LZWOutputStream(outputstream);
		byte abyte0[] = new byte[128];
		int i;
		while ((i = inputstream.read(abyte0)) >= 0)
			lzwoutputstream.write(abyte0, 0, i);
		lzwoutputstream.flush();
		lzwoutputstream.end();
	}

	private OutputStream out;
	private int buffer;
	private int offset;
	private int size;
	private LZWHash hash;
	private int w_code;
	private int mask_size;
}