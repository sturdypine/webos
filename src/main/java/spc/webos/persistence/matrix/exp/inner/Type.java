package spc.webos.persistence.matrix.exp.inner;

/**
 * Simple Enum class to indicate column or row type in expression.
 * 
 * @see RawDataExpression, TestTypeEnum
 */
public class Type
{ // implements Comparable {
	private final String name;

	private Type(String name)
	{
		this.name = name;
	}

	public static final Type COL = new Type("C");
	public static final Type ROW = new Type("R");
	public static final Type NODE = new Type("N");
	public static final Type NONE = new Type("NONE");

	public static Type getType(String s)
	{
		if (COL.name.equalsIgnoreCase(s)) return COL;
		if (ROW.name.equalsIgnoreCase(s)) return ROW;
		if (NODE.name.equalsIgnoreCase(s)) return NODE;
		return NONE;
	} // getType ()

	public String toString()
	{
		return name;
	}
} // TypeEnum

