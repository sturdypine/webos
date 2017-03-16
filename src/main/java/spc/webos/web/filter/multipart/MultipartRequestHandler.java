package spc.webos.web.filter.multipart;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class MultipartRequestHandler
{
	// it seems the HTTP line breaks are windows line breaks. also when files
	// are uploaded from a Linux box.
	// the Linux line break code is kept for a little while though, until I am
	// 100% sure.
	public static final byte[] LINE_BREAK_WIN = new byte[] { 0x0d, 0x0a };
	public static final byte[] LINE_BREAK_UNIX = new byte[] { 0x0a };
	public static final byte[] BOUNDARY_PREFIX = new byte[] { 0x2d, 0x2d };

	int boundaryIndex = 0;
	int boundaryOffset = LINE_BREAK_WIN.length;
	byte[] lineBreak = LINE_BREAK_WIN;

	public Map<String, List<MultipartEntry>> handle(HttpServletRequest request, MultipartEntryProcessor processor,
			MultipartFilter filter) throws Exception
	{
		String boundary = request.getContentType()
				.substring(request.getContentType().indexOf("boundary=") + "boundary=".length());

		// if(request.getHeader("User-Agent").indexOf("Linux") > -1){
		// System.out.println("Unix line breaks");
		// boundaryOffset = LINE_BREAK_UNIX.length;
		// lineBreak = LINE_BREAK_UNIX;
		// }

		byte[] boundaryBytes = createBoundaryBytes(boundary);
		Map<String, List<MultipartEntry>> entries = new HashMap<>();

		MultipartEntry entry = null;
		try (InputStream input = new BufferedInputStream(request.getInputStream(), 4 * 1024))
		{
			int data = input.read();
			while (data != -1)
			{
				if (((byte) data) == boundaryBytes[boundaryIndex + boundaryOffset])
				{
					boundaryIndex++;
					if (isBoundary(boundaryIndex, boundaryOffset, boundaryBytes))
						entry = handleBoundary(entry, processor, input, entries, filter);
					data = input.read();
				}
				else
				{
					if (boundaryIndex > 0)
					{
						if (entry != null) processor.addBytes(boundaryBytes, 0, boundaryIndex);
						boundaryIndex = 0;
					}
					else
					{
						if (entry != null) processor.addByte(data);
						data = input.read();
					}
				}
			}
		}
		return entries;
	}

	private byte[] createBoundaryBytes(String boundary)
	{
		byte[] boundaryBytes = boundary.getBytes();
		byte[] boundaryTotal = new byte[lineBreak.length + BOUNDARY_PREFIX.length
				+ boundaryBytes.length];

		int index = 0;

		for (int i = 0; i < lineBreak.length; i++)
			boundaryTotal[index++] = lineBreak[i];
		for (int i = 0; i < BOUNDARY_PREFIX.length; i++)
			boundaryTotal[index++] = BOUNDARY_PREFIX[i];
		for (int i = 0; i < boundaryBytes.length; i++)
			boundaryTotal[index++] = boundaryBytes[i];
		return boundaryTotal;
	}

	private MultipartEntry handleBoundary(MultipartEntry entry, MultipartEntryProcessor processor,
			InputStream input, Map<String, List<MultipartEntry>> entries, MultipartFilter filter) throws Exception
	{
		boundaryIndex = 0;
		boundaryOffset = 0; // every boundary after the first has a \n in front
		// of it.
		if (entry != null) processor.endEntry();
		entry = MultipartEntryTypeDeterminer.determineEntryType(input, lineBreak, filter);
		addEntryToMap(entries, entry);
		processor.beginEntry(entry);
		return entry;
	}

	private boolean isBoundary(int boundaryIndex, int boundaryOffset, byte[] boundaryBytes)
	{
		return boundaryIndex + boundaryOffset == boundaryBytes.length;
	}

	private void addEntryToMap(Map<String, List<MultipartEntry>> entries, MultipartEntry entry)
	{
		if (entry.getParameterName() == null) return;
		List<MultipartEntry> list = entries.get(entry.getParameterName());
		if (list == null) entries.put(entry.getParameterName(), list = new ArrayList<>());
		list.add(entry);
		
//		if (entries.get(entry.getParameterName()) != null)
//		{ // ´æÔÚ
//			Object firstEntryObj = entries.get(entry.getParameterName());
//			if (firstEntryObj instanceof MultipartEntry)
//			{
//				List<MultipartEntry> entriesWithSameParameterName = new ArrayList<>();
//				MultipartEntry firstEntry = (MultipartEntry) firstEntryObj;
//				entriesWithSameParameterName.add(firstEntry);
//				entriesWithSameParameterName.add(entry);
//				entries.put(entry.getParameterName(), entriesWithSameParameterName);
//			}
//			else if (firstEntryObj instanceof List)
//			{
//				List entriesWithSameParameterName = (List) firstEntryObj;
//				entriesWithSameParameterName.add(entry);
//			}
//		}
//		else
//		{
//			 entries.put(entry.getParameterName(), entry);
//		}
	}

	public static boolean isMultipartRequest(ServletRequest request)
	{
		return request.getContentType() != null
				&& (request.getContentType().indexOf("multipart/form-data") > -1
						|| request.getContentType().indexOf("multipart/mixed") > -1);
	}
}
