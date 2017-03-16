package spc.webos.util.lzc;

class LZWDict
{
	public LZWDict(int i)
	{
		data = new byte[i];
		parent = new char[i];
		size = 0;
		for (int j = 0; j < 256; j++)
			data[j] = (byte) j;

		size = 257;
		resetSize = size;
	}

	public void reset()
	{
		size = resetSize;
	}

	public int size()
	{
		return size;
	}

	public char put(char c, byte byte0)
	{
		if (c >= size)
		{
			throw new ArrayIndexOutOfBoundsException(Integer.toString(c));
		}
		else
		{
			data[size] = byte0;
			parent[size] = c;
			size++;
			return (char) (size - 1);
		}
	}

	public void get(char c, ByteBuffer bytebuffer)
	{
		if (c >= size) throw new ArrayIndexOutOfBoundsException(Integer.toString(c));
		bytebuffer.reset();
		for (; c > '\u0100'; c = parent[c])
			bytebuffer.append(data[c]);

		bytebuffer.append(data[c]);
		bytebuffer.reverse();
	}

	private byte data[];
	private char parent[];
	private int resetSize;
	private int size;
}
