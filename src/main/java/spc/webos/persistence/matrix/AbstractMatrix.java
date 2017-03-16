package spc.webos.persistence.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import spc.webos.persistence.matrix.exp.inner.MatrixExpParser;
import spc.webos.persistence.matrix.exp.inner.MatrixInterpreter;
import spc.webos.persistence.matrix.exp.inner.MatrixParameter;
import spc.webos.persistence.matrix.exp.inner.Operation;
import spc.webos.persistence.matrix.exp.inner.Type;
import spc.webos.util.StringX;

import com.eteks.parser.CompiledExpression;
import com.eteks.parser.ExpressionParser;

public abstract class AbstractMatrix implements IMatrix
{
	public IMatrix add(IMatrix b)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * M.add(s)
	 * 
	 * @param s
	 *            another sunqian
	 * @return M
	 */
	public IMatrix add(double s)
	{
		for (int i = 0; i < getHeight(); i++)
		{
			for (int j = 0; j < getWidth(); j++)
				setCell(i, j, add(getCell(i, j), s));
		}
		return this;
	}

	public List sumCol(int[] colNum)
	{
		if (colNum == null) return null;
		int h = getHeight();
		List result = new ArrayList(getHeight());
		for (int i = 0; i < h; i++)
		{
			double sum = 0.0;
			for (int k = 0; k < colNum.length; k++)
			{
				if (getCell(i, colNum[k]) instanceof Number) sum += new Double(
						getCell(i, colNum[k]).toString()).doubleValue();
			}
			result.add(new Double(sum));
		}

		return result;
	}

	/**
	 * M.sumRow(r0,rows)
	 * 
	 * @param r0,rows
	 *            another sunqian
	 * @return List
	 */
	public List sumRow(int[] rowNum)
	{
		int w = getWidth();
		List result = new ArrayList(getWidth());
		for (int j = 0; j < w; j++)
		{
			double sum = 0.0;
			for (int k = 0; k < rowNum.length; k++)
			{
				if (getCell(rowNum[k], j) instanceof Number) sum += new Double(
						getCell(rowNum[k], j).toString()).doubleValue();
			}
			result.add(new Double(sum));
		}
		return result;
	}

	/**
	 * v = add(v,d)
	 * 
	 * @param v,d
	 *            another sunqian
	 * @return v
	 */
	protected Object add(Object v, double d)
	{
		if (!(v instanceof Number)) { return v; }
		return new Double(new Double(v.toString()).doubleValue() + d);
	}

	public IMatrix subtract(IMatrix b)
	{
		// TODO Auto-generated method stub

		return this;
	}

	/**
	 * M.subtract(s)
	 * 
	 * @param s
	 *            another sunqian
	 * @return M
	 */
	public IMatrix subtract(double s)
	{
		for (int i = 0; i < getHeight(); i++)
		{
			for (int j = 0; j < getWidth(); j++)
				setCell(i, j, subtract(getCell(i, j), s));
		}
		return this;
	}

	protected Object subtract(Object v, double d)
	{
		if (!(v instanceof Number)) { return v; }
		return new Double(new Double(v.toString()).doubleValue() - d);
	}

	public IMatrix multiply(IMatrix B)
	{
		// TODO Auto-generated method stub
		return this;
	}

	/**
	 * M.multiply(s)
	 * 
	 * @param s
	 *            another sunqian
	 * @return M
	 */
	public IMatrix multiply(double s)
	{
		for (int i = 0; i < getHeight(); i++)
		{
			for (int j = 0; j < getWidth(); j++)
				setCell(i, j, multiply(getCell(i, j), s));
		}
		return this;
	}

	public IMatrix divide(IMatrix B)
	{
		// TODO Auto-generated method stub
		return this;
	}

	/**
	 * M.divide(s)
	 * 
	 * @param s
	 *            another sunqian
	 * @return M
	 */
	public IMatrix divide(double s)
	{
		for (int i = 0; i < getHeight(); i++)
		{
			for (int j = 0; j < getWidth(); j++)
				setCell(i, j, divide(getCell(i, j), s));
		}
		return this;
	}

	protected Object divide(Object v, double d)
	{
		if (!(v instanceof Number)) { return v; }
		return new Double(new Double(v.toString()).doubleValue() / d);
	}

	/**
	 * M.uminus()
	 * 
	 * @param another
	 *            sunqian
	 * @return M
	 */
	public IMatrix uminus()
	{
		// for (int i = 0; i < getHeight(); i++)
		// {
		// for (int j = 0; j < getWidth(); j++)
		// setCell(i, j, uminus(getCell(i, j)));
		// }
		return this;
	}

