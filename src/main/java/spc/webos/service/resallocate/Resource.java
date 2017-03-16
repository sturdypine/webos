package spc.webos.service.resallocate;

public interface Resource
{
	boolean match(String key);

	String group();

	String id();
}
