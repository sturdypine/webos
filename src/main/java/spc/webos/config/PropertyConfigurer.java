package spc.webos.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.util.StringX;

/**
 * 容许spring注入属性时使用?分隔，如果配置文件找不到值则使用?后面的值作为默认值
 * 
 * @author chenjs
 *
 */
public class PropertyConfigurer extends PropertyPlaceholderConfigurer
{
	public Properties getProperties() throws IOException
	{
		return props != null ? props : super.mergeProperties();
	}

	protected Properties props;
	protected Properties dbProps = new Properties();

	protected String resolvePlaceholder(String placeholder, Properties props)
	{
		int index = placeholder.indexOf(delim);
		String defValue = index < 0 ? null
				: placeholder.length() > index + 1 ? placeholder.substring(index + 1)
						: StringX.EMPTY_STRING;
		String[] keys = StringX.split(index < 0 ? placeholder : placeholder.substring(0, index),
				",");
		String value = null;
		for (String k : keys)
		{ // 容许设置多重key
			value = getProperty(k);
			if (value != null) return value;
		}
		// if (value == null) value = dbProps.getProperty(keys[0]);
		// if (value == null) value = defValue;
		return defValue;
	}

	protected String getProperty(String key)
	{
		String value = dbProps.getProperty(key + '.' + app + '.' + jvm);
		if (value == null) value = dbProps.getProperty(key + '.' + app);
		if (value == null) value = dbProps.getProperty(key);
		if (value == null) value = super.resolvePlaceholder(key, props);
		return value;
	}

	protected void loadProperties(Properties props) throws IOException
	{
		super.loadProperties(props);

		// 1. 首先加载位于jar里面的配置文件
		PathMatchingResourcePatternResolver prpr = new PathMatchingResourcePatternResolver();
		for (Resource res : prpr.getResources(jarResource))
		{
			log.info("loading jar properties:{}", res);
			try (InputStreamReader reader = new InputStreamReader(res.getInputStream(),
					Common.CHARSET_UTF8))
			{
				props.load(reader);
			}
		}

		// 2. 再次加载位于运行环境的配置文件
		for (Resource res : prpr.getResources(resource))
		{
			log.info("loading properties:{}", res);
			try (InputStreamReader reader = new InputStreamReader(res.getInputStream(),
					Common.CHARSET_UTF8))
			{
				props.load(reader);
			}
		}

		// 3. 最后加载某一特定jvm编号的配置文件
		String workerId = System.getProperty(Config.APP_WORKERID);
		if (!StringX.nullity(workerId))
		{
			Resource res = prpr.getResource("classpath:" + workerId + ".properties");
			log.info("loading jvm properties:{}", res);
			try (InputStreamReader reader = new InputStreamReader(res.getInputStream(),
					Common.CHARSET_UTF8))
			{
				props.load(reader);
			}
		}
		this.props = props;
		this.app = props.getProperty("app.name");
		this.jvm = props.getProperty("app.workerId");
		log.info("load jvm properties:{}, app:{}.{}", workerId, app, jvm);
		String dbconfig = props.getProperty(APP_DBCONFIG);
		String url = props.getProperty(DEFAULT_JDBC_URL);
		if (!"false".equalsIgnoreCase(dbconfig) && url != null) loadDBConfig();
		else log.info("no dbconfig: {}, url:{}", dbconfig, url);
	}

	protected String app;
	protected String jvm;

	public static String APP_DBCONFIG = "app.dbconfig";
	public static String DEFAULT_JDBC_URL = "default.jdbc.url";
	public static String DEFAULT_JDBC_USERNAME = "default.jdbc.username";
	public static String DEFAULT_JDBC_PASSWORD = "default.jdbc.password";
	public static String DEFAULT_JDBC_DRIVER = "default.jdbc.driver";
	public static String APP_SYS_CONFIG_SQL = "app.dbconfig.sql";

	// 从数据库加载配置信息
	protected void loadDBConfig()
	{
		String url = props.getProperty(DEFAULT_JDBC_URL);
		String username = props.getProperty(DEFAULT_JDBC_USERNAME);
		String password = props.getProperty(DEFAULT_JDBC_PASSWORD);
		String driver = props.getProperty(DEFAULT_JDBC_DRIVER);
		if (StringX.nullity(driver))
		{
			if (url.startsWith("jdbc:mysql")) driver = "com.mysql.jdbc.Driver";
			else if (url.startsWith("jdbc:oracle")) driver = "oracle.jdbc.driver.OracleDriver";
			else driver = "org.apache.derby.jdbc.ClientDriver";
		}
		String sql = props.getProperty(APP_SYS_CONFIG_SQL);
		if (sql == null) sql = "SELECT code,val FROM sys_config where status='1'";
		log.info("load db config url:{}, username:{}, driver:{}, sql:{}", url, username, driver,
				sql);
		try
		{
			Class.forName(driver);
			try (Connection con = DriverManager.getConnection(url, username, password);
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery(sql))
			{
				while (rs.next())
					dbProps.put(rs.getString(1), rs.getString(2));
			}
		}
		catch (Exception e)
		{
			log.info("Fail to load db config", e);
		}
		log.info("db config size:{}", dbProps.size());
		log.debug("db config:{}", dbProps.keySet());
	}

	protected String resource = "classpath*:app*.properties";
	protected String jarResource = "classpath*:META-INF/conf/app*.properties";
	protected String delim = "?";
	protected Logger log = LoggerFactory.getLogger(getClass());

	public void setDelim(String delim)
	{
		this.delim = delim;
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}

	public void setJarResource(String jarResource)
	{
		this.jarResource = jarResource;
	}
}
