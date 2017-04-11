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
			// 支持配置参数里面引用当前jvm代号，940
			if (value != null) return StringX.replace(value, "${jvm}", jvm);
		}
		// if (value == null) value = dbProps.getProperty(keys[0]);
		// if (value == null) value = defValue;
		return defValue;
	}

	protected String getProperty(String key)
	{
		String value = dbProps.getProperty(key + '.' + app + '.' + jvm);
		if (value == null) value = dbProps.getProperty(key + '.' + app);
		if (value == null)
		{
			value = System.getProperty(key); // 940 从系统配置信息获取
			if (StringX.nullity(value)) value = null; // -Dname配置不能为空字符串
		}
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
			catch (Exception e)
			{ // 940， 支持在docker环境下启动一个独立jvm进程无需一定要读取jvm.properties
				log.warn("fail to load: classpath:" + workerId + ".properties, " + e);
			}
		}
		this.props = props;
		this.app = props.getProperty("app.name");
		this.jvm = props.getProperty("app.workerId");
		// 940_20170329,
		// 如果jvm.properties里面没有提供app.workerId配置，则使用jvm启动命令的-Dapp.workerId=${jvm}
		if (StringX.nullity(jvm))
		{
			this.jvm = workerId;
			props.setProperty("app.workerId", workerId);
		}
		String dbconfig = props.getProperty(APP_DBCONFIG);
		log.info("load: {}.properties, app:{}.{}, dbconfig:{}", workerId, app, jvm, dbconfig);
		if (!"false".equalsIgnoreCase(dbconfig)) loadDBConfig();
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
		String url = System.getProperty(DEFAULT_JDBC_URL);
		if (StringX.nullity(url)) url = props.getProperty(DEFAULT_JDBC_URL);
		if (StringX.nullity(url))
		{
			log.warn("no db url:{}", url);
			return;
		}
		String username = System.getProperty(DEFAULT_JDBC_USERNAME);
		if (StringX.nullity(username)) username = props.getProperty(DEFAULT_JDBC_USERNAME);
		String password = System.getProperty(DEFAULT_JDBC_PASSWORD);
		if (StringX.nullity(password)) password = props.getProperty(DEFAULT_JDBC_PASSWORD);
		String driver = System.getProperty(DEFAULT_JDBC_DRIVER);
		if (StringX.nullity(driver)) driver = props.getProperty(DEFAULT_JDBC_DRIVER);

		if (StringX.nullity(driver))
		{
			if (url.startsWith("jdbc:mysql")) driver = "com.mysql.jdbc.Driver";
			else if (url.startsWith("jdbc:oracle")) driver = "oracle.jdbc.driver.OracleDriver";
			else if (url.startsWith("jdbc:db2")) driver = "com.ibm.db2.jcc.DB2Driver";
			else if (url.startsWith("jdbc:sqlserver"))
				driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			else if (url.startsWith(" jdbc:postgresql")) driver = "org.postgresql.Driver";
			else if (url.startsWith("jdbc:derby")) driver = "org.apache.derby.jdbc.ClientDriver";
		}
		String sql = props.getProperty(APP_SYS_CONFIG_SQL);
		if (sql == null) sql = "SELECT code,val FROM sys_config where status='1'";
		log.info("load db config url:{}\nusername:{}\ndriver:{}\nsql:{}", url, username, driver,
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
			log.info("db config items:{}", dbProps.size());
			log.debug("db config:{}", dbProps.keySet());
		}
		catch (Exception e)
		{
			log.warn("Fail to load db config", e);
		}
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
