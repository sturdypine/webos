package spc.webos.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;

public class CipherUtil
{
	static final String ALG_DES = "DES";

	/**
	 * 对称加密方法
	 * 
	 * @param byteSource
	 *            需要加密的数据
	 * @return 经过加密的数据
	 * @throws Exception
	 */
	public static byte[] desEncrypt(byte[] byteSource, byte[] key)
	{
		return desEncrypt(byteSource, 0, byteSource.length, key);
	}

	public static byte[] desEncrypt(String source, String key)
	{
		byte[] byteSource = StringX.decodeBase64(source);
		return desEncrypt(byteSource, 0, byteSource.length, StringX.decodeBase64(key));
	}

	public static byte[] desEncrypt(byte[] byteSource, int offset, int len, byte[] key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(ALG_DES);
			cipher.init(Cipher.ENCRYPT_MODE,
					SecretKeyFactory.getInstance(ALG_DES).generateSecret(new DESKeySpec(key)));
			return cipher.doFinal(byteSource, offset, len);
		}
		catch (Exception e)
		{
			throw new AppException(AppRetCode.ENCRYPT_ENCODE, e);
		}
	}

	public static byte[] desDecrypt(String source, String key)
	{
		byte[] byteSource = StringX.decodeBase64(source);
		return desDecrypt(byteSource, 0, byteSource.length, StringX.decodeBase64(key));
	}

	public static byte[] desDecrypt(byte[] byteSource, byte[] key)
	{
		return desDecrypt(byteSource, 0, byteSource.length, key);
	}

	/**
	 * 对称解密方法
	 * 
	 * @param byteSource
	 *            需要解密的数据
	 * @return 经过解密的数据
	 * @throws Exception
	 */
	public static byte[] desDecrypt(byte[] byteSource, int offset, int len, byte[] key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(ALG_DES);
			cipher.init(Cipher.DECRYPT_MODE,
					SecretKeyFactory.getInstance(ALG_DES).generateSecret(new DESKeySpec(key)));
			return cipher.doFinal(byteSource, offset, len);
		}
		catch (Exception e)
		{
			throw new AppException(AppRetCode.ENCRYPT_DECODE, e);
		}
	}
}
