package spc.webos.persistence.jdbc.rowtype;

import java.util.ArrayList;

public class RowList extends ArrayList
{
	private static final long serialVersionUID = 1L;
	String[] columnName;

	public String[] getColumnName()
	{
		return columnName;
	}

	public RowList(String[] columnName, int columnCount)
	{
		super(columnCount);
		this.columnName = columnName;
	}

	public StringBuffer toJson()
	{
		StringBuffer row = new StringBuffer(64);
		row.append('{');

		for (int i = 0; i < size(); i++)
		{
			Object v = get(i);
			if (v == null) continue;
			if (row.length() > 2) row.append(',');
			row.append(columnName[i].toLowerCase());
			row.append(":'");
			String str = v.toString();
			if (str.indexOf('\'') >= 0) str = str.replace("'", "\\'");
			if (str.indexOf('\n') >= 0) str = str.replace("\n", "\\n");
			row.append(str);
			row.append('\'');
		}
		row.append('}');
		return row;
	}
}
