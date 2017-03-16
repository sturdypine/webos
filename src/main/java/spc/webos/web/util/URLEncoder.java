package spc.webos.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.constant.Web;
import spc.webos.util.StringX;

/**
 * 主要对Get方式的URL进行带密钥的加密
 * 
 * @author Hate
 */
public class URLEncoder
{
	protected static Logger log = LoggerFactory.getLogger(URLEncoder.class);

	public static boolean isSGet(String queryString, String sgetSalt)
	{
		if (queryString == null || queryString.length() == 0) return true;
		if (!queryString.startsWith(Web.REQ_SECURITY_GET_KEY))
		{
			log.info("queryString:{}, not startsWith:{}", queryString, Web.REQ_SECURITY_GET_KEY);
			return false;
		}
		int index = queryString.indexOf('&');
		if (index < 0) return false;
		String secretKey = queryString.substring(Web.REQ_SECURITY_GET_KEY.length() + 1, index);
		String qs = queryString.substring(index + 1);
		String secret = StringX.md5((sgetSalt + qs).getBytes());
		boolean ret = secret.equals(secretKey);
		log.debug("sget:{}, qs:{}, s:{}, t/f:{}", secretKey, qs, secret, ret);
		return ret;
	}

	/**
	 * 加密一个URL的queryString部分
	 * 
	 * @param queryString
	 * @param sgetSalt
	 * @return
	 */
	public static String sget(String queryString, String sgetSalt)
	{
		if (queryString == null || queryString.length() == 0) return queryString;
		String skey = StringX.md5((sgetSalt + queryString).getBytes());
		log.debug("sget: sget:{}, qs:{}", skey, queryString);
		StringBuilder secretKey = new StringBuilder(Web.REQ_SECURITY_GET_KEY);
		secretKey.append("=");
		secretKey.append(skey);
		secretKey.append("&");
		return secretKey.append(queryString).toString();
	}
}
