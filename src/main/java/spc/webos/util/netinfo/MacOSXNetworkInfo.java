package spc.webos.util.netinfo;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.StringTokenizer;

public class MacOSXNetworkInfo extends LinuxNetworkInfo
{
	protected String parseMacAddress(String ipConfigResponse)
			throws ParseException
	{
		String localHost = null;
		try
		{
			localHost = InetAddress.getLocalHost().getHostAddress();
		}
		catch (java.net.UnknownHostException ex)
		{
			ex.printStackTrace();
			throw new ParseException(ex.getMessage(), 0);
		}
		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		while (tokenizer.hasMoreTokens())
		{
			String line = tokenizer.nextToken().trim();
//			boolean containsLocalHost = line.indexOf(localHost) >= 0;
			// see if line contains MAC address
//			if (containsLocalHost && macAddressCandidate != null) { return macAddressCandidate; }
			int macAddressPosition = line.indexOf("ether");
			if (macAddressPosition != 0) continue;
			String macAddressCandidate = line.substring(macAddressPosition + 6)
					.trim();
			if (isMacAddress(macAddressCandidate)) { return macAddressCandidate; }
		}
		ParseException ex = new ParseException("cannot read MAC address for "
				+ localHost + " from [" + ipConfigResponse + "]", 0);
		ex.printStackTrace();
		throw ex;
	}
}
