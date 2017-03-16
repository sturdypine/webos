package spc.webos.config;

import spc.webos.advice.log.LogTrace;

public interface Config
{
	<T> T getProperty(String key, T defValue);

	<T> T getProperty(String key, boolean jvm, T defValue);

	<T> T getProperty(String[] keys, T defValue);

	boolean isProduct();

	@LogTrace
	void refresh();
	// void setProperty(String key, Object value);
}
