package spc.webos.service.common.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.model.UserPO;
import spc.webos.service.BaseService;
import spc.webos.service.common.TokenService;
import spc.webos.util.CipherUtil;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.web.util.WebUtil;

public class TokenServiceImpl extends BaseService implements TokenService
{
	@Value("${app.login.token.prefix?token:}")
	protected String prefix = "token:";

	@Override
	public String generate(Map<String, String> user, int expire)
	{
		String token = WebUtil.generateSessionId(64);
		jedis.execute((redis) -> {
			redis.setex(prefix + token, expire, JsonUtil.obj2json(user));
		});
		return token;
	}

	@Override
	public UserPO validate(String token)
	{
		StringBuilder info = new StringBuilder();
		jedis.execute((redis) -> {
			String key = prefix + token;
			String v = redis.get(key);
			if (!StringX.nullity(v))
			{
				info.append(v);
				redis.del(key); // token
			}
		});
		UserPO user = JsonUtil.json2obj(JsonUtil.obj2json(info.toString()), UserPO.class);
		log.info("token code:{}, realCode:{}", user.getCode(), user.getRealCode());
		return user;
	}

	@Override
	public String generate(String app, Map<String, String> user, int expire) throws Exception
	{
		Map<String, Object> token = new HashMap<>();
		token.put("expire", String.valueOf(System.currentTimeMillis() + expire * 1000));
		token.put("user", user);
		byte[] buf = CipherUtil.desEncrypt(JsonUtil.obj2json(token).getBytes(Common.CHARSET_UTF8),
				config.getProperty(Config.app_web_session_token_des + '.' + app, false, "spc-webos")
						.getBytes());
		return StringX.encodeBase64Url(buf);
	}

	/**
	 * {app:'', token:{expire:'14000000',
	 * user:{code:'public',realCode:'chenjs',name:'³Â¾¢ËÉ'}}}
	 */
	@Override
	public UserPO validate(String app, String token) throws Exception
	{
		byte[] buf = StringX.decodeBase64Url(token);
		Map<String, Object> t = (Map<String, Object>) JsonUtil
				.json2obj(
						new String(
								CipherUtil
										.desDecrypt(buf,
												config.getProperty(Config.app_web_session_token_des
														+ '.' + app, false, "spc-webos")
														.getBytes()),
								Common.CHARSET_UTF8));
		long expire = Long.parseLong(StringX.null2emptystr(t.get("expire"), "0"));
		if (System.currentTimeMillis() > expire)
		{
			log.info("token expired:{}", expire);
			return null;
		}
		UserPO user = JsonUtil.json2obj(JsonUtil.obj2json(t.get("user")), UserPO.class);
		log.info("token app:{}, code:{}, realCode:{}, expire:{}", app, user.getCode(),
				user.getRealCode(), expire);
		return user;
	}
}
