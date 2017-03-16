package spc.webos.util.netinfo;

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsNetworkInfo extends NetworkInfo
{
	public static final String IPCONFIG_COMMAND = "ipconfig /all";

	public String parseMacAddress() throws ParseException
	{
		// run command
		String ipConfigResponse = null;
		try
		{
			ipConfigResponse = runConsoleCommand(IPCONFIG_COMMAND);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		}

		// get localhost address
		String localHost = getLocalHost();

		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(
				ipConfigResponse, "\n");
		String lastMacAddress = null;
		while (tokenizer.hasMoreTokens())
		{
			String line = tokenizer.nextToken().trim();

			// see if line contains IP address, this means stop if we've already
			// seen a MAC address
			if (line.endsWith(localHost) && lastMacAddress != null) { return lastMacAddress; }

			// see if line might contain a MAC address
			int macAddressPosition = line.indexOf(":");
			if (macAddressPosition <= 0) continue;

			// trim the line and see if it matches the pattern
			String macAddressCandidate = line.substring(macAddressPosition + 1)
					.trim();
			if (isMacAddress(macAddressCandidate))
			{
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		ParseException ex = new ParseException("Cannot read MAC address from ["
				+ ipConfigResponse + "]", 0);
		ex.printStackTrace();
		throw ex;
	}

	private static boolean isMacAddress(String macAddressCandidate)
	{
		Pattern macPattern = Pattern
				.compile("[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}");
		Matcher m = macPattern.matcher(macAddressCandidate);
		return m.matches();
	}
}
