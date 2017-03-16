package spc.webos.persistence.jdbc.converter;

import spc.webos.persistence.jdbc.IColumnConverter;

public abstract class AbstractColumnConverter implements IColumnConverter
{
	String name;

	public void init()
	{
		if (name != null) COLUMN_CONVERTER.put(name, this);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
