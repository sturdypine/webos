package spc.webos.service.common.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spc.webos.constant.AppRetCode;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.model.MenuPO;
import spc.webos.model.RolePO;
import spc.webos.model.UserPO;
import spc.webos.persistence.IPersistence;
import spc.webos.service.BaseService;
import spc.webos.service.common.LoginService;
import spc.webos.service.common.TokenService;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;
import spc.webos.web.common.SUI;
import spc.webos.web.common.SessionUserInfo;
import spc.webos.web.util.WebUtil;

public class LoginServiceImpl extends BaseService implements LoginService
{
	protected volatile List<MenuPO> fns;
	protected TokenService tokenService;

	public LoginServiceImpl()
	{
		versionKey = "status.refresh.common.login";
	}

	public boolean pwd(String oldPwd, String newPwd)
	{
		WebUtil.WEB.set(null); // 清空web权限控制标示，让服务用于任意指定权限
		SUI sui = SUI.SUI.get();
		if (sui == null)
		{
			log.info("session is over!!!");
			return false;
		}
		if (StringX.nullity(oldPwd) || StringX.nullity(newPwd))
		{
			log.info("oldPwd || newPwd is empty!!!");
			return false;
		}
		UserPO user = persistence.find(new UserPO(sui.getUserCode()));
		if (user == null) throw new AppException(AppRetCode.CMMN_PWD_ERR);

		oldPwd = md5(oldPwd, user.getPwdSalt());
		if (!oldPwd.equals(user.getPwd())) throw new AppException(AppRetCode.CMMN_PWD_ERR);

		user = new UserPO(sui.getUserCode());
		user.setPwdSalt(
				SpringUtil.random(config.getProperty(Config.app_login_pwd_salt_len, false, 32)));
		user.setPwd(md5(newPwd, user.getPwdSalt()));
		return persistence.update(user) > 0;
	}

	protected String md5(String pwd, String salt)
	{
		return StringX.md5((StringX.null2emptystr(salt) + pwd).getBytes());
	}

	public void refresh()
	{
		// fns = null; // 清空当前缓存
		Map<String, Object> params = new HashMap<>();
		params.put(IPersistence.SELECT_ATTACH_TAIL_KEY, "order by parentid,morder,mid");
		fns = persistence.get(new MenuPO(), params);
		log.info("sys menus:{}", fns.size());
	}

	public List<MenuPO> getMenu()
	{
		List<String> roles = SUI.SUI.get().getRoles();
		List<MenuPO> m = new ArrayList<>();
		fns.forEach((po) -> {
			for (int i = 0; i < roles.size(); i++)
			{
				if ("*".equals(roles.get(i)) || roles.get(i).startsWith(po.getMid())
						|| "00".equals(po.treeId())
						|| (roles.get(i).endsWith("*") && (po.getMid()
								.startsWith(roles.get(i).substring(0, roles.get(i).length() - 1))
								|| roles.get(i).substring(0, roles.get(i).length() - 1)
										.startsWith(po.getMid()))))
				{
					m.add(po);
					return;
				}
			}
		});
		return m;
	}

	public boolean session()
	{
		return SUI.SUI.get() != null;
	}

	public boolean logout()
	{
		SUI sui = (SUI) SUI.SUI.get();
		if (sui == null) return false;
		// session invalidate 放在filter里面完成
		return true;
	}

	public void login(Map<String, String> info)
	{
		WebUtil.WEB.set(null); // 清空web权限控制标示，让服务用于任意指定权限
		String code = info.get("code");
		String pwd = info.get("password");
		String verify = StringX.null2emptystr(info.get("verify"));
		UserPO userVO = pwd(code, pwd, verify);
		checkIP(userVO); // 验证登录IP
		SessionUserInfo sui = (SessionUserInfo) SUI.SUI.get();
		sui.setUser(userVO);
		assign();
	}

	public void token(String app, String token, boolean force) throws Exception
	{ // 使用token登录
		WebUtil.WEB.set(null); // 清空web权限控制标示，让服务用于任意指定权限
		SessionUserInfo sui = (SessionUserInfo) SUI.SUI.get();
		if (!force && sui != null && !StringX.nullity(sui.getUserCode()))
		{ // 防止token登录成功后，重复使用token访问
			log.info("token code:{} in session, token:{}", sui.getUserCode(), token);
			return;
		}
		UserPO userVO = StringX.nullity(app) ? tokenService.validate(token)
				: tokenService.validate(app, token);
		if (userVO == null)
			throw new AppException(AppRetCode.TOKEN_FAIL_VALIDATE, new Object[] { token });
		UserPO user = persistence.find(new UserPO(userVO.getCode()));
		if (user == null) throw new AppException(AppRetCode.TOKEN_FAIL_USER,
				new Object[] { token, userVO.getCode() });
		user.setRealCode(userVO.getRealCode());
		user.setName(userVO.getName());
		sui.setUser(user);
		assign();
		log.info("token login code:{}, token:{}", user.getCode(), token);
	}

