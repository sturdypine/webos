package spc.webos.persistence.matrix.exp.inner;

/**
 * Simple Enum class to indicate insert or replace operation with expression
 * result.
 * 
 * @see RawDataExpParser, TestOperationEnum
 */
public class Operation
{ // implements Comparable {
	private final String name;

	private Operation(String name)
	{
		this.name = name;
	}

	public static final Operation INSERT = new Operation("+=");
	public static final Operation REPLACE = new Operation("=");
	public static final Operation NONE = new Operation("NONE");

	public static Operation getType(String s)
	{
		if (INSERT.name.equalsIgnoreCase(s)) return INSERT;
		if (REPLACE.name.equalsIgnoreCase(s)) return REPLACE;
		return NONE;
	} // getType ()

	public String toString()
	{
		return name;
	}
} // OperationEnum

