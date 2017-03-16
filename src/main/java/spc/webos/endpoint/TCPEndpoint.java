package spc.webos.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import spc.webos.constant.AppRetCode;
import spc.webos.exception.AppException;
import spc.webos.util.FileUtil;
import spc.webos.util.StringX;
import spc.webos.util.bytes.BytesUtil;
import spc.webos.util.charset.EBCDUtil;

public class TCPEndpoint extends AbstractTCPEndpoint
{
	public boolean singleton()
	{
		return true;
	}

	/**
	 * 当多个线程同时需要访问时，socket连接需要同步使用。
	 */
	public void execute(Executable exe) throws Exception
	{
		if (longCnn) longCnn(exe);
		else shortCnn(exe);
	}

	protected synchronized void longCnn(Executable exe) throws Exception
	{
		int fail = 0;
		do
		{
			try
			{
				cnn();
				is = cnn(s, os, is, exe);
				return;
			}
			catch (IOException ioe)
			{
				close(); // 如果发生IO异常，则关闭连接
				log.error("err: ip:" + ip + ", port:" + ports[0] + ", retryTimes: " + retryTimes
						+ ", fail: " + fail + ", reqbytes base64: "
						+ new String(StringX.encodeBase64(exe.request)), ioe);
				if (exe.cnnSnd || retryTimes <= fail)
					throw new AppException(AppRetCode.PROTOCOL_SOCKET,
							new Object[] { String.valueOf(ip), String.valueOf(ports[0]) });
				fail++;
			}
		}
		while (!exe.cnnSnd);
	}

	protected void shortCnn(Executable exe) throws Exception
	{
		shortCnn(exe, getCurrentIP(exe), getCurrentPort(exe));
	}

	protected void shortCnn(Executable exe, String ip, int port) throws Exception
	{
		Socket s = null;
		OutputStream os = null;
		InputStream is = null;

		try
		{ // modified by chenjs 2011-06-23 短链接取消重试
			s = createSocket(ip, port);
			// modified by chenjs 2011-10-15 对socket输出使用BufferedOutputStream
			// os = new BufferedOutputStream(s.getOutputStream());
			os = s.getOutputStream();
			is = cnn(s, os, null, exe);
		}
		catch (IOException ioe)
		{
			log.error(toString(ip, port) + ", reqbytes base64: "
					+ new String(StringX.encodeBase64(exe.request)), ioe);
			throw new AppException(AppRetCode.PROTOCOL_SOCKET,
					new Object[] { String.valueOf(ip), String.valueOf(port) });
		}
		finally
		{
			release(s, os, is);
		}
	}

	protected Socket createSocket() throws IOException
	{
		return createSocket(ip, ports[0]);
	}

	protected Socket createSocket(String ip, int port) throws IOException
	{
		if (log.isInfoEnabled()) log.info("cnn::" + toString(ip, port) + ", cnnTimeout:"
				+ cnnTimeout + ", soTimeout:" + soTimeout);
		Socket socket = null;
		if (cnnTimeout <= 0) socket = new Socket(ip, port);
		else
		{
			socket = SocketFactory.getDefault().createSocket();
			socket.connect(new InetSocketAddress(ip, port), cnnTimeout); // 就在connect時設timeout
		}
		// added by chenjs 2011-06-13 增加读消息超时属性
		if (soTimeout > 0) socket.setSoTimeout(soTimeout);
		// added 2012-06-27
		if (receiveBufferSize > 0) socket.setReceiveBufferSize(receiveBufferSize);
		socket.setTcpNoDelay(tcpNoDelay);
		socket.setPerformancePreferences(0, 1, 2);
		return socket;
	}

	protected InputStream cnn(Socket socket, OutputStream os, InputStream is, Executable exe)
			throws Exception
	{
		if (!longCnn && !simplex && exe.timeout > 0) socket.setSoTimeout(exe.timeout * 1000);
		if (log.isInfoEnabled()) log.info("TCP corId:" + exe.getCorrelationID() + ", timeout:"
				+ exe.timeout + ", return:" + !exe.withoutReturn + ", len:" + exe.request.length);
		exe.reqTime = System.currentTimeMillis();
		send(socket, os, exe);
		// added by chenjs 2011-06-13 增加一个当前报文判断属性，如果当前交易调用时指定无返回则不接收返回
		if (simplex || exe.withoutReturn)
		{ // 如果单工或者指定无返回则直接返回
			// 如果是短连接,又是单工或者无返回则容易导致连接退出太快,服务端无法收到字节，需要程序睡眠50ms保证服务器收完请求
			if (log.isInfoEnabled()) log.info("simplex:" + simplex + ", return:"
					+ !exe.withoutReturn + ", sleepMillis:" + sleepMillis);
			if (sleepMillis > 0) Thread.sleep(sleepMillis);
			return is;
		}

		if (socket.isInputShutdown() || socket.isClosed()) log.warn(
				"isInputShutdown: " + socket.isInputShutdown() + ", close: " + socket.isClosed());
		if (is == null) is = socket.getInputStream();
		receive(socket, is, exe);
		exe.resTime = System.currentTimeMillis();
		if (log.isInfoEnabled()) log.info("cost:" + (exe.resTime - exe.reqTime));
		return is;
	}

