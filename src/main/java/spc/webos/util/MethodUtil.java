package spc.webos.util;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import aj.org.objectweb.asm.ClassReader;
import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.Label;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import aj.org.objectweb.asm.Type;

public class MethodUtil
{
	final static Logger log = LoggerFactory.getLogger(MethodUtil.class);
	private static Map<Method, String[]> PARAM_NAMES = new ConcurrentHashMap<>();
	private static Map<Class<?>, Map<Method, Annotation>> ANNS = new ConcurrentHashMap<>();
	private static Map<String, Method> METHODS = new ConcurrentHashMap<>(); // sayHello#2->method
	
	public boolean isPrimitive(Class<?> c)
	{
		if (Number.class.isAssignableFrom(c) || c == String.class || c.isPrimitive()) return true;
		return false;
	}

	public static Method findMethod(Object target, String method, int argNum)
	{
		if (target == null) return null;
		Method[] candidates = target.getClass().getMethods();
		for (int i = 0; i < candidates.length; i++)
		{
			Method candidate = candidates[i];
			if (candidate.getName().equals(method))
			{
				Class[] paramTypes = candidate.getParameterTypes();
				if (paramTypes.length == argNum) return candidate;
			}
		}
		return null;
	}

	public static String[] getParameterNames(Class<?> clazz, final Method method)
	{
		String[] names = PARAM_NAMES.get(method);
		if (names != null) return names;

		ParamNames pm = getAnnotation(clazz, method, ParamNames.class);
		names = pm == null ? null : StringX.split(pm.value(), ",");
		if (names == null)
			names = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
		if (names == null) names = getParameterNamesByAsm4(clazz, method);
		if (names == null) names = getParameterNamesByJava8(method);
		if (names != null) PARAM_NAMES.put(method, names);
		return names;
	}

	public static <T extends Annotation> T getAnnotation(Class<?> clazz, final Method method,
			Class<T> annotationClass)
	{
		if (method == null) return null;
		T ann = method.getAnnotation(annotationClass);
		if (ann != null) return ann;
		for (Class<?> infs : clazz.getInterfaces())
		{ // 查找接口上的方法注解
			Method m = findMethod(infs, method.getName(), method.getParameterTypes());
			if (m != null && (ann = m.getAnnotation(annotationClass)) != null) return ann;
		}
		// 查找父亲类中的方法注解
		return getAnnotation(clazz.getSuperclass(),
				findMethod(clazz.getSuperclass(), method.getName(), method.getParameterTypes()),
				annotationClass);
	}

	// 根据参数个数找指定方法
	public static Method findMethod(Class target, String method, int argsNum)
	{
		String key = target.getName() + "#" + method + "#" + argsNum;
		Method me = METHODS.get(key);
		if (me != null) return me;
		Method[] methods = target.getMethods();
		for (Method m : methods)
			if (m.getName().equals(method)
					&& (argsNum < 0 || m.getParameterTypes().length == argsNum))
			{
				METHODS.put(key, m);
				return m;
			}
		return null;
	}

	// 根据指定参数类型找方法，此方法参数只要能接收指定参数就行，无需完全等同
	public static Method findMethod(Class target, String method, Class[] parameterTypes)
	{
		Method[] methods = target.getMethods();
		for (Method m : methods)
		{
			if (m.getName().equals(method) && m.getParameterTypes().length == parameterTypes.length)
			{
				int i = 0;
				for (; i < parameterTypes.length; i++)
					if (!m.getParameterTypes()[i].isAssignableFrom(parameterTypes[i])) break;
				if (i >= parameterTypes.length) return m;
			}
		}
		return null;
	}

	public static java.lang.reflect.Type[] getGenericParameterTypes(Class s, String m, int argNum)
	{
		log.debug("c:{}, m:{}, args:{}", s, m, argNum);
		Method method = findMethod(s, m, argNum);
		if (method == null) log.info("cannot find method:{}, argNum:{} in class:{}", m, argNum, s);
		return method.getGenericParameterTypes();
	}

	public static String[] getParameterNamesByJava8(final Method method)
	{
		String[] parameterNames = new String[method.getParameterCount()];
		Parameter[] ps = method.getParameters();
		for (int i = 0; i < parameterNames.length; i++)
			parameterNames[i] = ps[i].getName();
		return parameterNames;
	}

	/**
	 * 获取指定类指定方法的参数名
	 * 
	 * @param clazz
	 *            要获取参数名的方法所属的类
	 * @param method
	 *            要获取参数名的方法
	 * @return 按参数顺序排列的参数名列表，如果没有参数，则返回null
	 */
	public static String[] getParameterNamesByAsm4(Class<?> clazz, final Method method)
	{
		final Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes == null || parameterTypes.length == 0) return null;
		final Type[] types = new Type[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++)
			types[i] = Type.getType(parameterTypes[i]);
		final String[] parameterNames = new String[parameterTypes.length];

		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(".");
		className = className.substring(lastDotIndex + 1) + ".class";
		try (InputStream is = clazz.getResourceAsStream(className))
		{
			ClassReader classReader = new ClassReader(is);
			classReader.accept(new ClassVisitor(Opcodes.ASM4)
			{
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc,
						String signature, String[] exceptions)
				{ // 只处理指定的方法
					Type[] argumentTypes = Type.getArgumentTypes(desc);
					if (!method.getName().equals(name) || !Arrays.equals(argumentTypes, types))
						return null;
					return new MethodVisitor(Opcodes.ASM4)
					{
						@Override
						public void visitLocalVariable(String name, String desc, String signature,
								Label start, Label end, int index)
						{
							// 静态方法第一个参数就是方法的参数，如果是实例方法，第一个参数是this
							if (Modifier.isStatic(method.getModifiers()))
								parameterNames[index] = name;
							else if (index > 0) parameterNames[index - 1] = name;
						}
					};
				}
			}, 0);
		}
		catch (Exception e)
		{
			log.debug("fail to getParameterNamesByAsm4:" + method, e);
			return null;
		}
		return parameterNames;
	}

	@Target({ ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Documented
	public static @interface ParamNames
	{
		String value() default "";
	}
}
