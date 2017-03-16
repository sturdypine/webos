package spc.webos.constant;

public class Web
{
	// public final static String JSRMI_OBJ_CLASS = "_JSRMI_OBJECT_CLASS_"; //
	// JSRMI的保留字

	// url地址的一些固定参数
	public static final String REQ_SECURITY_GET_KEY = "_sget"; // get请求中加密key
	public final static String REQ_KEY_UN_CLIENT_CACHE = "UN_CLIENT_CACHE"; // 不需要客户端缓存
	public final static String REQ_KEY_FORCE_FLUSH = "FORCE_FLUSH"; // 强制服务器缓存失效
	public final static String REQ_KEY_EXT_DC = "_dc"; // ext防止请求的地址重复而被浏览器缓存,
//	public final static String REQ_KEY_TOKEN = "TOKEN"; // 使用token请求时的 key
	public final static String REQ_KEY_LOGOUT = "_logout"; // 退出当前session
	// 加了一个时间参数的
	public final static String REQ_KEY_SEARCH_INDEX = "SEARCH_INDEX";
	public final static String REQ_KEY_FORM_SUCCESS_MODE = "SUCCESS_MODE";
	public final static String FORM_SUCCESS_MODE_JSON = "json";
	public final static String FORM_SUCCESS_MODE_PAGE = "page";
	public static final String REQ_KEY_VIEW_NAME_KEY = "viewName";
	public static final String REQ_KEY_VIEW_NAME_SKEY = "view";
	
	// 前端指定sql查询的行对象
	public static final String REQ_KEY_SQL_CLASS = "SQL_CLASS";

	// public final static String REQ_KEY_ERR_PAGE = "ERR_PAGE"; // 错误页面地址
	// public static final String REQ_KEY_EX_TYPE = "EX_TYPE"; // URL指定错误的返回类型.
	// value=json, htm
	public static final String REQ_KEY_EX_MSG = "EX_MSG"; // 异常显示消息
	public static final String REQ_KEY_EX_CODE = "EX_CODE"; // 异常显示消息
	public static final String REQ_KEY_EX_LOC = "EX_LOC"; // 异常位置
	public static final String REQ_KEY_EX_TRC = "EX_TRC"; // 异常跟踪信息
	// public static final String REQ_KEY_SERVICE_METHOD = "SERVICE_METHOD";
	public static final String REQ_KEY_SERVICE_DATA = "SERVICE_DATA";
	public static final String REQ_KEY_BATCH_SQL = "BATCH_SQL";
	public static final String REQ_KEY_BSQL = "bsid";
	public static final String REQ_PDFVIEW_NAME = "pdfView"; // pdf页面下载
	public static final String REQ_MSVIEW_NAME = "msView"; // microsoft 的一些模板,
	// 用于下载, word ,excel
	// ext grid服务的一些参数
	public static final String SERVICE_RET_KEY = "SVC_RET"; // 服务调用结果数据
	public static final String EXTGRID_DS_KEY = "GRID_DS"; // ext grid ds
	// public static final String EXTGRID_DS_SIZE_KEY = "GRID_SIZE"; // ext grid
	// ds
	// 总记录数
	// public static final String REQ_KEY_UTF8 = "UTF8";
	// public static final String REQ_KEY_TOTAL_SIZE_KEY_INMSG =
	// "TOTAL_SIZE_KEY";
	// public static final String REQ_KEY_RESULT_KEY_INMSG = "RESULT_KEY";
	public static final String REQ_KEY_COLUMN = "COLUMN"; // 对于服务返回的list每行的字段名
	// public static final String REQ_KEY_TOTAL_SIZE = "TOTAL_SIZE"; //
	// 用于服务方式产生的记录的分页方式的总条数
	public static final String REQ_KEY_SQL = "sid"; // 查询的sql id
	public static final String REQ_KEY_SIZE_SQL = "ssid"; // 查询方式下的总记录查询sql
	public static final String REQ_KEY_SQL_ID = "SQL_ID"; // 查询的sql id
	public static final String REQ_KEY_SIZE_SQL_ID = "SIZE_SQL_ID"; // 查询方式下的总记录查询sql
	public static final String REQ_PAGING = "PAGING"; // 服务器分页
	public static final String REQ_PAGING_DEF_LIMIT = "25";
	public static final String REQ_PAGING_ARGS_PAGE = "EXT_PAGE";
	public static final String REQ_PAGING_ARGS_START = "EXT_START";
	public static final String REQ_PAGING_ARGS_LIMIT = "EXT_LIMIT";
	public static final String REQ_EXTJS_PAGING_PAGE = "page";
	public static final String REQ_EXTJS_PAGING_START = "start";
	public static final String REQ_EXTJS_PAGING_LIMIT = "limit";
	public static final String REQ_KEY_UTF8 = "utf8"; // 转换为utf8格式\\u56\\u35

	// 附件下载
	public static final String REQ_KEY_FN = "FN"; // VO中的字段名
	public static final String REQ_KEY_VO = "VO"; // VO名
	public static final String REQ_KEY_ZIP = "ZIP"; // 是否zip处理
	// 报表下载
	public final static String REQ_KEY_VIEW_TYPE = "VIEW"; // view的类型
	// public final static String REQ_KEY_DOWNLOAD = "DOWNLOAD"; // 使用上面view代替
	public final static String REQ_KEY_DOWNLOAD_FILE_NAME = "FILE_NAME"; // 对应的配置文件的模板ID
	public final static String REQ_KEY_DOWNLOAD_SUB_FILE_NAME = "SUB_FILE_NAME"; // 如果是批量文件下载,则提供每个子文件名,顺序对于TEMPLATE_ID
	public final static String REQ_KEY_TEMPLATE_ID = "TEMPLATE_ID"; // 对应的配置文件的模板ID

	// json
	public static final String RETURN_TYPE_JSON = "json";

	public final static String POST_METHOD = "POST";
	public final static String GET_METHOD = "GET";
	public final static String RESP_ATTR_ERROR_KEY = "SERVER_ERR"; // 当请求发生错误时，放在response.setAttribute(RESP_ATTR_ERR_KEY,
	// Object);
	public final static int SERVER_EXCEPTION_STATUS_CODE = 600; // 服务器异常，返回客户端状态码，返回格式为Json
	public final static int SERVER_JSON_STATUS_CODE = 601; // 服务器返回格式为json
	public final static int SERVER_XML_STATUS_CODE = 602; // 服务器返回格式为xml
}
