package spc.webos.constant;

/**
 * 系统错误采用统一编码模式实现, 错误码为12位: XX+XXXX 两位类别编号 编号说明 备注 0X 成功
 * 处理成功后的返回信息码，通用成功码为000000 1X 系统类错误 10 通信类错误 系统通信产生的错误类 11 文件类错误 12 数据库类错误 13
 * 中间件类错误 MQ,tuxedo，MB，WPS等 14通用服务类错误(加密, 转加密) 19 其他系统类错误 2X 报文类错误 20 报文格式类错误 29
 * 其他报文类错误 3X 业务类错误 30 业务授权错误 31 账户类错误 39 其他未归类业务错误 99 其他未归类错误 2011-09-27 chenjs
 * 5.2 容许返回码在不同规范场景下自定义使用
 * 
 * @author spc
 * 
 */
public class AppRetCode
{
	public static String SUCCESS = "000000"; // 通用成功

	public static String CMMN_UNDEF_EX = "999999"; // 未定义的异常
	public static String CMMN_NULLPOINT_EX = "999998"; // 空指针异常

	public static String NET_COMMON = "109999"; // 网络通用异常
	public static String PROTOCOL_SOCKET = "100001"; // socket协议的网络通用异常args=ip:port
	public static String PROTOCOL_HTTP = "100002"; // HTTP协议的网络通用异常args=url
	public static String PROTOCOL_MQ = "100003"; // MQ通讯异常
	public static String CMMN_BUF_TIMEOUT = "100004"; // 读取buf数据超时
	public static String URL_SECURIY = "100010"; // URL安全
	public static String UN_LOGIN = "100011"; // 未登陆
	public static String UN_LOGIN_IP = "100012"; // 非login ip

	public static String DB_EX = "129999"; // 数据库通用异常
	public static String DB_UNDEFINED_SQLID = "120001"; // 为定义的sql节点args=sqlid
	public static String DB_FREEMARKER = "120002"; // sql节点执行freemarker错误args=sqlid
	public static String DB_MULTI_CHANGE_DS = "120003"; // 多次切换数据源，args=m,ds1,ds2

	public static String MSG_ERRS = "200000"; // 报文整体校验不通过
	public static String ENCRYPT_ENCODE = "140001"; // 加密失败args=field
	public static String ENCRYPT_DECODE = "140002"; // 解密失败args=field
	public static String ENCRYPT_TRANSLATE = "140003"; // 转加密失败args=node1,node2，field
	public static String SIG_ENCODE = "140004"; // 签名失败args=node
	public static String SIG_DECODE = "140005"; // 验签失败args=node
	public static String APPLY_PUBKEY = "140006"; // 申请ESB公钥失败
	public static String RES_ALLOCATE_APPLY_FAIL = "140010"; // 资源池有此类型资源但申请资源失败
	public static String RES_ALLOCATE_APPLY_NO = "140011"; // 资源池中根本没有此类型资源

	public static String CMM_BIZ_ERR = "300000"; // 通用业务错误
	public static String SERVICE_UNAUTH = "300001"; // 执行未授权交易
	public static String NO_SN = "300002"; // 没有交易流水
	public static String REPEAT_SN = "300003"; // 交易重复，TCC
												// 原子交易流水号重复，此种原子服务不能发起cancel
	public static String SERVICE_NOTEXIST = "300005"; // 服务不存在
	
	public static String DB_UNAUTH = "309980"; // 没有权限访问此sql节点args=sqlid
	public static String SQL_INJECTION = "309981"; // sql注入风险，args=sqlid,name
	
	public static String FILTER_UNVALID_URI = "309997"; // 访问不合法的uri资源,可能是没登录
	public static String CMMN_UNLOGIN = "309990"; // 未登录
	public static String CMMN_PWD_ERR = "309991"; // 密码错误
	public static String UNAUTH_IP = "309992"; // 非授权IP
	public static String FREQUENT_VISITS = "309993"; // 频繁访问
	public static String VERIFY_NOT_MATCH = "309994"; // 验证码不匹配
	public static String TOKEN_FAIL_VALIDATE = "309995"; // token验证失败
	public static String TOKEN_FAIL_USER = "309996"; // token用户不存在

	// 5开头错误for tcc
	public static String TCC_XID_REPEAT = "500000"; // TCC 事物编号重复
	public static String TCC_XID_NOEXISTS = "500001"; // TCC 事物编号不存在
	public static String TCC_STATUS_CHANGE_FAIL = "500002"; // TCC 事物状态改变失败
	public static String FAIL_SEQ_NO = "500010"; // 流水号失败
	public static String DS_RULE_NULL = "500020"; // 未定义路由规则
	public static String DS_ARG_NULL = "500021"; // 动态数据源时，判断参数为空
}
