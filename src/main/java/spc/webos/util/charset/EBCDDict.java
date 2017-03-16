package spc.webos.util.charset;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import spc.webos.util.StringX;

/**
 * 采用数据映射方式解决特殊字符的bcd转码
 * 
 * @author chenjs
 * 
 */
public class EBCDDict
{
	// gbk -> ebcdic
	private static int[] gbk2ebcdic_g;
	private static int[] gbk2ebcdic_e;
	// ebcdic -> gbk
	private static int[] ebcdic2gbk_e;
	private static int[] ebcdic2gbk_g;
	static Logger log = LoggerFactory.getLogger(EBCDDict.class);

	static
	{
		InputStream is = null;
		try
		{
			load(is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("env/config/CN2BCD.txt"));
			log.info("success to load CN2BCD.txt from classpath:env/config/CN2BCD.txt");
		}
		catch (Exception e)
		{
			log.info("cannot load CN2BCD.txt in classpath:env/config!!!");
			try
			{
				load(is = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("CN2BCD.txt"));
				log.info("success to load CN2BCD.txt classpath:CN2BCD.txt");
			}
			catch (Exception ee)
			{
				log.info("cannot load classpath:CN2BCD.txt in classpath!!!");
			}
		}
		finally
		{
			try
			{
				if (is != null) is.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	public void setGbkCvt(Resource res) throws Exception
	{
		if (gbk2ebcdic_g != null) return;
		InputStream is = null;
		try
		{
			load(is = res.getInputStream());
		}
		finally
		{
			try
			{
				if (is != null) is.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private static void load(InputStream gbkcvtIS) throws Exception
	{
		TreeMap tmp_gbk2ebcdic = new TreeMap();
		TreeMap tmp_ebcdic2gbk = new TreeMap();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(gbkcvtIS, "GBK"));
		String line;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() == 0) continue;
			String[] gbkBcd = StringX.split(line, "|");
			if (gbkBcd == null || gbkBcd.length < 2) continue;
			Integer g = new Integer(Integer.parseInt(gbkBcd[0].trim(), 16));
			Integer e = new Integer(Integer.parseInt(gbkBcd[1].trim(), 16));
			// Integer g = new Integer(Integer.parseInt(line.substring(0, 4),
			// 16));
			// Integer e = new Integer(Integer.parseInt(line.substring(5, 9),
			// 16));
			tmp_gbk2ebcdic.put(g, e);
			tmp_ebcdic2gbk.put(e, g);
		}
		reader.close();
		// gbk2ebcdic table
		gbk2ebcdic_g = new int[tmp_gbk2ebcdic.size()];
		gbk2ebcdic_e = new int[tmp_gbk2ebcdic.size()];
		int ofs = 0;
		for (Iterator it = tmp_gbk2ebcdic.keySet().iterator(); it.hasNext();)
		{
			Integer g = (Integer) it.next();
			Integer e = (Integer) tmp_gbk2ebcdic.get(g);
			gbk2ebcdic_g[ofs] = g.intValue();
			gbk2ebcdic_e[ofs++] = e.intValue();
		}
		// ebcdic2gbk table
		ebcdic2gbk_g = new int[tmp_ebcdic2gbk.size()];
		ebcdic2gbk_e = new int[tmp_ebcdic2gbk.size()];
		ofs = 0;
		for (Iterator it = tmp_ebcdic2gbk.keySet().iterator(); it.hasNext();)
		{
			Integer e = (Integer) it.next();
			Integer g = (Integer) tmp_ebcdic2gbk.get(e);
			ebcdic2gbk_e[ofs] = e.intValue();
			ebcdic2gbk_g[ofs++] = g.intValue();
		}
	}

	/**
	 * 二分查找Gbk2Ebcdic表,from Collections.iteratorBinarySearch
	 * 
	 * @param source
	 * @return
	 */
	public static int gbk2bcd_binarySearch(int ch)
	{
		if (gbk2ebcdic_g == null) return -1;
		int low = 0;
		int high = gbk2ebcdic_g.length - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			int midVal = gbk2ebcdic_g[mid];
			int cmp = midVal - ch;
			if (cmp < 0) low = mid + 1;
			else if (cmp > 0) high = mid - 1;
			else return gbk2ebcdic_e[mid]; // key found
		}
		return -1; // key not found
	}

	/**
	 * 二分查找Gbk2Ebcdic表,from Collections.iteratorBinarySearch
	 * 
	 * @param source
	 * @return
	 */
	public static int bcd2gbk_binarySearch(int ch)
	{
		if (ebcdic2gbk_e == null) return -1;
		int low = 0;
		int high = ebcdic2gbk_e.length - 1;
		while (low <= high)
		{
			int mid = (low + high) >> 1;
			int midVal = ebcdic2gbk_e[mid];
			int cmp = midVal - ch;
			if (cmp < 0) low = mid + 1;
			else if (cmp > 0) high = mid - 1;
			else return ebcdic2gbk_g[mid]; // key found
		}
		return -1; // key not found
	}

	public static byte[] gbk2bcd(String gbk)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		char[] chars = gbk.toCharArray();
		boolean inHanzi = false;
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if (!inHanzi)
			{
				if (c <= 0xFF)
				{
					int d = Ebcdic.asciiToEbcdic(c);
					baos.write(d);
					continue;
				}
				if (c > 0xFF)
				{// 第一次碰到汉字
					int d1 = gbk2bcd_binarySearch(c);
					if (d1 == -1) throw new RuntimeException("Character " + c + "is not supported");
					inHanzi = true;
					baos.write(0X0E);
					baos.write((d1 >>> 8) & 0XFF);
					baos.write((d1 & 0XFF));
					continue;
				}
			}
			if (inHanzi)
			{
				if (c > 0XFF)
				{// 汉字串内，
					int d1 = gbk2bcd_binarySearch(c);
					if (d1 == -1) throw new RuntimeException("Character " + c + "is not supported");
					baos.write((d1 >>> 8) & 0XFF);
					baos.write((d1 & 0XFF));
					continue;
				}
				if (c <= 0XFF)
				{// 碰到非汉字字符了
					baos.write(0X0F);
					inHanzi = false;
					int d = Ebcdic.asciiToEbcdic(c);
					baos.write(d);
					continue;
				}
			}
		}
		if (inHanzi) baos.write(0X0F);
		return baos.toByteArray();
	}

