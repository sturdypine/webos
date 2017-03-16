package spc.webos.persistence.jdbc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.persistence.PO;
import spc.webos.util.FileUtil;
import spc.webos.util.StringX;
import spc.webos.util.SpringUtil;

public class JdbcUtil
{
	public static String JDBC_TEMP_DIR = "/dbtempdir"; // 900 
	static File tempdir;
	static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

	public static File getTempFile(PO vo, String fileName, InputStream is)
	{
		File target = null;
		try
		{
			File parent = new File(getTempdir(), vo.getClass().getSimpleName()); // 900_20160115
																					// vo.table()
			if (!parent.exists()) parent.mkdirs();
			target = new File(parent, fileName);
			if (target != null && target.exists()) target.delete();
			if (is != null && target != null) FileUtil.is2os(is,
					new BufferedOutputStream(new FileOutputStream(target)), true, true);
		}
		catch (Exception e)
		{
			log.error("JdbcUtil.getTempFile: fileName:" + fileName + ", vo:" + vo.toString(), e);
		}

		return target;
	}

	public static File getTempFileByVO(PO vo, String fieldName)
	{
		try
		{
			String fileName = genFileNameByVO(vo, fieldName);
			File parent = new File(getTempdir(), vo.getClass().getSimpleName()); // 900_20160115
																					// vo.table()
			if (!parent.exists()) parent.mkdirs();
			File target = new File(parent, fileName);
			return target.exists() ? target : null;
		}
		catch (Exception e)
		{
			log.error("getTempFileByVO: fieldName:" + fieldName + ", vo:" + vo.toString(), e);
		}
		return null;
	}

	public static String genFileNameByVO(Object vo, String fieldName) throws Exception
	{
		String primary = null;
		String fileType = null;
		if (vo instanceof PO)
			primary = vo.getClass().getName() + "|" + fieldName + "|" + primary(vo); // ((PO)
																						// vo).primary("-");
																						// //
																						// 900_20160115
		else primary = String.valueOf(Math.random());
		try
		{
			StringBuffer name = new StringBuffer(StringX.md5(primary.getBytes()));
			if (fileType != null && fileType.length() > 0)
			{
				name.append('.');
				name.append(fileType);
			}
			return name.toString();
		}
		catch (Exception e)
		{
			log.error("genFileNameByVO: fieldName:" + fieldName + ", vo:" + vo.toString(), e);
		}
		return null;
	}

	// 900_20160120
	protected static String primary(Object vo) throws Exception
	{
		try
		{
			Method m = vo.getClass().getMethod("getPrimary", new Class[] { String.class });
			return (String) m.invoke(vo, "-");
		}
		catch (Exception e)
		{
		}
		Method m = vo.getClass().getMethod("primary", null);
		return (String) m.invoke(vo, null);
	}

	public static File getTempdir() throws IOException
	{
		if (tempdir == null) tempdir = SpringUtil.getInstance().getResourceLoader()
				.getResource(JDBC_TEMP_DIR).getFile();
		return tempdir;
	}
}
