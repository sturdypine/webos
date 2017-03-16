package spc.webos.persistence.matrix.exp.inner;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import spc.webos.util.StringX;

/**
 * RawData column/row expression parser. usage:
 * 
 * <pre>
 * 
 * RawDataExpParser parser = new RawDataExpParser(rawdata);
 * expression = &quot;r3 = r1 + r4 * r6&quot;; // &quot;c0 += avg&quot;; &quot;r10 = sum&quot;; n(0,1)=n(1,2)+n(3,4)
 * parser.setExpression(expression);
 * if (!parser.isValid()) return;
 * OperationEnum type = parser.getOperationType();
 * int position = parser.getResultPosition();
 * String result = parser.getResultExpression();
 * 
 * </pre>
 * 
 * @see RawDataExpression, TestRawDataExpParser
 */

public class MatrixExpParser
{
	private String expression = null;
	public static final String EQUAL = "=";

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public Operation getOperation()
	{
		// "r0 +=.." --> "r0 +", "c12 =.." --> "c12 ", "n0_1=.." --> "n0_1"
		String left = getLeftPart();
		if (StringX.nullity(left)) return Operation.NONE;
		if (left.endsWith("+")) return Operation.INSERT;
		return Operation.REPLACE;
	} // getOperationType ()

	public Type getType()
	{
		// "r0 +" --> TypeEnum.ROW,
		// "C12 " --> TypeEnum.COL
		// "n0_1)" --> TypeEnum.NODE
		String left = getLeftPart();
		if (StringX.nullity(left)) return Type.NONE; // error code
		return Type.getType(left.substring(0, 1));
	} // getType ()

	/**
	 * for TypeEnum.ROW, TypeEnum.COL, not for TypeEnum.NODE
	 */
	public int getPosition()
	{
		String left = getLeftPart();
		if (StringX.nullity(left)) return -1;
		return pickNumber(left);
	} // getInsertNumber ()

	/**
	 * for TypeEnum.NODE, int[2]{row,col};
	 */
	public int[] getNodePosition()
	{
		String left = getLeftPart();
		int[] error = new int[] { -1, -1 };
		if (StringX.nullity(left)) return error;
		String str = left.substring(1); // 12_30
		int[] ints = StringX.split2ints(str, "_");
		if (ints.length != 2) return error;
		return ints;
	}

	/**
	 * return right part of original expression, like: <br> "= (r1+r2) * r3"
	 */
	public String getResultExpression()
	{
		String right = StringX.right(this.expression, EQUAL);
		if (StringX.nullity(right)) return null;
		return (EQUAL + right).trim();
	} // getResultExpression ()

	/**
	 * @return valid left part like: "r1+", "C12", "R10 +"; null if invalid.
	 */
	private String getLeftPart()
	{
		String left = StringX.left(this.expression, EQUAL).trim();
		if (!validLeftPart(left))
		{
			System.err
					.println("RawDataExpParser: left part of expression error.");
			return null;
		}
		return left;
	}

	private boolean validLeftPart(String left)
	{
		// pattern to match left part like: "r1+", "C12 ", "R10 +"
		String pattern_rowcol = "\\s*([rc])\\d+\\s*\\+?";
		Pattern p = new Pattern(pattern_rowcol, REFlags.IGNORE_CASE);
		Matcher m = p.matcher(left);
		boolean isRowCol = m.matches();

		// pattern to match left part like: "n0_1", "N1_13 "
		String pattern_node = "\\s*n\\d+_\\d+\\s*";
		p = new Pattern(pattern_node, REFlags.IGNORE_CASE);
		m = p.matcher(left);
		boolean isNode = m.matches();
		return (isRowCol || isNode);
	}

	private int pickNumber(String left)
	{
		Pattern p = new Pattern("\\d+");
		Matcher m = p.matcher(left);
		if (!m.find()) return 0;
		return Integer.parseInt(m.toString());
	}
} // RawDataExpParser class

