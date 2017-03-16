package spc.webos.service.common;

import java.util.List;
import java.util.Map;

import spc.webos.model.MenuPO;

public interface LoginService
{
	boolean pwd(String oldPwd, String newPwd);

	List<MenuPO> getMenu();

	void login(Map<String, String> login);

	void token(String app, String token, boolean force) throws Exception;

	boolean logout();

	boolean session();
}
