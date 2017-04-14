package spc.webos.constant;

public class Common
{
	// 当前版本信息
	public static final String VERSION = "9.4.0b";
	public static final String VERSION_DATE = "20170414";

	// jms. properties
	public static final String JMS_TRACE_NO = "TRACE_NO";

	// ws
	public static String SOAP_STR_ROOT_START_TAG = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://www.8f8.com/esb/\" xmlns:esb=\"http://www.8f8.com/esb/\">\n<soap:Body>\n";
	public static byte[] SOAP_ROOT_START_TAG = SOAP_STR_ROOT_START_TAG.getBytes();
	public static String SOAP_STR_ROOT_END_TAG = "\n</soap:Body>\n</soap:Envelope>";
	public static byte[] SOAP_ROOT_END_TAG = SOAP_STR_ROOT_END_TAG.getBytes();

	// 报文接入方式
	public static final String ACCEPTOR_PROTOCOL_HTTP = "HTTP";
	public static final String ACCEPTOR_PROTOCOL_UDP = "UDP";
	public static final String ACCEPTOR_PROTOCOL_TCPIP = "TCPIP";
	public static final String ACCEPTOR_PROTOCOL_TCPIP_SYN = "TCPIP-SYN"; // tcpip同步接入
	public static final String ACCEPTOR_PROTOCOL_TCPIP_ASYN = "TCPIP-ASYN"; // tcpip异步接入
	public static final String ACCEPTOR_PROTOCOL_TCPIP_XSOCKET = "TCPIP-XSOCKET"; // tcpip采用xsocket作为nio组件
	public static final String ACCEPTOR_PROTOCOL_QUEUE = "QUEUE";
	public static final String ACCEPTOR_PROTOCOL_QUEUE_MQ = "QUEUE-MQ"; // mq接入
	public static final String ACCEPTOR_PROTOCOL_QUEUE_TLQ = "QUEUE-TLQ"; // tlq接入
	public static final String ACCEPTOR_PROTOCOL_QUEUE_JMS = "QUEUE-JMS"; // jms接入

	public static final int MQ_EXPIRY_UNIT = 10; // MQ消息超时时间的单位,
	// MQ消息的超时基本单位时间为100毫秒
	public static final String WEBAPP_ROOT_PATH_KEY = "webapp.root";
	public static final String WEBAPP_ROOT_KEY = "webAppRootKey";

	public static final String YES = "1";
	public static final String NO = "0";

	public static final String APP_CD_ESB = "ESB";
	public static final String APP_CD_BPL = "BPL";
	// 2011-09-23 chenjs 未来将在代码级取消报文编号最后一位是否为0限制
	public static final String ESB_REQMSG_END = "0";

	public static final String TEMPLATE_TYPE_FTL = "ftl";
	public static final String TEMPLATE_TYPE_JSP = "jsp";

	// ftl model中固定的键值信息
	public static final String MODEL_PERSISTENCE_KEY = "persistence";
	public static final String MODEL_ROOT_KEY = "_root";
	public static final String MODEL_STATICS_KEY = "_statics";
	public static final String MODEL_ENUMS_KEY = "_enums";
	public static final String MODEL_SUI_KEY = "SUI";
	public static final String MODEL_REQUEST_KEY = "req";
	public static final String MODEL_APP_PATH_KEY = "appPath";
	public static final String MODEL_HTTP_CONTENT_TYPE = "_contentType";
	public static final String MODEL_APP_CFG_KEY = "_cfg";
	public static final String MODEL_APP_CONF_KEY = "_conf";
	public static final String MODEL_WEB_UTIL_KEY = "_webutil";
	public static final String MODEL_SPRING_UTIL_KEY = "_spring";
	// public static final String MODEL_REPORT_UTIL_OLD_KEY = "rptutil";
	public static final String MODEL_REPORT_UTIL_KEY = "_rptutil";
	public static final String MODEL_FILE_UTIL_KEY = "_fileutil";
	public static final String MODEL_JSON_UTIL_KEY = "_jsonutil";
	public static final String MODEL_CXT_KEY = "_ctx";
	public static final String MODEL_DICT_KEY = "_dict";
	public static final String MODEL_STRINGX_KEY = "_stringx";
	public static final String MODEL_EXCEPTION = "exception";
	public static final String MODEL_SERVICE_KEY = "services";
	public static final String MODEL_MS = "_ms";
	public static final String MODEL_CALENDAR = "_calendar";