	/**
	 * M.insertRow(r0,row)
	 * 
	 * @param r0,row
	 *            another sunqian
	 * @return M
	 */
	public IMatrix insertRow(int r0, Object row)
	{
		if (r0 <= getHeight())
		{
			moveRow(r0, 1);
		}
		else
		{
			moveRow(r0, 1 + (r0 - getHeight()));
		}
		replaceRow(r0, row);
		return this;
	}

	/**
	 * M.insertCol(c0,col)
	 * 
	 * @param c0,col
	 *            another sunqian
	 * @return M
	 */
	public IMatrix insertCol(int c0, Object col)
	{
		if (c0 <= getWidth())
		{
			moveCol(c0, 1);
		}
		else
		{
			moveCol(c0, 1 + (c0 - getWidth()));
		}
		replaceCol(c0, col);
		return this;
	}

	/**
	 * M.averageCol(c0,cols)
	 * 
	 * @param c0,cols
	 *            another sunqian
	 * @return List
	 */
	public List averageCol(int c0, int cols)
	{
		List resultCol = sumCol(c0, cols);
		for (int i = 0; i < resultCol.size(); i++)
		{
			resultCol.set(i, new Double(((Double) resultCol.get(i))
					.doubleValue()
					/ cols));
		}
		return resultCol;
	}

	/**
	 * M.averageCol(colNum)
	 * 
	 * @param colNum
	 *            another sunqian
	 * @return List
	 */
	public List averageCol(int[] colNum)
	{
		List resultCol = sumCol(colNum);
		for (int i = 0; i < resultCol.size(); i++)
		{
			resultCol.set(i, new Double(((Double) resultCol.get(i))
					.doubleValue()
					/ colNum.length));
		}
		return resultCol;
	}

	/**
	 * M.averageRow(r0,rows)
	 * 
	 * @param r0,rows
	 *            another sunqian
	 * @return List
	 */
	public List averageRow(int r0, int rows)
	{
		List resultRow = sumRow(r0, rows);
		for (int i = 0; i < resultRow.size(); i++)
		{
			resultRow.set(i, new Double(((Double) resultRow.get(i))
					.doubleValue()
					/ rows));
		}
		return resultRow;
	}

	/**
	 * M.averageRow(rowNum)
	 * 
	 * @param rowNum
	 *            another sunqian
	 * @return List
	 */
	public List averageRow(int[] rowNum)
	{
		List resultRow = sumRow(rowNum);
		for (int i = 0; i < resultRow.size(); i++)
		{
			resultRow.set(i, new Double(((Double) resultRow.get(i))
					.doubleValue()
					/ rowNum.length));
		}
		return resultRow;
	}

	public IMatrix merge(IMatrix rightRD, String[] sltLeftCols,
			String[] sltRightCols, Map regulation)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IMatrix merge(IMatrix rightRD, int[] sltLeftCols,
			int[] sltRightCols, Map regulation)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IMatrix removeCol(int col)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IMatrix removeRow(int row)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * M1.rightConnect(M2) join M2 to the right of M1
	 * 
	 * @param M2
	 *            another sunqian
	 * @return %M1M2%
	 */
	public IMatrix rightConnect(IMatrix targetM)
	{
		if (this.getHeight() < targetM.getHeight()) this.expandRow(targetM
				.getHeight()
				- this.getHeight());
		for (int j = 0; j < targetM.getWidth(); j++)
			this.insertCol(this.getWidth(), targetM.getCol(j));
		return this;
	}

	/**
	 * M1.bottomConnect(M2) join M2 to the bottom of M1
	 * 
	 * @param M2
	 *            another sunqian
	 * @return %M1% %M2%
	 */
	public IMatrix bottomConnect(IMatrix targetM)
	{
		if (this.getWidth() < targetM.getWidth()) this.expandCol(targetM
				.getWidth()
				- this.getWidth());
		for (int i = 0; i < targetM.getHeight(); i++)
			this.insertRow(this.getHeight(), targetM.getRow(i));
		return this;
	}

	public IMatrix replaceRow(int row0, Object row)
	{
		int w = getWidth();
		if (row instanceof List)
		{
			int size = w > ((List) row).size() ? ((List) row).size() : w;
			for (int j = 0; j < size; j++)
				setCell(row0, j, ((List) row).get(j));
		}
		else if (row instanceof Object[])
		{
			int size = w > ((Object[]) row).length ? ((Object[]) row).length
					: w;
			for (int j = 0; j < size; j++)
				setCell(row0, j, ((Object[]) row)[j]);
		}
		return this;
	}

