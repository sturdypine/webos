package spc.webos.util.netinfo;

import java.io.IOException;
import java.text.ParseException;

import spc.webos.util.StringX;

public class LinuxNetworkInfo extends NetworkInfo
{
	public static final String IPCONFIG_COMMAND = "ifconfig";

	public String parseMacAddress() throws ParseException
	{
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
		return parseMacAddress(ipConfigResponse);
	}

	protected String parseMacAddress(String ipConfigResponse)
			throws ParseException
	{
		String localHost = null;
		try
		{
			localHost = java.net.InetAddress.getLocalHost().getHostAddress();
		}
		catch (java.net.UnknownHostException ex)
		{
			ex.printStackTrace();
			throw new ParseException(ex.getMessage(), 0);
		}
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(
				ipConfigResponse, "\n");
		String lastMacAddress = null;

		while (tokenizer.hasMoreTokens())
		{
			String line = tokenizer.nextToken().trim();
			boolean containsLocalHost = line.indexOf(localHost) >= 0;

			// see if line contains IP address
			if (containsLocalHost && lastMacAddress != null) { return lastMacAddress; }

			// see if line contains MAC address
			int macAddressPosition = line.indexOf("HWaddr");
			if (macAddressPosition <= 0) continue;

			String macAddressCandidate = line.substring(macAddressPosition + 6)
					.trim();
			if (isMacAddress(macAddressCandidate))
			{
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}
		return StringX.EMPTY_STRING;
	}

	public String parseDomain(String hostname) throws ParseException
	{
		return StringX.EMPTY_STRING;
	}

	protected static boolean isMacAddress(String macAddressCandidate)
	{
		if (macAddressCandidate.length() != 17) return false;
		return true;
	}
}
