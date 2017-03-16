package spc.webos.util.lzc;

class ByteBuffer
{
	public ByteBuffer(int i)
	{
		content = new byte[i];
	}

	public void reset()
	{
		length = 0;
	}

	public int size()
	{
		return length;
	}

	public byte[] rawBuffer()
	{
		return content;
	}

	public void append(byte byte0)
	{
		if (length >= content.length)
		{
			byte abyte0[] = content;
			content = new byte[abyte0.length + 16];
			System.arraycopy(abyte0, 0, content, 0, length);
		}
		content[length++] = byte0;
	}

	public void reverse()
	{
		for (int i = 0; i < length / 2; i++)
		{
			byte byte0 = content[i];
			content[i] = content[length - 1 - i];
			content[length - 1 - i] = byte0;
		}

	}

	private byte content[];
	private int length;
}
