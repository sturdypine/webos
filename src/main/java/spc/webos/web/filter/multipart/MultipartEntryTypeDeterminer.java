package spc.webos.web.filter.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartEntryTypeDeterminer
{
	public static final String CONTENT_DISPOSITION = "Content-Disposition:";
	public static final String CONTENT_TYPE = "Content-Type:";
	public static final String FILE_ENTRY_STRING = ".*name=\"(.*)\";.*filename=\"(.*)\".*";
	public static final String PARAMETER_ENTRY_STRING = ".*name=\"(.*)\".*";

	private static final Pattern FILE_ENTRY_PATTERN = Pattern.compile(FILE_ENTRY_STRING,
			Pattern.DOTALL);
	private static final Pattern PARAMETER_ENTRY_PATTERN = Pattern.compile(PARAMETER_ENTRY_STRING,
			Pattern.DOTALL);

	public static MultipartEntry determineEntryType(InputStream input, byte[] lineBreak,
			MultipartFilter filter) throws Exception
	{

		MultipartEntry entry = new MultipartEntry();
		String line = readLine(input, lineBreak);
		line = readLine(input, lineBreak);
		if (line.indexOf(CONTENT_DISPOSITION) > -1)
		{
			Pattern pattern = FILE_ENTRY_PATTERN;
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches())
			{
				entry.setType(MultipartEntry.TYPE_FILE);
				entry.setParameterName(matcher.group(1));
				if (filter.getTargetCharset() != null)
					entry.setFileName(new String(matcher.group(2).getBytes(
							filter.getSourceCharset()), filter.getTargetCharset()));
				else entry.setFileName(matcher.group(2));
				line = readLine(input, lineBreak);
				line = readLine(input, lineBreak);
			}
			else
			{
				pattern = PARAMETER_ENTRY_PATTERN;
				matcher = pattern.matcher(line);
				if (matcher.matches())
				{
					entry.setType(MultipartEntry.TYPE_PARAMETER);
					entry.setParameterName(matcher.group(1));
					line = readLine(input, lineBreak);
				}
			}

		}
		return entry;
	}

	private static String readLine(InputStream input, byte[] lineBreak) throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		int data = input.read();
		int lineBreakIndex = 0;
		while (data != -1)
		{
			if (data == lineBreak[lineBreakIndex])
			{
				lineBreakIndex++;
				if (lineBreakIndex == lineBreak.length)
				{
					break;
				}
			}
			else
			{
				for (int i = 0; i < lineBreakIndex; i++)
				{
					buffer.append((char) lineBreak[i]);
				}
				lineBreakIndex = 0;
				buffer.append((char) data);
			}
			data = input.read();
		}
		return buffer.toString();
	}
}
