package spc.webos.util.netinfo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spc.webos.util.RegExp;

public class NetworkInfo
{
	private static final String LOCALHOST = "localhost";
	public static final String NSLOOKUP_CMD = "nslookup";

	public static void main(String[] args) throws Exception
	{
		System.out.println("MAC:" + NetworkInfo.getMacAddress());
	}

	public String parseMacAddress() throws ParseException
	{
		return null;
	}

	/**
	 * Not too sure of the ramifications here, but it just doesn't seem right
	 */
	public String parseDomain() throws ParseException
	{
		return parseDomain(LOCALHOST);
	}

	/** Universal entry for retrieving MAC Address */
	public final static String getMacAddress() throws IOException
	{
		try
		{
			NetworkInfo info = getNetworkInfo();
			String mac = info.parseMacAddress();
			return mac;
		}
		catch (ParseException ex)
		{
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
	}

	/** Universal entry for retrieving domain info */
	public final static String getNetworkDomain() throws IOException
	{
		try
		{
			NetworkInfo info = getNetworkInfo();
			String domain = info.parseDomain();
			return domain;
		}
		catch (ParseException ex)
		{
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
	}

	protected String parseDomain(String hostname) throws ParseException
	{
		// get the address of the host we are looking for - verification
		java.net.InetAddress addy = null;
		try
		{
			addy = java.net.InetAddress.getByName(hostname);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		}
		// back out to the hostname - just validating
		hostname = addy.getCanonicalHostName();
		String nslookupCommand = NSLOOKUP_CMD + " " + hostname;
		// run the lookup command
		String nslookupResponse = null;
		try
		{
			nslookupResponse = runConsoleCommand(nslookupCommand);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ParseException(e.getMessage(), 0);
		}

		StringTokenizer tokeit = new StringTokenizer(nslookupResponse, "\n", false);
		while (tokeit.hasMoreTokens())
		{
			String line = tokeit.nextToken();
			if (line.startsWith("Name:"))
			{
				line = line.substring(line.indexOf(":") + 1);
				line = line.trim();
				if (isDomain(line, hostname))
				{
					line = line.substring(hostname.length() + 1);
					return line;
				}
			}
		}

		return "n.a.";
	}

	private static boolean isDomain(String domainCandidate, String hostname)
	{
		Pattern domainPattern = Pattern.compile("[\\w-]+\\.[\\w-]+\\.[\\w-]+\\.[\\w-]+");
		Matcher m = domainPattern.matcher(domainCandidate);
		return m.matches() && domainCandidate.startsWith(hostname);
	}

	protected String runConsoleCommand(String command) throws IOException
	{
		Process p = Runtime.getRuntime().exec(command);
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		while (true)
		{
			int c = stdoutStream.read();
			if (c == -1) break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		stdoutStream.close();
		return outputText;
	}

	/** Sort of like a factory... */
	public static NetworkInfo getNetworkInfo() throws IOException
	{
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows")) return new WindowsNetworkInfo();
		else if (os.startsWith("Mac")) return new MacOSXNetworkInfo();
		else if (os.startsWith("Linux")) return new LinuxNetworkInfo();
		else throw new IOException("unknown operating system: " + os);
	}

	public final static String getLocalHost()
	{
		String ip = "127.0.0.1";
		try
		{
			ip = InetAddress.getLocalHost().getHostAddress();
			if (!ip.equals("localhost") && !ip.equals("127.0.0.1")) return ip;
			for (Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces(); interfaces.hasMoreElements();)
			{
				NetworkInterface net = interfaces.nextElement();
				if (net.isLoopback() || net.isVirtual() || !net.isUp()) continue;
				Enumeration<InetAddress> addresses = net.getInetAddresses();
				while (addresses.hasMoreElements())
				{
					String add = addresses.nextElement().getHostAddress();
					if (RegExp.match(add, RegExp.IP)) ip = add;
				}
			}
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return ip;
	}

	/** Test method */
	// public final static void main(String[] args)
	// {
	// try
	// {
	// System.out.println("Network infos");
	// System.out.println(" Operating System: "
	// + System.getProperty("os.name"));
	// System.out.println(" IP/Localhost: " + getLocalHost());
	// System.out.println(" MAC Address: " + getMacAddress());
	// System.out.println(" Domain: " + getNetworkDomain());
	// }
	// catch (Throwable t)
	// {
	// t.printStackTrace();
	// }
	// }
}