	public final static String REQ_HEADER_KEY_1 = "Content-Disposition";

	public final static String CHARSET_BCD = "BCD";
	public final static String CHARSET_UTF8 = "UTF-8";
	public final static String CHARSET_GBK = "GBK";
	public final static String CHARSET_ISO = "ISO8859_1";

	public final static String SPRING_MACRO_REQ_CXT_KEY = "springMacroRequestContext";

	// response格式
	public final static String OCTET_CONTENTTYPE = "application/octet-stream";
	public final static String FILE_ZIP_CONTENTTYPE = "application/x-zip-compressed";
	public final static String FILE_WORD_CONTENTTYPE = "application/msword";
	public final static String FILE_EXCEL_CONTENTTYPE = "application/vnd.ms-excel";
	public final static String FILE_POWERPOINT_CONTENTTYPE = "application/ms-powerpoint";
	public final static String FILE_PDF_CONTENTTYPE = "application/pdf";
	public final static String FILE_HTML_CONTENTTYPE = "text/html;charset=utf-8";
	public final static String FILE_JSON_CONTENTTYPE = "application/json;charset=utf-8";
	public final static String FILE_TEXT_CONTENTTYPE = "text/plain;charset=utf-8";
	public final static String FILE_XML_CONTENTTYPE = "text/xml;charset=utf-8";
	public static final String OBJECT_TYPE_EXCEL = "xls";
	public static final String OBJECT_TYPE_EXCELXML = "xlsx";
	public static final String OBJECT_TYPE_POWERPOINT = "ppt";
	public static final String OBJECT_TYPE_POWERPOINTXML = "pptx";
	public static final String OBJECT_TYPE_WORD = "doc";
	public static final String OBJECT_TYPE_WORDXML = "docx";
	public static final String OBJECT_TYPE_PDF = "pdf";
	public static final String OBJECT_TYPE_HTM = "htm";
	public static final String OBJECT_TYPE_HTML = "html";
	public static final String OBJECT_TYPE_TEXT = "txt";
	public static final String OBJECT_TYPE_ASP = "asp";
	public static final String OBJECT_TYPE_PHP = "php";
	public static final String OBJECT_TYPE_JSP = "jsp";
	public static final String OBJECT_TYPE_JAVA = "java";
	public static final String OBJECT_TYPE_XML = "xml";

	public static final String VER()
	{
		return VERSION + '_' + VERSION_DATE;
	}

	public final static String getContentType(String suffix)
	{
		int index = suffix.lastIndexOf('.');
		if (index >= 0) suffix = suffix.substring(index + 1);
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_HTM)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_HTML))
			return FILE_HTML_CONTENTTYPE;
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_WORD)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_WORDXML))
			return Common.FILE_WORD_CONTENTTYPE;
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_EXCEL)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_EXCELXML))
			return Common.FILE_EXCEL_CONTENTTYPE;
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_POWERPOINT)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_POWERPOINTXML))
			return Common.FILE_POWERPOINT_CONTENTTYPE;
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_PDF)) return Common.FILE_PDF_CONTENTTYPE;
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_XML)) return FILE_XML_CONTENTTYPE;
		if (suffix.equalsIgnoreCase(Common.OBJECT_TYPE_TEXT)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_JAVA)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_JSP)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_ASP)
				|| suffix.equalsIgnoreCase(Common.OBJECT_TYPE_PHP))
			return FILE_TEXT_CONTENTTYPE;

		return Common.OCTET_CONTENTTYPE;
	}

	public static final String ACTION_SELECT = "SELECT";
	public static final String ACTION_DELETE = "DELETE";
	public static final String ACTION_INSERT = "INSERT";
	public static final String ACTION_UPDATE = "UPDATE";
}
