package spc.webos.persistence.matrix;

import java.util.ArrayList;
import java.util.List;

public class ListMatrix extends AbstractMatrix
{
	List data;

	public ListMatrix()
	{
	}

	public ListMatrix(List data)
	{
		this.data = data;
	}

	public ListMatrix(int width, int height)
	{
		allocate(width, height);
	}

	public int getHeight()
	{
		return data.size();
	}

	public int getWidth()
	{
		if (data == null || data.size() == 0) return 0;
		List row = (List) data.get(0);
		return row.size();
	}

	public void setCell(int r, int c, Object node)
	{
		List row = (List) data.get(r);
		row.set(c, node);
	}

	public Object getCell(int r, int c)
	{
		List row = (List) data.get(r);
		return row.get(c);
	}

	public void allocate(int width, int height)
	{
		if (data == null) data = new ArrayList();
		else data.clear();
		for (int i = 0; i < height; i++)
		{
			List row = new ArrayList();
			for (int j = 0; j < width; j++)
				row.add(null);
			data.add(row);
		}
	}

	public void expandRow(int rows)
	{
		if (rows <= 0) return;
		int w = getWidth();
		for (int i = 0; i < rows; i++)
		{
			List row = new ArrayList();
			for (int j = 0; j < w; j++)
				row.add(null);
			data.add(row);
		}
	}

	public void expandCol(int cols)
	{
		if (cols <= 0) return;
		int h = getHeight();
		for (int i = 0; i < h; i++)
		{
			List row = (List) data.get(i);
			for (int j = 0; j < cols; j++)
				row.add(null);
		}
	}

	public Object clone()
	{
		int w = this.getWidth();
		int h = this.getHeight();
		ListMatrix m = new ListMatrix(w, h);
		m.replace(this, 0, 0, h, w, 0, 0);
		return m;
	}

	public void moveCol(int start, int step)
	{
		expandCol(step);
		int w = getWidth();
		int h = getHeight();
		for (int i = 0; i < h; i++)
		{
			Object lastColItem = ((List) data.get(i)).get(w - 1);
			for (int j = (w - step - 1); j >= start; j--)
				((List) data.get(i)).set((j + step), ((List) data.get(i))
						.get(j));
			((List) data.get(i)).set(start, lastColItem);
		}
	}

	public void moveRow(int start, int step)
	{
		expandRow(step);
		Object lastRow = data.get(data.size() - 1);
		for (int i = data.size() - step - 1; i >= start; i--)
			data.set((i + step), data.get(i));
		data.set(start, lastRow);
	}

	/**
	 * @add by sunqian
	 */
	public IMatrix create(int rows, int cols)
	{
		ListMatrix matrix = new ListMatrix(rows, cols);
		return matrix;
	}

	/**
	 * @add by sunqian return the col which is selected
	 */
	public Object getCol(int col)
	{
		List colList = new ArrayList();
		int h = getHeight();
		for (int i = 0; i < h; i++)
			colList.add(((List) data.get(i)).get(col));
		return colList;
	}

	/**
	 * @add by sunqian return the row which is selected
	 */
	public Object getRow(int row)
	{
		return data.get(row);
	}
}
