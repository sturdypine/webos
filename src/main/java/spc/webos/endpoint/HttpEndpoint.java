package spc.webos.endpoint;

import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.config.AppConfig;
import spc.webos.constant.AppRetCode;
import spc.webos.constant.Config;
import spc.webos.exception.AppException;
import spc.webos.util.StringX;

public class HttpEndpoint implements Endpoint
{
	public HttpEndpoint()
	{
	}

	public HttpEndpoint(String url)
	{
		this.url = url;
	}

	public void execute(Executable exe) throws Exception
	{
		long start = System.currentTimeMillis();
		log.info("http corId:{}, timeout:{}, url:{}, len:{}", exe.getCorrelationID(), exe.timeout,
				url, (exe.request == null ? ")" : exe.request.length));

		HttpPost post = new HttpPost(url);
		// HttpGet post = new HttpGet(url);
		post.setConfig(RequestConfig.custom().setSocketTimeout(exe.timeout * 1000)
				.setConnectTimeout(1000).build());
		// added by chenjs 2013-03--25， 在webservice中可能需要填写soapaction
		if (exe.reqHttpHeaders != null && !exe.reqHttpHeaders.isEmpty())
			exe.reqHttpHeaders.keySet().iterator()
					.forEachRemaining((key) -> post.setHeader(key, exe.reqHttpHeaders.get(key)));
		if (exe.request != null && exe.request.length > 0)
			post.setEntity(new ByteArrayEntity(exe.request));

		try (CloseableHttpClient client = HttpClients.createDefault();
				CloseableHttpResponse response = client.execute(post))
		{
			exe.cnnSnd = true; // http协议无论如何都算成功
			exe.httpStatus = response.getStatusLine().getStatusCode();

			// 设置响应信息
			exe.response = EntityUtils.toByteArray(response.getEntity());
			log.info("http status:{}, len:{}, cost:{}", exe.httpStatus,
					exe.response == null ? 0 : exe.response.length,
					(System.currentTimeMillis() - start));
			if (log.isDebugEnabled()
					|| AppConfig.getInstance().getProperty(Config.app_trace_tcp, true, false))
				log.info("response:{}", new String(exe.response));
		}
		catch (IOException ioe)
		{
			log.error("http err:" + url + ", status:" + exe.httpStatus + ", request.base64:"
					+ (exe.request == null ? "" : StringX.base64(exe.request)), ioe);
			throw new AppException(AppRetCode.PROTOCOL_HTTP,
					new Object[] { url, new Integer(exe.httpStatus) });
		}
		if (exe.httpStatus < 200 || exe.httpStatus >= 300)
		{ // 如果返回不在200-300服务器正常返回则打印调试日志
			log.info("http status:{}, request:{}\nresponse:{}", exe.httpStatus,
					exe.request == null ? "" : new String(exe.request),
					exe.response == null ? "" : new String(exe.response));
		}
	}

	public boolean singleton()
	{
		return true;
	}

	public void close()
	{
	}

	public void setLocation(String location) throws Exception
	{
		this.url = location;
	}

	protected String url; // 后台url地址
	protected Logger log = LoggerFactory.getLogger(getClass());
}