	public IMatrix replaceCol(int col0, Object col)
	{
		int h = getHeight();
		if (col instanceof List)
		{
			int size = h > ((List) col).size() ? ((List) col).size() : h;
			for (int i = 0; i < size; i++)
				setCell(i, col0, ((List) col).get(i));

		}
		else if (col instanceof Object[])
		{
			int size = h > ((Object[]) col).length ? ((Object[]) col).length
					: h;
			for (int i = 0; i < size; i++)
				setCell(i, col0, ((Object[]) col)[i]);

		}
		return this;
	}

	public IMatrix replace(IMatrix target, int tr0, int tc0, int tr1, int tc1,
			int r0, int c0)
	{
		int tempHeight = (tr0 + tr1) <= target.getHeight() ? tr1 : (target
				.getHeight() - tr0);
		int tempWidth = (tc0 + tc1) <= target.getWidth() ? tc1 : (target
				.getWidth() - tc0);
		int h = tempHeight < (this.getHeight() - r0) ? tempHeight : (this
				.getHeight() - r0);
		int w = tempWidth < (this.getWidth() - c0) ? tempWidth : (this
				.getWidth() - c0);
		for (int i = 0; i < h; i++)
		{
			for (int j = 0; j < w; j++)
			{
				this.setCell((r0 + i), (c0 + j), target.getCell(i, j));
			}
		}
		return this;
	}

	/**
	 * M.rotate()
	 * 
	 * @param another
	 *            sunqian
	 * @return m
	 */
	public IMatrix rotate()
	{
		// TODO Auto-generated method stub
		IMatrix m = create(this.getWidth(), this.getHeight());
		for (int i = 0; i < m.getHeight(); i++)
		{
			for (int j = 0; j < m.getWidth(); j++)
			{
				m.setCell(i, j, this.getCell(j, i));
			}
		}
		return m;
	}

