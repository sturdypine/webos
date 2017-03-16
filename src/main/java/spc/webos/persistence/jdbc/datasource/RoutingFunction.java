package spc.webos.persistence.jdbc.datasource;

@FunctionalInterface
public interface RoutingFunction
{
	String routing(Object column) throws Exception;
}
