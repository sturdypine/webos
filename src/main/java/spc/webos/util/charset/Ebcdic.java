package spc.webos.util.charset;

public final class Ebcdic
{
	static final char ASCII2EBCDIC[] = { '\0', '\001', '\002', '\003', '7', '-', '.', '/', '\026',
			'\005', '\025', '\013', '\f', '\r', '\016', '\017', '\020', '\021', '\022', '\023',
			'<', '=', '2', '&', '\030', '\031', '?', '\'', '"', '\035', '5', '\037', '@', 'Z',
			'\177', '{', '[', 'l', 'P', '}', 'M', ']', '\\', 'N', 'k', '`', 'K', 'a', '\360',
			'\361', '\362', '\363', '\364', '\365', '\366', '\367', '\370', '\371', 'z', '^', 'L',
			'~', 'n', 'o', '|', '\301', '\302', '\303', '\304', '\305', '\306', '\307', '\310',
			'\311', '\321', '\322', '\323', '\324', '\325', '\326', '\327', '\330', '\331', '\342',
			'\343', '\344', '\345', '\346', '\347', '\350', '\351', '\255', '\340', '\275', '_',
			'm', 'y', '\201', '\202', '\203', '\204', '\205', '\206', '\207', '\210', '\211',
			'\221', '\222', '\223', '\224', '\225', '\226', '\227', '\230', '\231', '\242', '\243',
			'\244', '\245', '\246', '\247', '\250', '\251', '\300', 'O', '\320', '\241', '\007',
			'C', ' ', '!', '\034', '#', '\353', '$', '\233', 'q', '(', '8', 'I', '\220', '\272',
			'\354', '\337', 'E', ')', '*', '\235', 'r', '+', '\212', '\232', 'g', 'V', 'd', 'J',
			'S', 'h', 'Y', 'F', '\352', '\332', ',', '\336', '\213', 'U', 'A', '\376', 'X', 'Q',
			'R', 'H', 'i', '\333', '\216', '\215', 's', 't', 'u', '\372', '\025', '\260', '\261',
			'\263', '\264', '\265', 'j', '\267', '\270', '\271', '\314', '\274', '\253', '>', ';',
			'\n', '\277', '\217', ':', '\024', '\240', '\027', '\313', '\312', '\032', '\033',
			'\234', '\004', '4', '\357', '\036', '\006', '\b', '\t', 'w', 'p', '\276', '\273',
			'\254', 'T', 'c', 'e', 'f', 'b', '0', 'B', 'G', 'W', '\356', '3', '\266', '\341',
			'\315', '\355', '6', 'D', '\316', '\317', '1', '\252', '\374', '\236', '\256', '\214',
			'\335', '\334', '9', '\373', '\200', '\257', '\375', 'x', 'v', '\262', '\237', '\377' };
	static final char EBCDIC2ASCII[] = { '\0', '\001', '\002', '\003', '\317', '\t', '\323',
			'\177', '\324', '\325', '\303', '\013', '\f', '\r', '\016', '\017', '\020', '\021',
			'\022', '\023', '\307', '\n', '\b', '\311', '\030', '\031', '\314', '\315', '\203',
			'\035', '\322', '\037', '\201', '\202', '\034', '\204', '\206', '\n', '\027', '\033',
			'\211', '\221', '\222', '\225', '\242', '\005', '\006', '\007', '\340', '\356', '\026',
			'\345', '\320', '\036', '\352', '\004', '\212', '\366', '\306', '\302', '\024', '\025',
			'\301', '\032', ' ', '\246', '\341', '\200', '\353', '\220', '\237', '\342', '\253',
			'\213', '\233', '.', '<', '(', '+', '|', '&', '\251', '\252', '\234', '\333', '\245',
			'\231', '\343', '\250', '\236', '!', '$', '*', ')', ';', '^', '-', '/', '\337', '\334',
			'\232', '\335', '\336', '\230', '\235', '\254', '\272', ',', '%', '_', '>', '?',
			'\327', '\210', '\224', '\260', '\261', '\262', '\374', '\326', '\373', '`', ':', '#',
			'@', '\'', '=', '"', '\370', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '\226',
			'\244', '\363', '\257', '\256', '\305', '\214', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', '\227', '\207', '\316', '\223', '\361', '\376', '\310', '~', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z', '\357', '\300', '\332', '[', '\362', '\371', '\265', '\266',
			'\375', '\267', '\270', '\271', '\346', '\273', '\274', '\275', '\215', '\331', '\277',
			']', '\330', '\304', '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', '\313', '\312',
			'\276', '\350', '\354', '\355', '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'\241', '\255', '\365', '\364', '\243', '\217', '\\', '\347', 'S', 'T', 'U', 'V', 'W',
			'X', 'Y', 'Z', '\240', '\205', '\216', '\351', '\344', '\321', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '\263', '\367', '\360', '\372', '\247', '\377' };

	private Ebcdic()
	{
	}

	public static final byte[] asciiBytes2EbcdicBytes(byte abyte0[])
	{
		byte abyte1[] = new byte[abyte0.length];
		for (int i = 0; i < abyte0.length; i++)
			abyte1[i] = (byte) asciiToEbcdic((char) (abyte0[i] & 0xff));
		return abyte1;
	}

	public static final byte asciiToEbcdic(byte byte0)
	{
		return (byte) asciiToEbcdic((char) (byte0 & 0xff));
	}

	public static final char asciiToEbcdic(char c)
	{
		return ASCII2EBCDIC[c];
	}

	public static final String dumpEbcdicBytes(byte abyte0[], int i, int j)
	{
		StringBuffer buf = new StringBuffer();
		for (int k = i; k < j; k++)
			buf.append(k).append(Integer.toHexString((char) (abyte0[k] & 0xff))).append(' ');
		return buf.toString();
	}

	public static final byte[] ebcdicBytes2AsciiBytes(byte abyte0[])
	{
		byte abyte1[] = new byte[abyte0.length];
		for (int i = 0; i < abyte0.length; i++)
			abyte1[i] = (byte) ebcdicToAscii((char) (abyte0[i] & 0xff));
		return abyte1;
	}

	public static final byte[] ebcdicBytes2AsciiBytes(byte abyte0[], int i, int j)
	{
		byte abyte1[] = new byte[j];
		for (int k = 0; k < j; k++)
			abyte1[k] = (byte) ebcdicToAscii((char) (abyte0[k + i] & 0xff));
		return abyte1;
	}

	public static final void ebcdicBytes2AsciiBytesInSitu(byte abyte0[], int i, int j)
	{
		for (int k = 0; k < j; k++)
			abyte0[k + i] = (byte) ebcdicToAscii((char) (abyte0[k + i] & 0xff));
	}

	public static final char[] ebcdicBytes2AsciiChars(byte abyte0[])
	{
		return ebcdicBytes2AsciiChars(abyte0, 0, abyte0.length);
	}

	public static final char[] ebcdicBytes2AsciiChars(byte abyte0[], int i, int j)
	{
		char ac[] = new char[j];
		for (int k = 0; k < j; k++)
			ac[k] = ebcdicToAscii((char) (abyte0[k + i] & 0xff));
		return ac;
	}

	public static final byte ebcdicToAscii(byte byte0)
	{
		return (byte) EBCDIC2ASCII[byte0];
	}

	public static final char ebcdicToAscii(char c)
	{
		return EBCDIC2ASCII[c];
	}

	public static final void string2EbcdicBytes(String s, byte abyte0[])
	{
		for (int i = 0; i < s.length(); i++)
			abyte0[i] = (byte) asciiToEbcdic(s.charAt(i));
	}
}
