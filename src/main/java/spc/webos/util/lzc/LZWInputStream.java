package spc.webos.util.lzc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LZWInputStream extends InputStream
{
	public LZWInputStream(InputStream inputstream)
	{
		in = inputstream;
		entry = new ByteBuffer(128);
		max_mask_size = -1;
		mask_size = 9;
		w_code = -1;
	}

	public void setNoHeader()
	{
		if (max_mask_size < 0) max_mask_size = 16;
	}

	private int readCode(int i) throws IOException
	{
		for (; offset < i; offset += 8)
		{
			int j = in.read();
			if (j < 0) return -1;
			buffer |= j << offset;
		}

		int k = (1 << i) - 1;
		int l = buffer & k;
		buffer >>>= i;
		offset -= i;
		return l;
	}

	private void readHeader() throws IOException
	{
		int i = readCode(8) << 8 | readCode(8);
		if (i != 8093) throw new RuntimeException("Bad magic number " + i);
		int j = readCode(8);
		block_mode = (j & 0x80) != 0;
		max_mask_size = j & 0x1f;
		if (max_mask_size > 16)
		{
			throw new RuntimeException("Cannot handle " + max_mask_size + " bits");
		}
		else
		{
			dict = new LZWDict(1 << max_mask_size);
			return;
		}
	}

	private int uncompress() throws IOException
	{
		int i = readCode(mask_size);
		code_count++;
		if (i < 0) return -1;
		if (i == 256)
		{
			for (; block_mode && code_count % 8 != 0; code_count++)
				readCode(mask_size);

			dict.reset();
			entry.reset();
			w_code = -1;
			mask_size = 9;
			return 0;
		}
		int j = dict.size();
		if (i < j) dict.get((char) i, entry);
		else if (i == j) entry.append(entry.rawBuffer()[0]);
		else throw new IOException("Invalid code " + i);
		buffer_read_offset = 0;
		if (w_code >= 0 && j < 1 << max_mask_size)
		{
			dict.put((char) w_code, entry.rawBuffer()[0]);
			if (j + 1 >= 1 << mask_size && mask_size < max_mask_size)
			{
				mask_size++;
				code_count = 0;
			}
		}
		w_code = (char) i;
		return entry.size();
	}

	public int read() throws IOException
	{
		if (max_mask_size < 0) readHeader();
		while (buffer_read_offset >= entry.size())
			if (uncompress() < 0) return -1;
		return 0xff & entry.rawBuffer()[buffer_read_offset++];
	}

	public static void uncompress(InputStream inputstream, OutputStream outputstream)
			throws IOException
	{
		LZWInputStream lzwinputstream = new LZWInputStream(inputstream);
		byte abyte0[] = new byte[128];
		int i;
		while ((i = lzwinputstream.read(abyte0)) >= 0)
			outputstream.write(abyte0, 0, i);
		outputstream.flush();
	}

	private InputStream in;
	private int buffer;
	private int offset;
	private int max_mask_size;
	private LZWDict dict;
	private boolean block_mode;
	private ByteBuffer entry;
	private int mask_size;
	private int w_code;
	private int buffer_read_offset;
	private int code_count;
}
