package spc.webos.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Number (int, Double) handle utilities.
 * 
 * <pre>
 * usage:
 * 
 *  NumberX.getSafeIndex (4, 3);					// return 1
 *  NumberX.getSafeRange (10, -2, 3); 				// return 3
 *  NumberX.getNumberString (new Double(10.343)); 	// &quot;10.343&quot;
 *  NumberX.getInt (&quot;-234d&quot;, -100);					// -100
 * 
 *  // 调平
 *  NumberX.power (2, 4); 							// 16
 *  NumberX.sum ({0.1, 0.3, 0.6});					// 1.0	
 *  NumberX.radio ({0.2, 0.3});						// {0.4, 0.6}
 *  NumberX.hasZero ({0.1, 4.0, 0, 1.2});			// true						
 *  NumberX.zeros ({2, 5, 0, 4, 0});				// {1,1,0,1,0}
 *  NumberX.to_double ({2, 5, 0, 4});				// {2.0, 5.0, 0.0, 4.0}
 *  NumberX.to_int ({2.3, 5.8, 0.0, 4.0});			// {2, 5, 0, 4}
 *  NumberX.multiply ({5.0, 5.0}, {0.1, 0.9})		// {0.5, 4.5}
 *  NumberX.adjustSum ({2,3,5}, {0,4,5})			// {0, (40+20*3/8), (50+20*5/8)}
 *  NumberX.adjustRound ({3.3, 3.3, 3.4}, 0);		// {4, 3, 3}
 *  NumberX.isDigit (&quot;234324&quot;);						// true
 * 
 *  // Excel 行列号
 *  NumberX.Excel2Ints (&quot;AB30&quot;);					// {27, 29}
 *  NumberX.AZ2Int (&quot;AB&quot;);  						// 27
 *  NumberX.Int2AZ (27);							// &quot;AB&quot;
 * 
 * </pre>
 * 
 * @see TestNumberX, StringX
 */
public class NumberX
{
	// 703_20140321 判断SOP报文剩余多少字节
	public static int getSOPRemain(byte[] sop, int splitSize, int lenSize)
	{
		byte[] SOPLenBytes = new byte[lenSize];
		System.arraycopy(sop, 0, SOPLenBytes, 0, lenSize);
		int SOPLen = NumberX.bytes2int(SOPLenBytes);
		// 获取报文真实接收长度
		int num1 = sop.length % (splitSize + lenSize);
		int num2 = sop.length / (splitSize + lenSize);
		int num = (num1 == 0) ? num2 : num2 + 1;
		return SOPLen - (sop.length - lenSize * num);
	}

	// added by spc 2008-06-10
	/**
	 * 把一个字节数组变为整数
	 */
	public static int bytes2int(byte[] b)
	{
		return bytes2int(b, b.length);
	}

