package spc.webos.endpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

/**
 * 根据资源定义符, 动态构建endpoint类型。比如tcp://192.168.0.01:8080,
 * http://192.168.0.01:8080/FA/ws. 目前只支持tcp/http协议
 * 
 * @author chenjs
 * 
 */
public class EndpointFactory
{
	protected final static Logger log = LoggerFactory.getLogger(EndpointFactory.class);
	protected Map<String, Endpoint> endpoints = new ConcurrentHashMap<>();
	protected static Map<String, Class<?>> PROTOCOLS = new ConcurrentHashMap<>();

	static
	{
		PROTOCOLS.put("http", HttpEndpoint.class);
		PROTOCOLS.put("https", HttpsEndpoint.class);
		PROTOCOLS.put("tcp", TCPEndpoint.class);
		PROTOCOLS.put("udp", UDPEndpoint.class);
	}

	public Endpoint getEndpoint(String location) throws Exception
	{
		Endpoint endpoint = endpoints.get(location);
		if (endpoint != null) return endpoint;
		endpoint = createEndpoint(location);
		if (endpoint.singleton()) endpoints.put(location, endpoint);
		return endpoint;
	}

	public Endpoint createEndpoint(String location) throws Exception
	{
		if (StringX.nullity(location)) return null;
		location = StringX.trim(location);
		if (ClusterEndpoint.isCluster(location)) return new ClusterEndpoint(location);
		int idx = location.indexOf(":");
		String protocol = location.substring(0, idx).toLowerCase();
		if (protocol.equals("class"))
		{ // 使用自定义类完成
			int index = location.indexOf(':', 8);
			String strClass = location.substring(6, index);
			String loc = location.substring(index + 1);
			Endpoint endpoint = (Endpoint) Class
					.forName(strClass, true, Thread.currentThread().getContextClassLoader())
					.newInstance();
			endpoint.setLocation(loc);
			return endpoint;
		}
		Class<?> clazz = PROTOCOLS.get(protocol);
		if (clazz != null)
		{
			Endpoint endpoint = (Endpoint) clazz.newInstance();
			endpoint.setLocation(location);
			return endpoint;
		}

		log.warn("undefined protocal: " + location);
		return null;
	}

	public static void register(String protocol, Class<?> clazz)
	{
		if (PROTOCOLS.containsKey(protocol))
			log.warn("protocol:{} has exists class:{}, will be replaced", protocol, clazz);
		else log.info("register protocol:{}={}", protocol, clazz);
		PROTOCOLS.put(protocol, clazz);
	}

	public static void register(String protocol, String clazz)
	{
		try
		{
			register(protocol,
					Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void setProtocols(String protocols)
	{
		if (StringX.nullity(protocols)) return;
		Map<String, String> ps = (Map<String, String>) JsonUtil.json2obj(protocols);
		ps.forEach((k, v) -> register(k, v));
	}

	static EndpointFactory factory = new EndpointFactory();

	public static EndpointFactory getInstance()
	{
		return factory;
	}
}
