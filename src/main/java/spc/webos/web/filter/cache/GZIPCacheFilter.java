package spc.webos.web.filter.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import spc.webos.constant.Web;
import spc.webos.util.StringX;
import spc.webos.web.filter.AbstractURLFilter;

/**
 * 服务器端缓存请求的URL内容, 下一次访问的时候不需要执行请求操作, 此功能谨慎使用
 * 
 * @author Hate
 */
public class GZIPCacheFilter extends AbstractURLFilter implements ResourceLoaderAware
{
	long defaultCacheTimeout = 12 * 60; // 默认为12小时
	ResourceLoader resourceLoader; // 缓存文件的资源加载器
	String tempFileDir = "WEB-INF/env/cachedir"; // 存放生成的临时文件目录
	String fileName = "MAIN"; // 当queryString为空字符串的时候采用的文件名
	String forceFlushParam = "_FORCE_FLUSH_=";
	// long defaultValidFileLength = 0; // 默认为1024大小的内容才给予缓存, 防止生成错误的页面后长时间不被更新

	// public void setDefaultValidFileLength(long defaultValidFileLength)
	// {
	// this.defaultValidFileLength = defaultValidFileLength;
	// }

	public void setTempFileDir(String tempFileDir)
	{
		this.tempFileDir = tempFileDir;
	}

	public void setDefaultCacheTimeout(long defaultCacheTimeout)
	{
		this.defaultCacheTimeout = defaultCacheTimeout;
	}

	public void setResourceLoader(ResourceLoader resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

	public void filter(ServletRequest req, ServletResponse res, FilterChain chain,
			String patternURL) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		if (!Web.GET_METHOD.equalsIgnoreCase(request.getMethod()))
		{ // 只对GET请求给于服务器端缓存支持
			chain.doFilter(req, res);
			return;
		}
		// System.out.println("clazz: " +
		// response.getOutputStream().getClass());
		Map paramMap = queryStringToMap(patternURL);
		String strCacheTimeout = (String) paramMap.get("timeoutMinute");
		long cacheTimeout = defaultCacheTimeout;
		if (strCacheTimeout != null)
			cacheTimeout = 1000 * 60 * Long.parseLong(strCacheTimeout.trim()); // 传入参数单位是分钟

		// String strValidFileLength = (String) paramMap.get("validFileLength");
		// long validFileLength = defaultValidFileLength;
		// if (strValidFileLength != null) validFileLength = Long
		// .parseLong(strValidFileLength);

		// customize to match parameters
		String fileName = this.fileName;
		String querySring = request.getQueryString();
		String query = restoreURL(restoreURL(querySring, Web.REQ_KEY_EXT_DC), forceFlushParam); // 如果有查询条件，则用查询条件作为文件名
		if (query != null && query.length() > 0)
		{
			// 是否压缩QueryString作为缓存的文件名
			if ("true".equalsIgnoreCase((String) paramMap.get("compressQueryString")))
				fileName = StringX.md5(query.getBytes());
			else fileName = query;
		}
		String childPath = getUri(request);
		// get possible cache
		File targetDir = new File(resourceLoader.getResource(tempFileDir).getFile(), childPath);
		if (!targetDir.exists()) targetDir.mkdirs();
		File targetFile = new File(targetDir, fileName);
		if ((querySring != null && querySring.indexOf(forceFlushParam) >= 0) || !targetFile.exists()
				|| cacheTimeout < Calendar.getInstance().getTimeInMillis()
						- targetFile.lastModified()
				|| targetFile.length() < 3)
		{ // 当前文件不存在 or 时间过期 or 强制重新生成 调用Servlet chain生成内容到指定的文件
			OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile));
			CacheResponseWrapper wrappedResponse = new CacheResponseWrapper(response, os);
			try
			{
				chain.doFilter(req, wrappedResponse);
			}
			catch (ServletException e)
			{
				targetFile.delete();
				throw e;
			}
			finally
			{
				try
				{
					os.close();
				}
				catch (Exception e)
				{
				}
			}
		}

		/*
		 * // 从文件系统中获得生成好的内容 InputStream is = new BufferedInputStream( new
		 * FileInputStream(targetFile));
		 * response.setContentType(request.getContentType());
		 * ServletOutputStream sos = res.getOutputStream(); try { if
		 * (targetFile.length() > 3) { int i = is.read(); sos.write((byte) i);
		 * for (i = is.read(); i != -1; i = is.read()) sos.write((byte) i); } }
		 * finally { try { is.close(); } catch (Exception e) { } }
		 */
		// 当前请求发生错误则删除生成的缓存文件
		Boolean err = (Boolean) request.getAttribute(Web.RESP_ATTR_ERROR_KEY);
		if (err != null && err.booleanValue()) targetFile.delete();
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getForceFlushParam()
	{
		return forceFlushParam;
	}

	public void setForceFlushParam(String forceFlushParam)
	{
		this.forceFlushParam = forceFlushParam;
	}
}
