package spc.webos.persistence.jdbc.namedparam;

import java.util.Map;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;

public class MapSqlParameterSource extends AbstractSqlParameterSource
{
	private final Map values;

	public MapSqlParameterSource(Map values)
	{
		this.values = values;
	}

	public boolean hasValue(String paramName)
	{
		return true;
	}

	public Object getValue(String paramName) throws IllegalArgumentException
	{
		return values.get(paramName);
	}
}
