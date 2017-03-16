package spc.webos.web.filter.multipart;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import spc.webos.web.filter.AbstractURLFilter;

public class MultipartFilter extends AbstractURLFilter implements ResourceLoaderAware
{
	static final int REQUEST_WRAPPING_MODE = 1;
	static final int REQUEST_ATTACHING_MODE = 2;

	ResourceLoader resourceLoader;
	String tempDirPath = "WEB-INF/data/upload";
	volatile File tempDir = null;
	// boolean debug = false;
	public static final String REQ_ATTR_ENTRIES_KEY = "_MULTIPART.ENTRIES_";

	int requestHandlingMode = REQUEST_WRAPPING_MODE;
	public static final String REQ_ATRR_REQ_WRAPPER_KEY = "_MULTIPART.REQUEST_";

	String sourceCharset = "ISO8859_1"; // 客户端上传数据字符编码集
	String targetCharset = "UTF-8"; // 服务器处理数据字符编码集

	public void setSourceCharset(String sourceCharset)
	{
		this.sourceCharset = sourceCharset;
	}

	public void setTargetCharset(String targetCharset)
	{
		this.targetCharset = targetCharset;
	}

	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	public void setRequestHandlingMode(int requestHandlingMode)
	{
		this.requestHandlingMode = requestHandlingMode;
	}

	public void setTempDirPath(String tempDirPath)
	{
		this.tempDirPath = tempDirPath;
	}

	public void filter(ServletRequest req, ServletResponse resp, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		if (!MultipartRequestHandler.isMultipartRequest(request))
		{ // 不是上传文件...
			chain.doFilter(req, resp);
			return;
		}
		if (tempDir == null) tempDir = resourceLoader.getResource(tempDirPath).getFile();
		MultipartRequestHandler handler = new MultipartRequestHandler();

		Map<String, List<MultipartEntry>> entries = null;
		try
		{
			entries = handler.handle(request, new MultipartEntryProcessor(tempDir, this), this);
			if (log.isDebugEnabled()) log.debug("upload files:{}", entries.keySet());
			request.setAttribute(REQ_ATTR_ENTRIES_KEY, entries);

			if (requestHandlingMode == REQUEST_WRAPPING_MODE)
				chain.doFilter(createRequestProxy(request, entries), resp);
			else if (requestHandlingMode == REQUEST_ATTACHING_MODE)
			{
				request.setAttribute(REQ_ATRR_REQ_WRAPPER_KEY,
						createRequestProxy(request, entries));
				chain.doFilter(request, resp);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				cleanUp(request, entries);
			}
			catch (Exception e)
			{
				log.error("cleanUp", e);
			}
		}
	}

	private void cleanUp(HttpServletRequest request, Map<String, List<MultipartEntry>> entries)
	{
		entries.forEach((k, v) -> {
			v.forEach(e -> {
				if (!e.isFile()) return;
				log.info("clean file:{}, len:{}", e.getTempFile(), e.getTempFile().length());
				e.getTempFile().delete();
			});
		});
		request.removeAttribute(REQ_ATTR_ENTRIES_KEY);
		entries.clear();
	}

	private HttpServletRequest createRequestProxy(HttpServletRequest request, Map entries)
	{
		Class[] clazzArray = (Class[]) CLASS_MAP.get(request.getClass());
		if (clazzArray == null) clazzArray = getClazz(request.getClass());
		return (HttpServletRequest) Proxy.newProxyInstance(
				HttpServletRequest.class.getClassLoader(), clazzArray,
				new HttpServletRequestProxy(request, entries));
	}

	public synchronized static Class[] getClazz(Class clazz)
	{
		Class[] clazzArray = (Class[]) CLASS_MAP.get(clazz);
		if (clazzArray == null)
		{
			clazzArray = getInterfacesForObject(clazz);
			CLASS_MAP.put(clazz, clazzArray);
		}
		return clazzArray;
	}

	public static Map CLASS_MAP = new HashMap();

	// Class[] clazzArray = getInterfacesForObject(HttpServletRequest.class);

	/**
	 * modified by spc. 2009-08-08,
	 * 由于在WAS上部署时，不能代理com.ibm.wsspi.webcontainer.servlet.IServletRequest接口
	 */
	static Class[] getInterfacesForObject(Class objectClass)
	{
		Set interfaceSet = new HashSet();
		interfaceSet.add(HttpServletRequest.class);

		// Class objectClass = object.getClass();
		while (!(Object.class.equals(objectClass)))
		{
			Class[] classInterfaces = objectClass.getInterfaces();
			for (int i = 0; i < classInterfaces.length; i++)
				// 只对javax.包里面的接口进行代理
				if (classInterfaces[i].getName().startsWith("javax"))
					interfaceSet.add(classInterfaces[i]);
			objectClass = objectClass.getSuperclass();
		}

		Class[] interfaceArray = new Class[interfaceSet.size()];
		Iterator iterator = interfaceSet.iterator();
		int i = 0;
		while (iterator.hasNext())
			interfaceArray[i++] = (Class) iterator.next();
		return interfaceArray;
	}

	public String getSourceCharset()
	{
		return sourceCharset;
	}

	public String getTargetCharset()
	{
		return targetCharset;
	}
}
