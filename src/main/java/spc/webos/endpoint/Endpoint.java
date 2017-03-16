package spc.webos.endpoint;

public interface Endpoint extends AutoCloseable
{
	void setLocation(String location) throws Exception;

	void execute(Executable exe) throws Exception;

	boolean singleton();
}