	public static int bytes2int(byte[] b, int len)
	{
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < len; i++)
		{
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static long bytes2long(byte[] v)
	{
		return (((long) v[0] << 56) + ((long) (v[1] & 255) << 48) + ((long) (v[2] & 255) << 40)
				+ ((long) (v[3] & 255) << 32) + ((long) (v[4] & 255) << 24) + ((v[5] & 255) << 16)
				+ ((v[6] & 255) << 8) + ((v[7] & 255) << 0));
	}

	/**
	 * 把一个整数变为字节数组
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] int2bytes(int num)
	{
		return int2bytes(num, 4);
	}

	public static byte[] int2bytes(int num, int len)
	{
		byte[] b = new byte[4];
		// int mask = 0xff;
		for (int i = 0; i < 4; i++)
			b[i] = (byte) (num >>> (24 - i * 8));
		if (len == 4) return b;
		byte[] nb = new byte[len];
		if (len < 4) System.arraycopy(b, 4 - len, nb, 0, len);
		else System.arraycopy(b, 0, nb, len - 4, 4);
		return nb;
	}

	public static byte[] long2bytes(long v)
	{
		byte[] value = new byte[8];
		value[0] = (byte) (v >>> 56);
		value[1] = (byte) (v >>> 48);
		value[2] = (byte) (v >>> 40);
		value[3] = (byte) (v >>> 32);
		value[4] = (byte) (v >>> 24);
		value[5] = (byte) (v >>> 16);
		value[6] = (byte) (v >>> 8);
		value[7] = (byte) (v >>> 0);
		return value;
	}

	// end

	/**
	 * 把数组index限定在安全范围内[0, array.length]
	 * 
	 * @param length
	 *            数组的大小（array.length） return index in range [0, length)
	 *            condition: length > 0; 例如： -1 --> 最后一个(array.length-1), -2 -->
	 *            倒数第2个(array.length-2) 其他
	 * @see TestNumberX
	 */
	public static int getSafeIndex(int index, int length)
	{
		return (index % length) + (index < 0 ? length : 0);
	}

	/**
	 * 把给定值n限定在指定区间[min, max] return int value in range [min, max]
	 */
	public static int getSafeRange(int n, int min, int max)
	{
		if (n < min) return min;
		if (n > max) return max;
		return n;
	}

	/**
	 * return formated string of a double. -10.0 --> -10 -10.53 --> -10.53
	 */
	public static String getNumberString(Double number)
	{
		if (number == null) return StringX.EMPTY_STRING;
		NumberFormat formatter = new DecimalFormat("#.########");
		String string = formatter.format(number.doubleValue());
		return string;
	}

	/**
	 * return formated string of a double. -10.0 --> -10 -10.53 --> -10.53
	 */
	public static String getNumberString(double number)
	{
		return getNumberString(new Double(number));
	}

	/**
	 * safe version of Integer.parseInt (); return transfered int value of given
	 * valid string, default int value for invalid string.
	 */
	public static int getInt(String s, int defaultValue)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			System.err.println("NumberX.getInt(): " + e);
			return defaultValue;
		}
	}

	/**
	 * get double from string.
	 * 
	 * @param s
	 *            double string to parse.
	 * @param defaultValue
	 *            default value if exception.
	 * @return double
	 */
	public static double getDouble(String s, double defaultValue)
	{
		try
		{
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e)
		{
			System.err.println("NumberX.getDouble(): " + e);
			return defaultValue;
		}
	}

	/**
	 * return n**m
	 */
	public static int power(int n, int m)
	{
		int rt = 1;
		for (int i = 0; i < m; i++)
			rt *= n;
		return rt;
	}

	/**
	 * @return sum of int[] array
	 * @param values
	 *            待计算的int[]
	 */
	public static double sum(double[] values)
	{
		if (values == null) return 0;
		double rt = 0.0;
		for (int i = 0; i < values.length; i++)
			rt += values[i];
		return rt;
	}

	/**
	 * 计算int[]中每个元素的占比
	 */
	public static double[] radio(double[] values)
	{
		if (values == null) return null;
		double sum = sum(values);
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++)
			rt[i] = (sum == 0) ? Double.NaN : (values[i] / sum);
		return rt;
	}

	/**
	 * 判断是否有值为0的元素
	 */
	public static boolean hasZero(double[] values)
	{
		if (values == null) return false;
		for (int i = 0; i < values.length; i++)
			if (values[i] == 0.0) return true;
		return false;
	}

	/**
	 * 把所有非零元素设为1
	 * 
	 * @param values
	 *            like {2, 5, 0, 4, 0}
	 * @return {1, 1, 0, 1, 0}
	 */
	public static double[] zeros(double[] values)
	{
		if (values == null) return null;
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++)
			rt[i] = values[i] == 0 ? 0.0 : 1.0;
		return rt;
	}

	/**
	 * 把int[]强制转换为double[]
	 */
	public static double[] to_double(int[] values)
	{
		if (values == null) return null;
		double[] rt = new double[values.length];
		for (int i = 0; i < values.length; i++)
			rt[i] = (double) values[i];
		return rt;
	}

	/**
	 * 把double[]强制转换为int[]
	 */
	public static int[] to_int(double[] values)
	{
		if (values == null) return null;
		int[] rt = new int[values.length];
		for (int i = 0; i < values.length; i++)
			rt[i] = (int) values[i];
		return rt;
	}

	/**
	 * 值（double[]）和系数（double[]）相乘
	 */
	public static double[] multiply(double[] values, double[] radios)
	{
		if (values == null || radios == null) return null;
		if (values.length != radios.length) return null;
		int n = values.length;
		double[] rt = new double[n];
		for (int i = 0; i < n; i++)
			rt[i] = values[i] * radios[i];
		return rt;
	}

	/**
	 * 总分值调平，调平结果的sum值 == 基准值的sum值，例如： bases：20%，30%，50% values: 0%, 40%, 50%
	 * --> return: 0%, (40+20*3/8)%, (50+20*5/8)%
	 * 
	 * @param v0
	 *            基准值
	 * @param v1
	 *            实际值
	 * @return 调整值
	 */
	public static double[] adjustSum(double[] v0, double[] v1)
	{
		if (v0 == null || v1 == null) return null;
		if ((v0.length != v1.length) || !hasZero(v1)) return v1;
		int n = v0.length;
		// double sum = sum (v1);
		double sum1 = sum(v0) - sum(v1);
		double[] zeros = zeros(v1);
		double[] v2 = multiply(v0, zeros);
		double[] radio = radio(v2);
		double[] rt = new double[n];
		for (int i = 0; i < n; i++)
			rt[i] = v1[i] + sum1 * radio[i];
		return rt;
	}

	/**
	 * 四舍五入调平。double[]每个值四舍五入后余数调平到某个指定值上，例如： {3.3, 3.3, 3.4} --> {3, 3, 4}
	 * 
	 * @param index
	 *            将被调平的那个值的index 验证：Math.round (未调平之和) = 调平之和
	 */
	public static double[] adjustRound(double[] values, int index)
	{
		if (values == null) return null;
		int n = values.length;
		double[] rt = new double[n];
		double sum0 = Math.round(sum(values));
		for (int i = 0; i < n; i++)
			rt[i] = Math.round(values[i]);
		double sum1 = sum(rt);
		if (sum0 != sum1)
		{
			double delta = sum0 - sum1;
			int safeIndex = getSafeIndex(index, values.length);
			rt[safeIndex] += Math.round(delta);
		}
		return rt;
	}

	/**
	 * 判断是否数字字符串，不含小数点和"-"符号
	 */
	public static boolean isDigit(String value)
	{
		if (value == null) return false;
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++)
			if (!Character.isDigit(chars[i])) return false;
		return true;
	}

	/**
	 * 把Excel的单元格位置标识转换为int(col, row)。 规则：第一个必须是字母，第二个是字母或者数字，之后的全是数字 A1->(0,0),
	 * IV3->(255,2)
	 */
	public static int[] Excel2Ints(String code)
	{
		int[] rt = new int[] { -1, -1 };
		if (code == null || code.length() < 2)
		{
			System.err.println("NumberX.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}
		char[] chars = code.toCharArray();
		if (!Character.isLetter(chars[0]))
		{
			System.err.println("NumberX.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}
		int colLen = Character.isLetter(chars[1]) ? 2 : 1;
		String col = code.substring(0, colLen);
		String row = code.substring(colLen);

		if (!isDigit(row))
		{
			System.err.println("NumberX.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}

		int col_i = AZ2Int(col);
		int row_i = getInt(row, 0);
		if (row_i == 0)
		{
			System.err.println("NumberX.Excel2Ints() 输入参数不对：" + code);
			return rt;
		}

		return new int[] { col_i, row_i - 1 };
	}

	/**
	 * 把Excel的列号(A-ZZ)转换为数值。 A->0, B->1, Z->25, AA->26, IV->255
	 */
	public static int AZ2Int(String AZ)
	{
		int range = 'Z' - 'A' + 1; // 26
		if (AZ == null || (AZ.length() != 1 && AZ.length() != 2))
		{
			System.err.println("NumberX.AZtoInt() 列代号不符合要求：" + AZ);
			return -1;
		}
		AZ = AZ.toUpperCase();
		if (AZ.length() == 1) { return (AZ.charAt(0) - 'A'); }
		char c0 = AZ.charAt(0);
		char c1 = AZ.charAt(1);
		return ((c0 - 'A' + 1) * range + (c1 - 'A'));
	}

	/**
	 * 把数值(0-255)转换为Excel的列号(A-ZZ)。 0->A, 1->B, 25->Z, 26->AA, 255->IV
	 */
	public static String Int2AZ(int i)
	{
		int range = 'Z' - 'A' + 1; // 26
		if (i < 0 || i > 255)
		{
			System.err.println("列标号出界[0-255]：" + i);
			return null;
		}
		if (i < range) return String.valueOf((char) ('A' + i));
		else return String.valueOf((char) ('A' + i / range - 1) + (char) ('A' + i % range));
	}
} // class