	protected void send(Socket socket, OutputStream os, Executable exe) throws Exception
	{
		byte[] request = exe.request;
		send(socket, os, request);
		os.flush();
		exe.cnnSnd = true; // 已经发送成功
		log.debug("set cnnSnd is true!!!");
	}

	protected void send(Socket socket, OutputStream os, byte[] buf) throws Exception
	{
		// 700 2013-08-22 不在容许发送不带长度，默认sndlenWithBuf=true
		if (isTrace() && log.isInfoEnabled())
			log.info("trace base64 req buf:[" + new String(StringX.encodeBase64(buf)) + "], buf:["
					+ (dhl.len2bcd ? EBCDUtil.bcd2gbk(buf) : new String(buf)) + "]");
		else if (log.isDebugEnabled())
			log.debug("base64 req buf:[" + new String(StringX.encodeBase64(buf)) + "], buf:["
					+ (dhl.len2bcd ? EBCDUtil.bcd2gbk(buf) : new String(buf)) + "]");
		if (dhl.hdrLen <= 0)
		{ // !sndLenWithBuf ||
			os.write(buf);
			if (log.isInfoEnabled()) log.info("no hdrLen: " + buf.length);
		}
		else
		{
			byte[] lenBytes = lenBytes(buf);
			os.write(BytesUtil.merge(lenBytes, buf));
			if (log.isInfoEnabled())
				log.info("snd buf: " + buf.length + ", lenBytes:[" + new String(lenBytes) + "]");
		}
	}

	protected void receive(Socket socket, InputStream is, Executable exe) throws Exception
	{
		if (is == null) is = socket.getInputStream();
		exe.response = receive(socket, is); // 读取报文
	}

	public byte[] receive(Socket socket, InputStream is) throws Exception
	{ // 短连接并且指定一次读取全部字节，或者长度头为0直接一次性读取
		byte[] buf = null;
		if (dhl.hdrLen <= 0) buf = FileUtil.readMsgWithLen(is, 0); // 无长度信息一次读取
		else if ((!longCnn && readAsOverall))
			buf = readBytesByHdrLength(FileUtil.readMsgWithLen(is, 0), 0); // 短连接有长度头,且要求一次读取
		else buf = readBytesByHdrLength(is); // 长连接或者要求分批读取，则统一使用分批读取

		if (isTrace() && log.isInfoEnabled())
			log.info("trace receive:: len:" + (buf == null ? 0 : buf.length) + "," + new String(buf)
					+ "\nbase64:" + StringX.base64(buf));
		else log.info("receive len:" + (buf == null ? 0 : buf.length)); // 711_20140711
		return buf;
	}

	public Socket getSocket()
	{
		return s;
	}

	public void init() throws Exception
	{
	}

	public void cnn() throws Exception
	{
		if (s != null && longCnn)
		{
			log.debug("it is long cnn!!!");
			return; // modified by spc 2011-03-11
		}
		release(s, os, is);
		try
		{
			s = createSocket();
			// modified by chenjs 2011-10-15 对socket输出使用BufferedOutputStream
			// os = new BufferedOutputStream(s.getOutputStream());
			os = s.getOutputStream();
		}
		catch (Exception e)
		{
			log.warn("Err createSocket: " + toString() + ", e:" + e);
			throw e;
		}
	}

	public void close()
	{
		release(s, os, is);
		is = null;
		os = null;
		s = null;
	}

	public void release(Socket s, OutputStream os, InputStream is)
	{
		if (s == null) return;
		log.info("close socket: " + s.getInetAddress());
		try
		{
			s.close();
		}
		catch (Throwable e)
		{
			log.warn("err to close socket", e);
		}
	}

	public Endpoint clone() throws CloneNotSupportedException
	{
		TCPEndpoint se = new TCPEndpoint();
		se.ip = ip;
		se.ports = ports;
		se.retryTimes = retryTimes;
		se.simplex = simplex;
		se.longCnn = longCnn;
		se.dhl.hdrLen = dhl.hdrLen;
		se.dhl.len2bcd = dhl.len2bcd;
		se.cnnTimeout = cnnTimeout;
		se.soTimeout = soTimeout;
		return se;
	}

	public String toString(String ip, int port)
	{
		return "TCP://" + ip + ":" + port + ", simplex:" + simplex + ", longCnn:" + longCnn + ":"
				+ dhl.hdrLen + ":" + dhl.len2bcd + ":" + dhl.hdrLenBinary + ":" + readAsOverall;
	}

	public TCPEndpoint()
	{
	}

	public TCPEndpoint(String location)
	{
		createEndpoint(location);
	}

	protected Socket s;
	protected OutputStream os;
	protected InputStream is;
}
