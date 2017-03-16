package spc.webos.endpoint;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.util.NumberX;
import spc.webos.util.StringX;
import spc.webos.util.charset.EBCDUtil;

public class UDPEndpoint implements Endpoint
{
	protected String location;
	protected String ip;
	protected int port;
	protected int len;
	protected boolean len2bcd; // 发送的长度字段是否需要BCD转码
	protected boolean hdrLenBinary;
	protected final Logger log = LoggerFactory.getLogger(getClass());
	static String TRUE = "true";

	public UDPEndpoint()
	{
	}

	public UDPEndpoint(String location)
	{
		createEndpoint(location);
	}

	public boolean singleton()
	{
		return true;
	}

	public void createEndpoint(String location)
	{
		// format is:
		// udp://192.168.0.1:8080?retryTimes=1&len=8&len2bcd=true&hdrLenBinary=false
		this.location = location;
		if (log.isDebugEnabled()) log.debug("location:" + location);
		StringTokenizer st = new StringTokenizer(location, ":?");
		List params = new ArrayList();
		while (st.hasMoreTokens())
			params.add(st.nextToken());
		ip = StringX.trim((String) params.get(1), "/");
		port = Integer.parseInt((String) params.get(2));
		String strExtParams = (params.size() > 3 ? (String) params.get(3) : null);
		if (StringX.nullity(strExtParams)) return;
		Map extParams = StringX.str2map(strExtParams, "&");
		String val = (String) extParams.get("len");
		if (!StringX.nullity(val)) len = Integer.parseInt(val);

		val = (String) extParams.get("len2bcd");
		if (!StringX.nullity(val)) len2bcd = TRUE.equalsIgnoreCase(val);

		val = (String) extParams.get("hdrLenBinary");
		if (!StringX.nullity(val)) hdrLenBinary = TRUE.equalsIgnoreCase(val);
	}

	public void init() throws Exception
	{
	}

	public void execute(Executable exe) throws Exception
	{
		if (log.isInfoEnabled())
			log.info("UDP corId:" + exe.getCorrelationID() + ", reqbytes:" + exe.request.length);
		DatagramSocket datagramSocket = new DatagramSocket();
		try
		{
			byte[] buf = exe.request;
			if (len > 0)
			{
				byte[] lenBytes = lenBytes(buf);
				byte[] nbuf = new byte[buf.length + lenBytes.length];
				System.arraycopy(lenBytes, 0, nbuf, 0, lenBytes.length);
				System.arraycopy(buf, 0, nbuf, lenBytes.length, buf.length);
				buf = nbuf;
			}
			DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(ip), port);
			datagramSocket.send(datagramPacket);
		}
		catch (Exception e)
		{
			log.error(toString() + ", reqbytes base64: "
					+ new String(StringX.encodeBase64(exe.request)), e);
			throw new AppException(AppRetCode.PROTOCOL_SOCKET,
					new Object[] { String.valueOf(ip), String.valueOf(port) });
		}
		finally
		{
			release(datagramSocket);
		}
	}

	protected byte[] lenBytes(byte[] buf)
	{
		if (len <= 0) return null;
		if (hdrLenBinary) return NumberX.int2bytes(buf.length, len);
		String strlen = (len <= 0 ? String.valueOf(buf.length)
				: StringX.int2str(String.valueOf(buf.length), len)); // 长度固定为10进制的8个字节，不足前面补0
		return len2bcd ? EBCDUtil.gbk2bcd(strlen) : strlen.getBytes();
	}

	protected void release(DatagramSocket datagram)
	{
		if (datagram == null) return;
		log.info("release datagram...");
		try
		{
			datagram.close();
		}
		catch (Exception e)
		{
		}
	}

	public void close()
	{
	}

	public String toString()
	{
		return "UDP://" + ip + ":" + port;
	}

	public Endpoint clone() throws CloneNotSupportedException
	{
		return null;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		createEndpoint(location);
	}

	public int getLen()
	{
		return len;
	}

	public void setLen(int len)
	{
		this.len = len;
	}

	public boolean isLen2bcd()
	{
		return len2bcd;
	}

	public void setLen2bcd(boolean len2bcd)
	{
		this.len2bcd = len2bcd;
	}

	public boolean isHdrLenBinary()
	{
		return hdrLenBinary;
	}

	public void setHdrLenBinary(boolean hdrLenBinary)
	{
		this.hdrLenBinary = hdrLenBinary;
	}
}
