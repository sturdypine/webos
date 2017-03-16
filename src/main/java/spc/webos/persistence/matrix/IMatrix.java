package spc.webos.persistence.matrix;

import java.util.List;
import java.util.Map;

public interface IMatrix extends java.lang.Cloneable
{
	void setCell(int row, int col, Object node);

	Object getCell(int row, int col);

	int getWidth();

	int getHeight();

	void allocate(int width, int height);

	IMatrix create(int rows, int cols);

	Object getRow(int row);

	Object getCol(int col);

	IMatrix rightConnect(IMatrix targetM);

	IMatrix bottomConnect(IMatrix targetM);

	/**
	 * 将矩阵以某行开始，向下移动几行
	 * 
	 * @param start
	 * @param step
	 */
	void moveRow(int start, int step);

	/**
	 * 将矩阵以某列开始，向右移动几列
	 * 
	 * @param start
	 * @param step
	 */
	void moveCol(int start, int step);

	void expandRow(int rows);

	void expandCol(int cols);

	Object clone();

	/**
	 * C = A + B
	 * 
	 * @param B
	 *            another IMatrix
	 * @return C, it is A + B.
	 */
	IMatrix add(IMatrix b);

	/**
	 * Add a scalar to each NUMBER element of a RawData, C = A + s
	 * 
	 * @param s
	 *            double
	 * @return C, it is A + s.
	 */
	IMatrix add(double s);

	/**
	 * C = A - B
	 * 
	 * @param B
	 *            another RawData
	 * @return C, it is A - B.
	 */
	IMatrix subtract(IMatrix b);

	/**
	 * Unary minus
	 * 
	 * @return -A
	 */
	IMatrix uminus();

	/**
	 * C = A * B
	 * 
	 * @param B,
	 *            another RawData
	 * @return C, A * B
	 */
	IMatrix multiply(IMatrix B);

	/**
	 * multiply a scalar to each NUMBER element of a RawData, C = A * s
	 * 
	 * @param s
	 *            double
	 * @return C a RawData
	 */
	IMatrix multiply(double s);

	/**
	 * C = A / B
	 * 
	 * @param B
	 *            another RawData
	 * @return C a RawData
	 */
	IMatrix divide(IMatrix B);

	/**
	 * each NUMBER element of a RawData divide a scalar, C = A / s
	 * 
	 * @param s
	 *            double
	 * @return C, a RawData
	 */
	IMatrix divide(double s);

	/**
	 * Insert a row into rawdata before given position. Precondition: row.length ==
	 * rd.getWidth ()
	 * 
	 * @param row
	 *            New row to insert.
	 * @return New rawdata after been inserted.
	 */
	IMatrix insertRow(int row0, Object row);

	/**
	 * Insert a column into rawdata before given position. Precondition:
	 * col.length == rd.getHeight ()
	 * 
	 * @param col0
	 *            Index of column to insert, if col0 == 0, insert as first col;
	 *            if col0 == w, insert as last col.
	 * @param col
	 *            New column to insert.
	 * @param title
	 *            Title of new column.
	 * @return New rawdata after been inserted.
	 */
	IMatrix insertCol(int col0, Object col);

	/**
	 * replace given row with new nodes[] row. Precondition: row.length ==
	 * rd.getWidth ()
	 * 
	 * @param row
	 *            New row to replace.
	 * @return New rawdata after been replaced.
	 */
	IMatrix replaceRow(int row0, Object row);

	/**
	 * replace given col with new nodes[] col. Precondition: col.length ==
	 * rd.getHeight ()
	 * 
	 * @param col0
	 *            Index of column to replace,
	 * @param col
	 *            New column to replace.
	 * @param title
	 *            Title of new column, if title == null, keep original.
	 * @return New rawdata after been replaced.
	 */
	IMatrix replaceCol(int col0, Object col);

	/**
	 * 用指定的matrix的指定的区域替换自己制定的区域
	 * 
	 * @param target
	 * @param tr0
	 * @param tc0
	 * @param tr1
	 * @param tc1
	 * @param r0
	 * @param c0
	 * @return
	 */
	IMatrix replace(IMatrix target, int tr0, int tc0, int tr1, int tc1, int r0,
			int c0);

	/**
	 * remove given row.
	 * 
	 * @param row
	 *            Index of row to remove.
	 * @return New rawdata after been replaced.
	 */
	IMatrix removeRow(int row);

	IMatrix removeCol(int col);

	/**
	 * Get sum row of given rows in rawdata.
	 * 
	 * @param row0
	 *            Index of row from which begin calculate.
	 * @param rows
	 *            Number of rows in calculation range.
	 * @return Row with sum values.
	 */
	List sumRow(int row0, int rows);

	List sumRow(int[] rows);

	/**
	 * Get sum column of given columns in rawdata.
	 * 
	 * @param col0
	 *            Index of column from which begin calculate.
	 * @param cols
	 *            Number of columns in calculation range.
	 * @return Column with sum values.
	 */
	List sumCol(int col0, int cols);

