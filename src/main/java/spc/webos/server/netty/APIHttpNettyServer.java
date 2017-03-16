package spc.webos.server.netty;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.util.JsonUtil;
import spc.webos.util.SpringUtil;
import spc.webos.util.StringX;

public class APIHttpNettyServer extends AbstractNettyServer
{
	public APIHttpNettyServer()
	{
	}

	public APIHttpNettyServer(int port)
	{
		this.port = port;
	}

	public void bootstrap() throws Exception
	{
		final APIHttpNettyServer server = this;
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>()
				{
					public void initChannel(SocketChannel ch) throws Exception
					{
						ch.pipeline().addLast("idle", new IdleStateHandler(0, 0, idleTimeout));
						ch.pipeline().addLast("readtimeout", new ReadTimeoutHandler(readTimeout));
						ch.pipeline().addLast("writetimeout",
								new WriteTimeoutHandler(writeTimeout));
						ch.pipeline().addLast("decoder", new HttpRequestDecoder()); // inbound:1
						ch.pipeline().addLast("encoder", new HttpResponseEncoder()); // outbound:2
						ch.pipeline().addLast("aggregator",
								new HttpObjectAggregator(maxContentLength)); // inbound:2
						ch.pipeline().addLast("deflater", new HttpContentCompressor()); // outbound:1
						ch.pipeline().addLast("handler", new FullHttpServerInboundHandler(server,
								Common.FILE_JSON_CONTENTTYPE)
						{
							protected void doPost(ChannelHandlerContext ctx) throws Exception
							{
								byte[] request = new byte[fullHttpRequest.content().capacity()];
								fullHttpRequest.content().readBytes(request);
								String uri = fullHttpRequest.uri();
								String[] m = StringX.last2path(uri);
								int type = 0; // api(json)
								if (uri.startsWith(wsUri)) type = 1; // ws soap
								else if (uri.startsWith(xmlUri)) type = 2; // xml

								log.info("service:{}.{}, len:{}", m[0], m[1], request.length);
								if (AppConfig.getInstance().getProperty(
										Config.app_trace_tcp + server.port, true, false))
									log.info("request:{}", new String(request, charset));
								else if (log.isDebugEnabled())
									log.debug("request base64:{}", StringX.base64(request));

								Object ret = null;
								int status = 0;
								try
								{
									// ws soap, xml or json?
									String args = type == 0 ? new String(request, charset)
											: xml2json(request, type);
									String s = m[0].endsWith(servicePostfix) ? m[0]
											: m[0] + servicePostfix;
									ret = SpringUtil.jsonCall(s, m[1],
											StringX.nullity(args) ? null : JsonUtil.json2obj(args),
											-1);
								}
								catch (Exception e)
								{
									status = errStatus;
									ret = SpringUtil.ex2status("HTTP", e);
								}
								byte[] response = type == 0
										? JsonUtil.obj2json(ret).getBytes(charset)
										: json2xml(JsonUtil.obj2json(ret), type, m[1]);
								writeResponse(ctx.channel(), response, status);
							}

							protected String xml2json(byte[] request, int type) throws Exception
							{
								if (type == 2)
									return JsonUtil.xml2json(new String(request, charset));
								Map<String, Object> soap = StringX
										.xml2map(new String(request, charset));
								Map<String, Object> body = (Map<String, Object>) soap
										.get(JsonUtil.TAG_BODY);
								String key = body.keySet().iterator().next();
								if (key == null) return "";
								return JsonUtil.obj2json(body.get(key));
							}

							protected byte[] json2xml(String json, int type, String method)
									throws Exception
							{
								int idx = method.indexOf('$');
								if (idx > 0) method = method.substring(0, idx);
								byte[] body = JsonUtil.json2xml(method, json).getBytes(charset);
								if (type == 2) return body; // is xml
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								baos.write(Common.SOAP_ROOT_START_TAG);
								baos.write(body);
								baos.write(Common.SOAP_ROOT_END_TAG);
								return baos.toByteArray();
							}
						}); // inbound:3
					}
				}).option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);
	}

	protected String wsUri = "/ws/";
	protected String xmlUri = "/xml/";
	protected int errStatus = 555;
	protected String servicePostfix = "Service";
	protected String charset = Common.CHARSET_UTF8;

	public void setErrStatus(int errStatus)
	{
		this.errStatus = errStatus;
	}

	public void setServicePostfix(String servicePostfix)
	{
		this.servicePostfix = servicePostfix;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public void setWsUri(String wsUri)
	{
		this.wsUri = wsUri;
	}

	public void setXmlUri(String xmlUri)
	{
		this.xmlUri = xmlUri;
	}
}