	protected void assign()
	{ // 分配当前用户的权限资源 // 验密和授权分离，方便未来支持免密登录
		SessionUserInfo sui = (SessionUserInfo) SUI.SUI.get();
		if (sui == null) return;
		UserPO userVO = sui.getUser();
		if (userVO == null) return;
		sui.setRoles(getUserRole(userVO));
		// 设置当前用户能访问的服务和sql
		final List<String> services = new ArrayList<>(config
				.getProperty(Config.app_login_default_services, false, new ArrayList<String>()));
		final List<String> sqlIds = new ArrayList<>( // 登录用户需要有修改密码权限
				config.getProperty(Config.app_login_default_sqls, false, new ArrayList<String>()));
		List<MenuPO> menus = getMenu();
		menus.forEach((m) -> {
			List<String> ss = StringX
					.split2list(StringX.null2emptystr(m.getService()).toLowerCase(), StringX.COMMA);
			ss.forEach((s) -> {
				if (!services.contains(s)) services.add(s);
			});
			ss = StringX.split2list(
					StringX.null2emptystr(m.getSqlId()).replace('.', '_').toLowerCase(),
					StringX.COMMA);
			ss.forEach((s) -> {
				if (!sqlIds.contains(s)) sqlIds.add(s);
			});
		});
		sui.setServices(services);
		sui.setSqlIds(sqlIds);
		log.debug("menus:{}, services:{}, sqlIds:{}", menus.size(), services, sqlIds);
	}

	protected void checkIP(UserPO userVO)
	{
		SessionUserInfo sui = (SessionUserInfo) SUI.SUI.get();
		// 检查当前ip是否符合配置IP, 增加安全性
		String loginIP = sui.getLoginIP();
		boolean securityIP = true;
		String[] ips = StringX.nullity(userVO.getIpAddress()) ? null
				: StringX.split(userVO.getIpAddress(), ",");
		if (ips != null)
		{
			securityIP = false;
			for (String ip : ips)
				if ((ip.endsWith("*") && loginIP.startsWith(ip.substring(0, ip.length() - 1)))
						|| ip.equals(loginIP))
					securityIP = true;
		}
		if (!securityIP)
		{
			log.info("{} is unauthorized IP for {} :: {}", loginIP, userVO.getCode(),
					userVO.getIpAddress());
			throw new AppException(AppRetCode.UNAUTH_IP, new Object[] { loginIP });
		}
	}

	protected UserPO pwd(String code, String pwd, String verify)
	{
		SessionUserInfo sui = (SessionUserInfo) SUI.SUI.get();
		log.info("{} login verify:{}", code, verify);
		if (config.isProduct() && config.getProperty(Config.app_login_verify, false, true)
				&& (StringX.nullity(verify) || !verify.equalsIgnoreCase(sui.getVerifyCode())))
		{
			sui.setVerifyCode(null);
			log.info("verification code not match {} != {}", verify, sui.getVerifyCode());
			throw new AppException(AppRetCode.VERIFY_NOT_MATCH, new Object[] { verify });
		}

		UserPO userVO = persistence.find(new UserPO(code));
		String salt = userVO == null ? "" : StringX.null2emptystr(userVO.getPwdSalt());
		String password = userVO == null ? "" : md5(pwd, salt);
		if (userVO == null || !userVO.getPwd().equalsIgnoreCase(password))
		{
			log.info("login fail:{}, salt:{}, {} != {}", code, salt, password,
					userVO == null ? "" : userVO.getPwd());
			throw new AppException(AppRetCode.CMMN_PWD_ERR, new Object[] { code });
		}
		sui.setVerifyCode(null); // 清空当前验证码
		userVO.setPwd(null); // 设置密码为null
		return userVO;
	}

	protected List<String> getUserRole(UserPO userVO)
	{
		String roleId = userVO.getRoleId();
		List<String> roles = StringX.split2list(roleId, StringX.COMMA);
		List<String> menus = new ArrayList<>();
		for (int i = 0; i < roles.size(); i++)
		{
			RolePO roleVO = new RolePO();
			roleVO.setId((String) roles.get(i));
			roleVO = (RolePO) persistence.find(roleVO);
			if (roleVO == null) continue;
			List<String> menu = StringX.split2list(roleVO.getMenu(), StringX.COMMA);
			for (int j = 0; menu != null && j < menu.size(); j++)
				if (!menus.contains(menu.get(j))) menus.add(menu.get(j));
		}
		log.info("menus:{}", menus);
		return menus;
	}

	public void setTokenService(TokenService tokenService)
	{
		this.tokenService = tokenService;
	}
}
