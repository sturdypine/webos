package spc.webos.service.common;

import java.util.Map;

import spc.webos.model.UserPO;

/**
 * ÷ß≥÷√‚√‹µ«¬º
 * 
 * @author chenjs
 *
 */
public interface TokenService
{
	String generate(Map<String, String> user, int expire);

	UserPO validate(String token);

	String generate(String app, Map<String, String> user, int expire) throws Exception;

	UserPO validate(String app, String token) throws Exception;
}