	List sumCol(int[] cols);

	/**
	 * Get average row of given rows in rawdata.
	 * 
	 * @param row0
	 *            Index of row from which begin calculate.
	 * @param rows
	 *            Number of rows in calculation range.
	 * @return Row with average values.
	 */
	List averageRow(int row0, int rows);

	List averageRow(int[] rows);

	/**
	 * Get average column of given columns in rawdata.
	 * 
	 * @param col0
	 *            Index of column from which begin calculate.
	 * @param cols
	 *            Number of columns in calculation range.
	 * @return Column with average values;
	 */
	List averageCol(int col0, int cols);

	List averageCol(int[] cols);

	/**
	 * merge two RawData by regulation
	 * 
	 * @param rightRD
	 *            the RawData to be merged that is right of the result RawData
	 * @param sltLeftCols
	 *            the columns' name selected from leftRD
	 * @param sltRightCols
	 *            the columns' name selected from rightRD
	 * @param regulation
	 *            the Regulation of merging leftRD and rightRD the key of the
	 *            Map is String: that contain a column from leftRD and a column
	 *            from rightRD,compare them with the regulation1, if return
	 *            true--merge the row, false--skip; the value of the Map is
	 *            String: that contain the name of regulation,the factory of
	 *            regulation return different regulation according to the name
	 *            Example: ┌──────────┬─────────┐ │leftcol1=rightcol1
	 *            │regulation1 │ ├──────────┼─────────┤ │leftcol2=rightcol2
	 *            │regulation1 │ └──────────┴─────────┘
	 * 
	 * @return the result RawData of merging leftRD and rightRD
	 */
	IMatrix merge(IMatrix rightRD, String[] sltLeftCols, String[] sltRightCols,
			Map regulation);

	/**
	 * merge two RawData by regulation
	 * 
	 * @param rightRD
	 *            the RawData to be merged that is right of the result RawData
	 * @param sltLeftCols
	 *            the columns' index selected from leftRD
	 * @param sltRightCols
	 *            the columns' index selected from rightRD
	 * @param regulation
	 *            the Regulation of merging leftRD and rightRD the key of the
	 *            Map is String: that contain a column Index from leftRD and a
	 *            column Index from rightRD,compare them with the regulation1,
	 *            if return true--merge the row, false--skip; the value of the
	 *            Map is String: that contain the name of regulation,the factory
	 *            of regulation return different regulation according to the
	 *            name Example: ┌───────────────┬─────────┐
	 *            │leftcolIndex1=rightcolIndex1 │regulation1 │
	 *            ├───────────────┼─────────┤ │leftcolIndex2=rightcolIndex2
	 *            │regulation1 │ └───────────────┴─────────┘
	 * 
	 * @return the result RawData of merging leftRD and rightRD
	 */
	IMatrix merge(IMatrix rightRD, int[] sltLeftCols, int[] sltRightCols,
			Map regulation);

	/**
	 * eg.<br>
	 * 
	 * <pre>
	 * 	A 		B		C
	 *  1		1.0		2.0
	 *  2		3.0		2.3
	 * 	3		4.3		4.5
	 * </pre>
	 * 
	 * @return rd:<br>
	 * 
	 * <pre>
	 *  A	1		2		3
	 *  B	1.0		3.0		4.3
	 * 	C	2.0		2.3		4.5
	 * </pre>
	 * 
	 * @see TestRawDataUtil
	 */
	IMatrix rotate();

	/**
	 * select those nodes rows and columns in given rawdata which matcher the
	 * condition.
	 * 
	 * @param colNames
	 *            the columns you want to select.
	 * @param condition
	 *            the limit for columns. the key of the Map is a String the
	 *            value of the Map is a List Example: ┌───┬─────────┐ │地区
	 *            │北京,上海,广州... │ ├───┼─────────┤ │年 │1999，2000... │
	 *            └───┴─────────┘
	 * 
	 * @return a subset of rawData satisfied with the condition.
	 */
	IMatrix select(int[] cols, Map condition);

	/**
	 * select those nodes columns in given rawdata
	 * 
	 * @param rawData
	 * @param cols
	 *            the index of columns.
	 * @return a new rawData include the columns.
	 */
	IMatrix selectCols(int[] cols);

	List selectCol(int colIndex);

	List selectRow(int rowIndex);

	/**
	 * select those nodes rows in given rawdata which matcher the condition.
	 * null condition means no limit for rawData
	 * 
	 * @param rawData
	 * @param condition
	 *            the limit for columns. the key of the Map is a String the
	 *            value of the Map is a List Example: ┌───┬─────────┐ │地区
	 *            │北京,上海,广州... │ ├───┼─────────┤ │年 │1999，2000... │
	 *            └───┴─────────┘
	 * 
	 * @return the rows of rawData satisfied with the condition.
	 */
	IMatrix selectRows(Map condition);

	// -- 表达式计算
	IMatrix process(String expression, String delim);
}
