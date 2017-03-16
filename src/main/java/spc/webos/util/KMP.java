package spc.webos.util;

public class KMP
{
	public static int[] next(String sub)
	{
		int[] a = new int[sub.length()];
		char[] c = sub.toCharArray();
		int length = sub.length();
		int i = 0, j;
		a[0] = -1;
		for (j = 1; j < length; j++)
		{
			i = a[j - 1];
			while (i >= 0 && c[j] != c[i + 1])
				i = a[i];
			if (c[j] == c[i + 1]) a[j] = i + 1;
			else a[j] = -1;
		}
		return a;
	}

	public static int[] next(byte[] sub)
	{
		int[] a = new int[sub.length];
		int length = sub.length;
		int i = 0, j;
		a[0] = -1;
		for (j = 1; j < length; j++)
		{
			i = a[j - 1];
			while (i >= 0 && sub[j] != sub[i + 1])
				i = a[i];
			if (sub[j] == sub[i + 1]) a[j] = i + 1;
			else a[j] = -1;
		}
		return a;
	}

	public static int[] next(char[] c)
	{
		int[] a = new int[c.length];
		int length = c.length;
		int i = 0, j;
		a[0] = -1;
		for (j = 1; j < length; j++)
		{
			i = a[j - 1];
			while (i >= 0 && c[j] != c[i + 1])
				i = a[i];
			if (c[j] == c[i + 1]) a[j] = i + 1;
			else a[j] = -1;
		}
		return a;
	}

	public static int indexOf(String str, String sub)
	{
		return indexOf(str, sub, next(sub));
	}

	public static int indexOf(String str, String sub, int[] next)
	{
		char[] ch1 = str.toCharArray();
		char[] ch2 = sub.toCharArray();
		int i = 0, j = 0;
		for (; i < ch1.length;)
		{
			if (ch1[i] == ch2[j])
			{
				if (j == ch2.length - 1) return i - ch2.length + 1;
				j++;
				i++;
			}
			else if (j == 0) i++;
			else j = next[j - 1] + 1;
		}
		return -1;
	}

	public static int lastIndexOf(byte[] str, int offset, byte[] sub)
	{
		byte[] strR = new byte[str.length];
		for (int i = 0; i < str.length; i++)
			strR[str.length - i - 1] = str[i];
		byte[] subR = new byte[sub.length];
		for (int i = 0; i < sub.length; i++)
			subR[sub.length - i - 1] = sub[i];
		int lastIndex = indexOf(strR, offset, subR);
		if (lastIndex < 0) return -1;
		return str.length - lastIndex - sub.length;
	}

	public static int indexOf(byte[] str, int offset, byte[] sub)
	{
		return indexOf(str, offset, sub, next(sub));
	}

	public static int indexOf(byte[] ch1, int offset, byte[] ch2, int[] next)
	{
		int i = offset, j = 0;
		for (; i < ch1.length;)
		{
			if (ch1[i] == ch2[j])
			{
				if (j == ch2.length - 1) return i - ch2.length + 1;
				j++;
				i++;
			}
			else if (j == 0) i++;
			else j = next[j - 1] + 1;
		}
		return -1;
	}

	public static int indexOf(char[] str, int offset, char[] sub)
	{
		return indexOf(str, offset, sub, next(sub));
	}

	public static int indexOf(char[] ch1, int offset, char[] ch2, int[] next)
	{
		int i = offset, j = 0;
		for (; i < ch1.length;)
		{
			if (ch1[i] == ch2[j])
			{
				if (j == ch2.length - 1) return i - ch2.length + 1;
				j++;
				i++;
			}
			else if (j == 0) i++;
			else j = next[j - 1] + 1;
		}
		return -1;
	}

	public static void main(String[] args)
	{
		String sub = "aaba";
		String str = "gdsaadfdgffccsdaabaccfdaddaabaccfaddddaabcga";
		// int[] next = next(sub);
		System.out.println(indexOf(str.getBytes(), 16, sub.getBytes()));
		System.out.println(lastIndexOf("12345abc0000".getBytes(), 0, "abc".getBytes()));
	}
}
