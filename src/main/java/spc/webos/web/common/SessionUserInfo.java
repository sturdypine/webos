package spc.webos.web.common;

import spc.webos.model.UserPO;
import spc.webos.util.StringX;

public class SessionUserInfo extends AbstractSessionUserInfo<UserPO>
{
	private static final long serialVersionUID = 20161219L;

	public String getUserCode()
	{
		return user == null ? StringX.EMPTY_STRING : user.getCode();
	}

	public String getUserName()
	{
		return user == null ? StringX.EMPTY_STRING : user.getName();
	}
}
