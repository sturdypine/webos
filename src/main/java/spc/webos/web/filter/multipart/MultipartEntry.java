package spc.webos.web.filter.multipart;

import java.io.File;

public class MultipartEntry
{
	public static final int TYPE_FILE = 1;
	public static final int TYPE_PARAMETER = 2;

	protected int type = -1;
	protected String parameterName = null;
	protected String parameterValue = null;
	protected String fileName = null;
	protected String tempFileName = null;
	protected File tempFile = null;

	public MultipartEntry()
	{
	}

	public MultipartEntry(int type)
	{
		this.type = type;
	}

	public MultipartEntry(int type, String fileName)
	{
		this.type = type;
		this.parameterName = fileName;
	}

	public boolean isFile()
	{
		return getType() == TYPE_FILE;
	}

	public boolean isParameter()
	{
		return getType() == TYPE_PARAMETER;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getParameterName()
	{
		return parameterName;
	}

	public void setParameterName(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public String getParameterValue()
	{
		return parameterValue;
	}

	public void setParameterValue(String parameterValue)
	{
		this.parameterValue = parameterValue;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getTempFileName()
	{
		return tempFileName;
	}

	public void setTempFileName(String tempFileName)
	{
		this.tempFileName = tempFileName;
	}

	public File getTempFile()
	{
		if (tempFile == null) tempFile = new File(getTempFileName());
		return tempFile;
	}

	public String getFileNameOnly()
	{
		String tempName = getFileName().replaceAll("\\\\", "/");
		tempName = tempName.substring(tempName.lastIndexOf("/") + 1);
		return tempName;
	}

	public void dispose()
	{
		this.parameterName = null;
		this.parameterValue = null;
		this.fileName = null;
		this.tempFileName = null;
		this.tempFile = null;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		if (isFile())
		{
			buffer.append("(file: ").append("parameter name = ").append(getParameterName()).append(
					", file name =  ").append(getFileName()).append(", temp file name = ").append(
					getTempFileName()).append(")");
			;
		}
		else if (isParameter())
		{
			buffer.append("(parameter: ").append(", parameter name = ").append(getParameterName())
					.append(", value =  ").append(getParameterValue()).append(")");
			;
		}
		else buffer.append("unknown entry type");

		return buffer.toString();
	}
}
