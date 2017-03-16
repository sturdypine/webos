package spc.webos.util.date;

//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.Date;

public interface DateParser
{
	Date parse(String str) throws DateParseException;

	// public DateFormat getFormat(String pattern)
	// {
	// return null;
	// }
	//	
	// public static String yyyyMMdd = "yyyy-MM-dd";
	// public static SimpleDateFormat f1 = new SimpleDateFormat(yyyyMMdd);
	// public static String yyyyMMdd = "yyyy-MM-dd";
	// public static SimpleDateFormat f1 = new SimpleDateFormat(yyyyMMdd);
}
