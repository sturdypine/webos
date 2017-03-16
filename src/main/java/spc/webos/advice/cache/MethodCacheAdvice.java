package spc.webos.advice.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import spc.webos.advice.cache.MethodCache.MCPath;
import spc.webos.redis.JedisTemplate;
import spc.webos.util.FileUtil;
import spc.webos.util.StringX;

public class MethodCacheAdvice
{
	protected final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired(required = false)
	protected JedisTemplate jedis;
	@Value("${app.mc.prefix?mc:}")
	protected String prefix = "mc:";

	public Object cache(ProceedingJoinPoint pjp) throws Throwable
	{
		if (jedis == null || !jedis.isReady()) return pjp.proceed();
		// 1. 当前方法是否需要缓存, 优先查找方法实现的注解，如果没有则查找接口注解
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method methodInf = sig.getDeclaringType().getMethod(sig.getName(), sig.getParameterTypes()); // 接口方法
		Method methodImpl = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(),
				((MethodSignature) pjp.getSignature()).getParameterTypes());
		MethodCache cacheImpl = methodImpl.getAnnotation(MethodCache.class); // 优先查找实现类方法的注解
		MethodCache cacheInf = methodInf.getAnnotation(MethodCache.class); // 接口是否定义了注解
		MethodCache cache = cacheImpl != null ? cacheImpl : cacheInf;
		if (cache == null) return pjp.proceed();
		String prefixKey = (cacheInf != null ? cacheInf : cacheImpl).value();
		if (StringX.nullity(prefixKey)) return pjp.proceed(); // 没有配置key

		int expire = cache.expire();
		boolean read = cache.read();
		boolean write = cache.write();

		// 2. 根据参数生成key, 参数key值得所有信息优先使用接口的注解参数
		Object[] args = pjp.getArgs();
		List<String> keys = getKeys(
				(cacheInf != null ? methodInf : methodImpl).getParameterAnnotations(), args); // 先找接口方法里的参数
		String origKey = StringX.join(keys, (cacheInf != null ? cacheInf : cacheImpl).delim());
		String key = prefix + prefixKey + ((cacheInf != null ? cacheInf : cacheImpl).md5()
				? StringX.md5(origKey.getBytes()) : origKey);
		log.debug("origKey:{}, key:{}", origKey, key);

		if (read)
		{
			StringBuilder value = new StringBuilder();
			try
			{ // 缓存失败不影响业务逻辑
				jedis.execute((redis) -> {
					String v = redis.get(key);
					log.info("Redis read:{}, len:{}", key, v == null ? 0 : v.length());
					log.debug("Redis read:{}, value:{}", key, v);
					if (v != null) value.append(v);
				});
				if (value.length() > 0)
					return FileUtil.unfst(StringX.decodeBase64(value.toString()));
			}
			catch (Exception e)
			{
				log.info("Redis fail read key:" + key, e);
			}
		}
		Object ret = pjp.proceed();
		if (!write || ret == null) return ret;
		try
		{ // 缓存失败不影响业务逻辑
			String value = StringX.base64(FileUtil.fst(ret));
			jedis.execute((redis) -> {
				log.info("Redis write:{}, expire:{}, len:{}", key, expire, value.length());
				if (expire > 0) redis.setex(key, expire, value);
				else redis.set(key, value);
			});
		}
		catch (Exception e)
		{
			log.info("Redis fail write key:" + key, e);
		}
		return ret;
	}

	// 找到第一个注解DSParam的参数注解信息，以及当前参数值
	protected List<String> getKeys(Annotation[][] annotations, Object[] args)
	{
		List<String> keys = new ArrayList<>();
		if (annotations == null) return keys;
		for (int i = 0; i < args.length; i++)
			for (int j = 0; annotations[i] != null && j < annotations[i].length; j++)
				if (annotations[i][j] instanceof MCPath)
					keys.addAll(getKeys((MCPath) annotations[i][j], args[i]));
		return keys;
	}

	protected List<String> getKeys(MCPath path, Object arg)
	{
		List<String> keys = new ArrayList<>();
		if (StringX.nullity(path.value()))
		{
			keys.add(arg != null ? arg.toString() : path.nullValue());
			return keys; // 如果注解路径没有则直接使用此参数
		}
		BeanWrapperImpl wrapper = new BeanWrapperImpl(false);
		wrapper.setWrappedInstance(arg);
		for (String name : StringX.split(path.value(), ","))
		{
			Object value = wrapper.getPropertyValue(name);
			log.debug("Path:{}={}, arg:{}", name, value, arg);
			keys.add(value == null ? path.nullValue() : value.toString());
		}
		return keys;
	}

	public void setJedis(JedisTemplate jedis)
	{
		this.jedis = jedis;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}
}
