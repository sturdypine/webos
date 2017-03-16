package spc.webos.util.date;

public class DateParseException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DateParseException(String message)
	{
		super(message);
	}

	public DateParseException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