	public static String bcd2gbk(byte[] data)
	{
		return bcd2gbk(data, 0, data.length);
	}

	public static String bcd2gbk(byte[] data, int ofs, int len)
	{
		StringBuffer buffer = new StringBuffer();
		boolean inHanzi = false;
		int pos = ofs;
		int totalLen = ofs + len;
		while (pos < totalLen)
		{
			int b = (data[pos++] & 0XFF);
			if (!inHanzi)
			{
				if (b == 0X0E)
				{// 碰到汉字开始
					inHanzi = true;
					// add by mazhenmin
					// buffer.append(' ');
					continue;
				}
				buffer.append(Ebcdic.ebcdicToAscii((char) (b & 0XFF)));
				continue;
			}
			if (inHanzi)
			{
				if (b == 0X0F)
				{
					inHanzi = false; // modified by spc 2010-04-28 取消汉字前空格
					// buffer.append(' ');
					continue;
				}
				int b2 = (data[pos++] & 0XFF);
				int d = (b << 8) + b2;
				int c = bcd2gbk_binarySearch(d);
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	/**
	 * 计算ebcdic长度，忽略0E 0F
	 */
	public static int getEbcdByteLen(byte[] data, int ofs, int len)
	{
		int result = 0;
		for (int i = ofs; i < (ofs + len); i++)
		{
			byte b = data[i];
			if (b == 0X0E || b == 0X0F) continue;
			result++;
		}
		return result;
	}
}
