package spc.webos.util.lzc;

class LZWHash
{
	public LZWHash(int i)
	{
		entries = new char[(int) ((double) i * 1.3300000000000001D)];
		entriesFree = new boolean[entries.length];
		parent = new char[i];
		data = new byte[i];
		reset();
	}

	public void reset()
	{
		size = 0;
		for (int i = 0; i < entriesFree.length; i++)
			entriesFree[i] = true;

		size = 257;
	}

	public int size()
	{
		return size;
	}

	private int calcHash(char c, byte byte0)
	{
		int i = ((0xffff & c) << 8 ^ byte0 & 0xff) * 31;
		return Math.abs(i) % entries.length;
	}

	public int putOrGet(int i, byte byte0)
	{
		if (i < 0) return 0xff & byte0;
		int j = calcHash((char) i, byte0);
		for (int k = 1; !entriesFree[j]; k++)
		{
			char c = entries[j];
			if (parent[c] == i && data[c] == byte0) return c;
			j = Math.abs(j + k * k) % entries.length;
		}

		if (size >= data.length)
		{
			return -2;
		}
		else
		{
			entries[j] = (char) size;
			entriesFree[j] = false;
			parent[size] = (char) i;
			data[size] = byte0;
			size++;
			return -1;
		}
	}

	public static final int COMPRESS_MAGIC_NUMBER = 8093;
	public static final int BLOCK_MODE_MASK = 128;
	public static final int MAX_MASK_SIZE_MASK = 31;
	public static final int MAX_MASK_SIZE = 16;
	public static final int INITIAL_MASK_SIZE = 9;
	public static final char CLEAR_CODE = 256;
	private static final int HASH_PRIME = 31;
	private char entries[];
	private boolean entriesFree[];
	private char parent[];
	private byte data[];
	private int size;
}
