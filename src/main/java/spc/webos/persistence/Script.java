package spc.webos.persistence;


public class Script
{
	public String script;
	public boolean isTemplate;
	public int type; // Ω≈±æ¿‡–Õ
	public String target;
	// public boolean main;

	public static final int BEENSHELL = 0;
	public static final int MATRIX_INNER_EXP = 1;
	public static final int MATRIX_OUTER_EXP = 2;

	public Script(String script, boolean isTemplate, int type, boolean function)
	{
		this.script = script;
		this.isTemplate = isTemplate;
		this.type = type;
//		if (!isTemplate && type == BEENSHELL && function)
//		{
//			i = new Interpreter();
//			i.setClassLoader(Thread.currentThread().getContextClassLoader());
//			try
//			{
//				i.eval("_FN(){" + script + "}");
//			}
//			catch (Exception e)
//			{
//				throw new RuntimeException("Ex.beenshell script=");
//			}
//		}
	}

	public Script(String script, boolean isTemplate, int type, String target,
			boolean function)
	{
		this.script = script;
		this.isTemplate = isTemplate;
		this.type = type;
		this.target = target;
//		if (!isTemplate && type == BEENSHELL && function)
//		{
//			i = new Interpreter();
//			i.setClassLoader(Thread.currentThread().getContextClassLoader());
//			try
//			{
//				i.eval("_FN(){" + script + "}");
//			}
//			catch (Exception e)
//			{
//				throw new RuntimeException("Ex.beenshell script=");
//			}
//		}
		// this.main = main;
	}
}
