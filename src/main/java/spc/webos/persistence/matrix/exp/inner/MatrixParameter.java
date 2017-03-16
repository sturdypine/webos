package spc.webos.persistence.matrix.exp.inner;

import spc.webos.persistence.matrix.IMatrix;
import spc.webos.util.StringX;

import com.eteks.parser.ExpressionParameter;

/**
 * used by RawDataExpression while get parameters' key and value. A dummy class
 * implementing ExpressionParameter supporting rawdata col, row and node.
 * 
 * Usage:
 * 
 * <pre>
 * 
 * RawDataParameter rdparam = new RawDataParameter(rd);
 * ExpressionParser parser = new ExpressionParser(rdparam);
 * CompiledExpression compiled = parser.compileExpression(resultExpression);
 * type = rdparam.getType();
 * size = rdparam.getSize();
 * for (i = 0; i &lt; size; i++)
 * {
 * 	rdparam.next();
 * 	compiled.computeExpression();
 * }
 * </pre>
 * 
 * @see MatrixExpression
 */
public class MatrixParameter implements ExpressionParameter
{
	private static final long serialVersionUID = 1L;
	private IMatrix matrix;
	private int h = 0;
	private int w = 0;
	/**
	 * "C": col, "R": row, "N": node, other: error
	 */
	private Type type = null;
	/**
	 * if (type==col) size = rawdata.height if (type==row) size = rawdata.width
	 * if (type==node) nouse
	 */
	private int size = 0;

	public MatrixParameter(IMatrix matrix)
	{
		this.matrix = matrix;
		h = matrix.getHeight();
		w = matrix.getWidth();
	}

	/**
	 * float index of rawdata's row or col TypeEnum.COL --> [0, h) for every
	 * node in the col TypeEnum.ROW --> [0, w) for every node in the row
	 */
	private int index = -1;

	public void next()
	{
		index++;
	}

	public int getSize()
	{
		return size;
	}

	public Type getType()
	{
		return type;
	}

	/**
	 * Returns a key matching the parameter, if parameter is a valid parameter
	 * in expressions parsed with this instance.
	 * 
	 * @param parameter
	 *            Like "R3", "c12", "N(1,3)" TypeEnum.COL / TypeEnum.ROW -->
	 *            return index of column/row TypeEnum.NODE --> return (row, col)
	 *            of Node.
	 */
	public Object getParameterKey(String parameter)
	{
		// current type, must keep same as others.
		Type ctype = parseType(parameter);
		if (ctype == Type.NONE) return null;
		if (this.type == null) this.type = ctype;
		else if (this.type != ctype)
		{
			System.err
					.println("RawDataParameter.getParameterKey (String): conflicting types.");
			return null;
		}
		if (ctype == Type.COL) { return getColParamKey(parameter); }
		if (ctype == Type.ROW) { return getRowParamKey(parameter); }
		if (ctype == Type.NODE) { return getNodeParamKey(parameter); }
		return null;
	}

	/**
	 * @param param
	 *            Like "C13"
	 * @return Like Integer(13)
	 */
	private Integer getColParamKey(String param)
	{
		this.size = h;
		return Integer.valueOf(param.substring(1));
	}

	/**
	 * @param param
	 *            Like "r13"
	 * @return Like Integer(13)
	 */
	private Integer getRowParamKey(String param)
	{
		this.size = w;
		return Integer.valueOf(param.substring(1));
	}

	/**
	 * @param param
	 *            Like "N1_3)"
	 * @return Like int[]{1,3}
	 */
	private int[] getNodeParamKey(String param)
	{
		String s = param.substring(1);
		int[] rt = StringX.split2ints(s, "_");
		if (rt.length == 2) return rt;
		return null;
	}

	/**
	 * Returns the value matching parameterKey. This value may be of any type
	 * (Double, String or other). in this case, give index of columns/row,
	 * return Node's number value.
	 */
	public Object getParameterValue(Object parameterKey)
	{
		if (this.type == Type.COL) return getColParamValue(((Integer) parameterKey)
				.intValue());
		if (this.type == Type.ROW) return getRowParamValue(((Integer) parameterKey)
				.intValue());
		if (this.type == Type.NODE) return getNodeparamValue((int[]) parameterKey);
		return null;
	}

	private Object getColParamValue(int col)
	{
		return matrix.getCell(index, col);
//		if (!(v instanceof Number)) return v;
//		if (v instanceof Double) return ((Double) v);
//		return new Double(v.toString());
	}

	private Object getRowParamValue(int row)
	{
		return matrix.getCell(row, index);
//		if (!(v instanceof Number)) return v;
//		if (v instanceof Double) return ((Double) v);
//		return new Double(v.toString());
	}

	private Object getNodeparamValue(int[] row_col)
	{
		return matrix.getCell(row_col[0], row_col[1]);
//		if (!(v instanceof Number)) return v;
//		if (v instanceof Double) return ((Double) v);
//		return new Double(v.toString());
	}

	private Type parseType(String parameter)
	{
		if (StringX.nullity(parameter))
		{
			System.err.println("RawDataParameter: parameter is null.");
			return Type.NODE;
		}
		parameter = parameter.trim().toUpperCase();
		String prefix = parameter.substring(0, 1);
		return Type.getType(prefix);
	}

	// private int checkCellIndex(int cell_index)
	// {
	// if (type == TypeEnum.COL)
	// {
	// return NumberX.getSafeIndex(cell_index, w);
	// }
	// else if (type == TypeEnum.ROW)
	// {
	// return NumberX.getSafeIndex(cell_index, h);
	// }
	// else
	// {
	// return cell_index;
	// }
	// }

} // RawDataParameter

