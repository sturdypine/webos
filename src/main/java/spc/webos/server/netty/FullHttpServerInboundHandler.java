package spc.webos.server.netty;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import spc.webos.config.AppConfig;
import spc.webos.constant.Common;
import spc.webos.constant.Config;
import spc.webos.util.LogUtil;
import spc.webos.util.StringX;

public class FullHttpServerInboundHandler extends SimpleChannelInboundHandler<HttpObject>
{
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected FullHttpRequest fullHttpRequest;
	protected FullHttpResponse fullHttpResponse;
	protected AbstractNettyServer server;
	protected String contentType = Common.FILE_TEXT_CONTENTTYPE;

	public FullHttpServerInboundHandler()
	{
	}

	public FullHttpServerInboundHandler(AbstractNettyServer server)
	{
		this.server = server;
	}

	public FullHttpServerInboundHandler(AbstractNettyServer server, String contentType)
	{
		this.server = server;
		this.contentType = contentType;
	}

	public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception
	{
		fullHttpRequest = (FullHttpRequest) msg;
		String uri = fullHttpRequest.uri();
		LogUtil.setTraceNo(server.uuid.format(server.uuid.uuid()), LogUtil.shortUri(uri), true);
		try
		{
			String method = fullHttpRequest.method().name();
			log.info("{} {}, len:{}, remote:{}", method, uri,
					fullHttpRequest.headers().get(HttpHeaderNames.CONTENT_LENGTH),
					ctx.channel().remoteAddress());
			if (log.isDebugEnabled()) log.debug(showHeaders());

			if (method.equalsIgnoreCase("OPTIONS"))
			{
				method = fullHttpRequest.headers().get("Access-Control-Request-Method");
				log.info("OPTIONS, len:{}, headers:{}", fullHttpRequest.content().capacity(),
						showHeaders());
				// if (method.equalsIgnoreCase("post"))
				// fullHttpRequest.setMethod(HttpMethod.POST);
				// else if (method.equalsIgnoreCase("get"))
				// fullHttpRequest.setMethod(HttpMethod.GET);
				writeResponse(ctx.channel(), "OK".getBytes(), 200);
				return;
			}

			try
			{
				if (method.equalsIgnoreCase("post")) doPost(ctx);
				else if (method.equalsIgnoreCase("get")) doGet(ctx);
				else
				{
					log.info("Not POST | GET: {}, headers:{}", fullHttpRequest.method(),
							showHeaders());
					doGet(ctx);
				}
			}
			catch (Exception e)
			{
				log.info("http server fail", e);
				writeResponse(ctx.channel(), e.toString().getBytes(), 500);
			}
		}
		finally
		{
			LogUtil.removeTraceNo();
		}
	}

	protected String showHeaders()
	{
		StringBuilder str = new StringBuilder();
		for (Entry<String, String> entry : fullHttpRequest.headers())
			str.append("header: " + entry.getKey() + '=' + entry.getValue() + "\r\n");
		return str.toString();
	}

	protected void doPost(ChannelHandlerContext ctx) throws Exception
	{
		throw new RuntimeException("Unsupported method doGet");
	}

	protected void doGet(ChannelHandlerContext ctx) throws Exception
	{
		throw new RuntimeException("Unsupported method doGet");
	}

	protected void writeResponse(Channel channel, byte[] buf, int status)
	{
		// Decide whether to close the connection or not.
		boolean close = server.shortCnn
				|| fullHttpRequest.headers().contains(HttpHeaderNames.CONNECTION,
						HttpHeaderValues.CLOSE, true)
				|| (fullHttpRequest.protocolVersion().equals(HttpVersion.HTTP_1_0)
						&& !fullHttpRequest.headers().contains(HttpHeaderNames.CONNECTION,
								HttpHeaderValues.KEEP_ALIVE, true));

		// Build the response object.
		createFullHttpResponse(channel, buf, status);
		fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
		fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.length);
		fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS,
				"x-requested-with,content-type");
		fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS,
				"OPTIONS,POST,GET");
		log.info("response close:{}, len:{}", close, buf.length);
		if (AppConfig.getInstance().getProperty(Config.app_trace_tcp + server.port, true, false))
			log.info("resposne base64:{}", StringX.base64(buf));
		else if (log.isDebugEnabled()) log.debug("resposne base64:{}", StringX.base64(buf));

		// Write the response.
		ChannelFuture future = channel.writeAndFlush(fullHttpResponse);
		// Close the connection after the write operation is done if necessary.
		if (close) future.addListener(ChannelFutureListener.CLOSE);
	}

	protected void createFullHttpResponse(Channel channel, byte[] buf, int status)
	{
		fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status > 0
				? new HttpResponseStatus(status, "service error") : HttpResponseStatus.OK,
				Unpooled.copiedBuffer(buf));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		log.warn("ex: " + server.port, cause);
		ctx.channel().close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception
	{
		messageReceived(ctx, msg);
	}
}
