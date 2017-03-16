package spc.webos.util;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.message.AppMessageSource;
import spc.webos.message.DictMessageSource;
import spc.webos.web.common.SUI;

public class FTLUtil
{
	final static Logger log = LoggerFactory.getLogger(FTLUtil.class);
	public static String JAR_FTL_PATH = "classpath*:META-INF/ftl/";
	@Autowired(required = false)
	SpringUtil springUtil = SpringUtil.getInstance();
	String[] templateLocations = new String[0]; // 模板路径
	@Autowired(required = false)
	Configuration ftlCfg; // 整个系统的ftl配置中心
	public static String numberFormat = "0.#########";

	// 800, 从DB等地方获取的ftl
	Map<String, Template> ftlCache = new ConcurrentHashMap<>();

	static FTLUtil util = new FTLUtil();

	public static FTLUtil getInstance()
	{
		return util;
	}

	public void setSpringUtil(SpringUtil springUtil)
	{
		this.springUtil = springUtil;
	}

	public boolean cacheContain(String key)
	{
		return ftlCache.containsKey(key);
	}

	public Map<String, Template> getFtlCache()
	{
		return ftlCache;
	}

	public void addFtlCache(Map<String, Template> ftls)
	{
		if (ftls == null || ftls.isEmpty()) return;
		log.info("refresh FTL id:{}", ftls.keySet());
		ftlCache.putAll(ftls);
	}

	public void addFtl(String id, Template t)
	{
		ftlCache.put(id, t);
	}

	public Configuration getFtlCfg()
	{
		return ftlCfg;
	}

	public void setFtlCfg(Configuration ftlCfg)
	{
		this.ftlCfg = ftlCfg;
	}

	public static String freemarker(String t, Map root) throws Exception
	{
		StringWriter sw = new StringWriter();
		Configuration cfg = new Configuration();
		cfg.setNumberFormat(FTLUtil.numberFormat);
		freemarker(new Template("ftlutil", new StringReader(t), cfg), root, sw);
		return sw.toString();
	}

	public static Map<String, Object> model(Map<String, Object> root)
	{
		if (root == null) root = new HashMap<>();
		// root.put(Common.MODEL_SERVICE_KEY, Status.SERVICES_PROXY);
		root.put(Common.MODEL_ROOT_KEY, root);

		root.put(Common.MODEL_STATICS_KEY, BeansWrapper.getDefaultInstance().getStaticModels());
		root.put(Common.MODEL_ENUMS_KEY, BeansWrapper.getDefaultInstance().getEnumModels());
		root.put(Common.MODEL_APP_CONF_KEY, AppConfig.getInstance().getConfig());
		root.put(Common.MODEL_APP_CFG_KEY, AppConfig.getInstance());
		root.put(Common.MODEL_SPRING_UTIL_KEY, SpringUtil.getInstance());
		if (SUI.SUI.get() != null) root.put(Common.MODEL_SUI_KEY, SUI.SUI.get());
		// root.put(Common.MODEL_FILE_UTIL_KEY, FileUtil.getInstance()); //
		// 2015-11-18 取消文件操作
		root.put(Common.MODEL_JSON_UTIL_KEY, JsonUtil.getInstance());
		// root.put(Common.MODEL_REPORT_UTIL_OLD_KEY, ReportUtil.getInstance());
		root.put(Common.MODEL_REPORT_UTIL_KEY, ReportUtil.getInstance());
		root.put(Common.MODEL_STRINGX_KEY, StringX.STRINGX);
		// 用于p/main/dict生成js字典
		root.put(Common.MODEL_DICT_KEY, DictMessageSource.getInstance().getDict());
		root.put(Common.MODEL_MS, AppMessageSource.getInstance());
		root.put(Common.MODEL_CALENDAR, Calendar.getInstance());
		return root;
	}

	public static Writer freemarker(Template t, Map root, Writer writer) throws Exception
	{
		t.process(model(root), writer);
		return writer;
	}

	public static String freemarker(Template t, Map root) throws Exception
	{
		StringWriter sw = new StringWriter();
		freemarker(t, root, sw);
		return sw.toString();
	}

	public static String ftl(String id, Map root) throws Exception
	{
		StringWriter sw = new StringWriter();
		ftl(id, root, sw);
		return sw.toString();
	}

	public static Writer ftl(String id, Map root, Writer writer) throws Exception
	{
		return freemarker(getTemplate(id), root, writer);
	}

	public static Template getTemplate(String id)
	{
		// 1. from db cache
		Template t = (Template) getInstance().ftlCache.get(id);
		// 2. from local disk
		try
		{
			if (t == null) t = getInstance().getFtlCfg().getTemplate(id + ".ftl");
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.debug("fail to load disk ftl:{}, ex:{}", id, e.toString());
		}
		return t;
	}

	// 2012-09-01 for FTL 判断一个对象是否为Map类型
	public static boolean isMap(Object o)
	{
		return o instanceof Map;
	}

	public static boolean isList(Object o)
	{
		return o instanceof List;
	}

	@PostConstruct
	public void init() throws Exception
	{
		if (ftlCfg == null)
		{
			log.info("using default ftl cfg...");
			ftlCfg = new Configuration();
			ftlCfg.setDefaultEncoding(Common.CHARSET_UTF8);
		}
		ftlCfg.setNumberFormat(numberFormat); // 2012-09-01
												// 这样对整数类型值${age}超过1000不会产生逗号
		List loaders = new ArrayList();
		try
		{ // 有些非web情况下, 不需要spring的配置信息
			loaders.add(new ClassTemplateLoader(FreeMarkerConfigurer.class, StringX.EMPTY_STRING));
		}
		catch (Throwable t)
		{
		}
		loaders.add(new ClassTemplateLoader(FTLUtil.class, "/"));

		// class path 下的模板
		for (String location : templateLocations)
		{
			try
			{
				Resource path = springUtil.getResourceLoader().getResource(location);
				File file = path.getFile();
				log.info("ftl in file: {}", file.getAbsolutePath());
				loaders.add(new FileTemplateLoader(file));
			}
			catch (Exception e)
			{
				log.warn("fail to add ftl path:{}, ex:{}", location, e.toString());
			}
		}

		for (Resource res : new PathMatchingResourcePatternResolver().getResources(JAR_FTL_PATH))
		{
			final URL url = res.getURL();
			log.info("ftl in cp: {}", url);
			loaders.add(new URLTemplateLoader()
			{
				protected URL getURL(String name)
				{
					try
					{
						URL u = new URL(url, name);
						u.openStream().close(); // 判断是否存在
						return u;
					}
					catch (Exception e)
					{
						return null;
					}
				}
			});
		}
		ftlCfg.setLocalizedLookup(false);
		ftlCfg.setTemplateLoader(new MultiTemplateLoader(
				(TemplateLoader[]) loaders.toArray(new TemplateLoader[loaders.size()])));

		ftlCfg.setObjectWrapper(new DefaultObjectWrapper());

		// 检查时候模本被更新的时间间隔, 以秒为单位 在生产环境,此时间可以配置大点比如6*60*60(S), 测试时候可以配置小点比如2(S),
		// default is 5 s
		log.info("set ftl update_delay:{}", AppConfig.isProductMode() ? "999999999" : "0");
		ftlCfg.setSetting(Configuration.TEMPLATE_UPDATE_DELAY_KEY,
				AppConfig.isProductMode() ? "999999999" : "0");

		// load ftl in db...
	}

	public void setTemplateLocations(String[] templateLocations)
	{
		this.templateLocations = templateLocations;
	}
}