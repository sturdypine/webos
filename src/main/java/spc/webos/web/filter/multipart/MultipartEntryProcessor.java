package spc.webos.web.filter.multipart;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.service.seq.UUID;
import spc.webos.service.seq.impl.TimeMillisUUID;

public class MultipartEntryProcessor
{
	protected File tempDir = null;
	protected File tempFile;
	protected OutputStream output = null;
	protected MultipartEntry entry = null;
	protected StringBuilder parameterValue = new StringBuilder();
	MultipartFilter filter;
	UUID uuid = new TimeMillisUUID();
	protected Logger log = LoggerFactory.getLogger(getClass());

	public MultipartEntryProcessor(File tempDir, MultipartFilter filter)
	{
		this.tempDir = tempDir;
		this.filter = filter;
		if (!tempDir.exists()) tempDir.mkdirs();
	}

	public void beginEntry(MultipartEntry entry) throws Exception
	{
		this.entry = entry;
		if (entry.isFile())
		{
			tempFile = new File(
					tempDir.getAbsolutePath() + File.separator + uuid.format(uuid.uuid()));
			this.entry.setTempFileName(tempFile.getAbsolutePath());

			log.info("entry param:{}, temp: {}, upload: {}", entry.getParameterName(),
					tempFile.getName(), entry.getFileNameOnly());
			this.output = new BufferedOutputStream(new FileOutputStream(tempFile), 4 * 1024);
		}
		else if (entry.isParameter()) parameterValue.delete(0, parameterValue.length());
	}

	public void addByte(int data) throws Exception
	{
		if (entry.isFile()) this.output.write(data);
		else if (entry.isParameter()) parameterValue.append((char) data);
	}

	public void addBytes(byte[] bytes, int start, int length) throws Exception
	{
		if (entry.isFile()) output.write(bytes, start, length);
		else if (entry.isParameter()) parameterValue.append(new String(bytes, start, length));
	}

	// todo when trailining line breaks after files and parameter values are
	// removed, remove the
	// this.parameterValue.toString().trim() call.

	public void endEntry() throws Exception
	{
		if (entry.isFile())
		{
			output.close();
			log.info("end entry:{}, len:{}", tempFile.getName(), tempFile.length());
		}
		else if (entry.isParameter())
		{
			if (filter.getTargetCharset() != null) entry.setParameterValue(
					new String(parameterValue.toString().trim().getBytes(filter.getSourceCharset()),
							filter.getTargetCharset()));
			else entry.setParameterValue(parameterValue.toString().trim());
		}
		entry = null;
	}

	public void dispose() throws Exception
	{
		try
		{
			super.finalize();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		tempDir = null;
		output = null;
		entry = null;
		parameterValue = null;
	}
}