	public IMatrix select(int[] cols, Map condition)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List selectCol(int colIndex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IMatrix selectCols(int[] cols)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List selectRow(int rowIndex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IMatrix selectRows(Map condition)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List sumCol(int col0, int cols)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List sumRow(int row0, int rows)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * cell对象计算方法
	 * 
	 * @param v
	 * @param d
	 * @return
	 */
	protected Object multiply(Object v, double d)
	{
		return v;
	}

	public Object clone()
	{
		return null;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<table width=95%>");
		buf.append("<tr><td>");
		for (int i = 0; i < getWidth(); i++)
		{
			buf.append("<td><b>");
			buf.append(i);
		}
		for (int i = 0; i < getHeight(); i++)
		{
			buf.append("<tr><td><b>");
			buf.append(i);
			for (int j = 0; j < getWidth(); j++)
			{
				buf.append("<td>");
				buf.append(getCell(i, j));
			}
		}
		buf.append("</table>");
		return buf.toString();
	}

	// ---- 矩阵表达式运算
	/**
	 * get new rawdata after compute with expression. "\n"
	 */
	public IMatrix process(String expression, String delim)
	{
		// IMatrix nrd = this.matrix;// = new ListMatrix();
		List nodes = new ArrayList();
		if (hasError(expression)) return this;
		if (StringX.nullity(delim)) delim = "\n";
		String[] expression_array = parseExpress(expression, delim);
		for (int i = 0; i < expression_array.length; i++)
		{
			if (expression_array[i].equals("")) continue;

			MatrixExpParser parser = new MatrixExpParser();
			parser.setExpression(expression_array[i]);
			String resultExpression = parser.getResultExpression();
			Type type = parser.getType();
			int position = parser.getPosition();
			int[] positions = parser.getNodePosition();
			Operation operation = parser.getOperation();
			processOne(resultExpression, type, position, positions, operation,
					nodes);
			// this.setMatrix(matrix);
		}
		return this;
	}

	public Object[] complie(String exp)
	{
		Object[] v = new Object[5];
		MatrixExpParser parser = new MatrixExpParser();
		parser.setExpression(exp);
		v[0] = parser.getResultExpression();
		v[1] = parser.getType();
		v[2] = new Integer(parser.getPosition());
		v[3] = parser.getNodePosition();
		v[4] = parser.getOperation();
		return v;
	}

	public IMatrix process(List exps)
	{
		List nodes = new ArrayList();
		for (int i = 0; i < exps.size(); i++)
		{
			Object[] exp = (Object[]) exps.get(i);
			processOne((String) exp[0], (Type) exp[1], ((Integer) exp[2])
					.intValue(), (int[]) exp[3], (Operation) exp[4], nodes);
		}
		return this;
	}

	/**
	 * 
	 */
	private IMatrix processOne(String resultExpression, Type type,
			int position, int[] positions, Operation operation, List nodes)
	{
		// IMatrix nrd = new ListMatrix();
		// IMatrix nrd = this.matrix;
		// System.out.println("processOne.in:" + System.currentTimeMillis());
		if (type == Type.ROW || type == Type.COL) return computeRowCol(
				resultExpression, operation, position, nodes, type);
		// parser.getPosition(), parser.getOperation()
		else if (type == Type.NODE) return computeNode(resultExpression,
				positions);
		// parser.getNodePosition());
		return this;
	}

	/**
	 * split expression into multi online express, like: <sql>r1=r2+1 \n
	 * c3+=c1+c2 \n n0_1="new"</sql> --> {"r1=r2+1", "c3+=c1+c2",
	 * "n0_1=\"new\""}
	 */
	private String[] parseExpress(String expression, String delim)
	{
		String s = expression.trim();
		String[] rt = StringX.split(s, delim);
		for (int i = 0; i < rt.length; i++)
			rt[i] = rt[i].trim();
		return rt;
	}

	private boolean hasError(String expression)
	{
		if (StringX.nullity(expression))
		{
			System.err.println("IMatrixExpression.process (..): error input.");
			return true;
		}
		return false;
	}

	private IMatrix computeRowCol(String resultExpression, Operation operation,
			int position, List nodes, Type type)
	{
		// System.out.println("computeRowCol:"+System.currentTimeMillis());
		List nnodes = getResultNodes(resultExpression, nodes, type);
		if (nnodes == null)
		{
			System.err.println("IMatrixExpression: error compute result.");
			return this;
		}
		// IMatrix nrd = (IMatrix) clone();
		// IMatrix nrd = this.matrix;
		if (Type.COL == type)
		{
			if (Operation.INSERT == operation)
			// insert column
			insertCol(position, nnodes);
			else if (Operation.REPLACE == operation)
			// replace column
			replaceCol(position, nnodes);
		}
		else if (Type.ROW == type)
		{
			if (Operation.INSERT == operation)
			// insert row
			insertRow(position, nnodes);
			else if (Operation.REPLACE == operation)
			// replace row
			replaceRow(position, nnodes);
		} // if
		// System.out.println("computeRowCol:over.."+System.currentTimeMillis());
		return this;
	}

	/**
	 * change this.rd
	 */
	private IMatrix computeNode(String resultExpression, int[] position)
	{
		// System.out.println("computeNode.in:" + System.currentTimeMillis());
		int row = position[0];
		int col = position[1];
		if (row == -1 || col == -1) return this;
		if (isString(resultExpression))
		{
			// get setting string node
			// String s_rt = StringX.between(resultExpression, "\"", "\"");
			setCell(row, col, StringX.between(resultExpression, "\"", "\""));
		}
		else
		{
			// get computed number node
			MatrixParameter rdparam = new MatrixParameter(this);
			ExpressionParser parser = new ExpressionParser(rdparam);
			CompiledExpression compiled = null;
			try
			{
				compiled = parser.compileExpression(resultExpression);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			double n_rt = compiled.computeExpression();
			setCell(row, col, new Double(n_rt));
		} // if
		return this;
	}

	/**
	 * get col/row compute result nodes array.
	 */
	private List getResultNodes(String resultExpression, List nodes, Type type)
	{
		nodes.clear();
		if (StringX.nullity(resultExpression))
		{
			System.err.println("IMatrixExpression: result expression is null.");
			return null;
		}
		if (isSumAvg(resultExpression)) return getSumAvg(resultExpression,
				nodes, type);
		if (isString(resultExpression)) return getString(resultExpression,
				nodes, type);
		MatrixParameter rdparam = new MatrixParameter(this);
		ExpressionParser parser = new ExpressionParser(rdparam);
		// ExpressionNode a;
		CompiledExpression compiled = null;
		try
		{
			compiled = parser.compileExpression(resultExpression);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (type != rdparam.getType())
		{
			System.err
					.println("IMatrixExpression.compute (..): expression left and right is NOT compatible");
			return nodes;
		}
		int size = rdparam.getSize();
		// Node[] nodes = new Node[size];
		for (int i = 0; i < size; i++)
		{
			// System.out.println(rdparam.)
			rdparam.next();
			try
			{
				// double rt =
				// compiled.computeExpression(MatrixInterpreter.getInstance());
				nodes.add(compiled.computeExpression(MatrixInterpreter
						.getInstance()));
			}
			catch (IllegalArgumentException iae)
			{
				iae.printStackTrace();
				nodes.add(null);
			}
			// nodes[i] = new Node(rt);
		}
		return nodes;
	}

	/**
	 * get col/row compute result nodes array without checking type.
	 * 
	 * @author LiJian 2003-12-19
	 */
	/*
	 * public List getResult(String resultExpression, List nodes, Type type) {
	 * nodes.clear(); if (StringX.nullity(resultExpression)) {
	 * System.err.println("IMatrixExpression: result expression is null.");
	 * return null; } if (isSumAvg(resultExpression)) return
	 * getSumAvg(resultExpression, nodes, type); if (isString(resultExpression))
	 * return getString(resultExpression, nodes, type); MatrixParameter rdparam =
	 * new MatrixParameter(matrix); ExpressionParser parser = new
	 * ExpressionParser(rdparam); CompiledExpression compiled = null; try {
	 * compiled = parser.compileExpression(resultExpression); } catch (Exception
	 * e) { e.printStackTrace(); } int size; if (rdparam.getType() == Type.NODE)
	 * size = 1; else size = rdparam.getSize(); // Node[] nodes = new
	 * Node[size]; for (int i = 0; i < size; i++) { rdparam.next(); double rt =
	 * compiled.computeExpression(); nodes.add(new Double(rt)); // nodes[i] =
	 * new Node(rt); } return nodes; } // getResult ()
	 */
	/**
	 * "= SUM", "=Avg"
	 */
	private boolean isSumAvg(String resultExpression)
	{
		// pattern to match left part like: "= SUM", "=sum", "= AvG"
		Pattern p = new Pattern("\\s*=\\s*(sum|avg)\\s*", REFlags.IGNORE_CASE);
		Matcher m = p.matcher(resultExpression);
		return m.matches();
	} // isSumAvg ()

	/**
	 * String setting. (= "abc"), (="a","b","c")
	 */
	private boolean isString(String string)
	{
		// pattern to match left part like: "= SUM", "=sum", "= AvG"
		Pattern p = new Pattern("\\s*=\\s*\".*\"\\s*", REFlags.IGNORE_CASE);
		Matcher m = p.matcher(string);
		return m.matches();
	}

	// 获得行，列的平均值或者是总和
	// added by spc 2008-04-25. 增加sum，avg的参数，容许有行，列作为参数
	private List getSumAvg(String resultExpression, List nodes, Type type)
	{
		nodes.clear();
		Pattern p = new Pattern("(sum|avg)", REFlags.IGNORE_CASE);
		Matcher m = p.matcher(resultExpression);
		String sumavg = null;
		if (!m.find()) return nodes;
		else sumavg = m.toString().trim().toLowerCase();
		String strLines = StringX.between(sumavg, "(", ")");
		int[] lines = null;
		if (!StringX.nullity(strLines)) lines = StringX.split2ints(strLines,
				",");

		if (sumavg.startsWith("sum"))
		{
			if (type == Type.COL)
			// sum cols
			nodes = lines == null ? sumCol(0, getWidth()) : sumCol(lines);
			else if (type == Type.ROW)
			// sum rows
			nodes = lines == null ? sumRow(0, getHeight()) : sumRow(lines);
		}
		else if (sumavg.startsWith("avg"))
		{
			if (type == Type.COL)
			// avg cols
			nodes = lines == null ? averageCol(0, getWidth())
					: averageCol(lines);
			else if (type == Type.ROW)
			// avg rows
			nodes = lines == null ? averageRow(0, getHeight())
					: averageRow(lines);
		}
		return nodes;
	} // getSumAvg ()

	// c1=a,b,c,d or r2=m,d,s,k
	private List getString(String resultExpression, List nodes, Type type)
	{
		nodes.clear();
		int length = 0;
		if (type == Type.COL) length = getHeight();
		else if (type == Type.ROW) length = getWidth();

		// List new_nodes = new ArrayList();
		String strs = StringX.right(resultExpression, "=");
		String[] str_List = StringX.split(strs, ",");
		int n = str_List.length;
		if (n > length) n = length;
		for (int i = 0; i < n; i++)
			nodes.add(StringX.between(str_List[i], "\"", "\""));
		// new_nodes[i] = value;
		return nodes;
	} // getString ()
}
