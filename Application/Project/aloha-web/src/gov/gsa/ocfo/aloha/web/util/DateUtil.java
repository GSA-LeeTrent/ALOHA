package gov.gsa.ocfo.aloha.web.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
	public interface DateFormats {
		public static final String YYYYMMDD = "yyyyMMdd";		
		public static final String DEFAULT_FORMAT = "MM/dd/yyyy";
	}
	
	public static Date convertStringToDate(String val, String format) throws ParseException {
		DateFormat df = new SimpleDateFormat(format);
		return df.parse(val);
	}
	public static String formatDate(Date dateVal, String format) {
		return new SimpleDateFormat(format).format(dateVal);
	}
	public static String formatDate(Date dateVal) {
		return new SimpleDateFormat(DateFormats.DEFAULT_FORMAT).format(dateVal);
	}	
	public static Date getNextRefreshDateTime() {
		Date now = new Date();
		GregorianCalendar tomorrowAt2AM = new GregorianCalendar();
		tomorrowAt2AM.setTime(now);
		tomorrowAt2AM.add(Calendar.DAY_OF_MONTH, 1);
		tomorrowAt2AM.set(Calendar.HOUR_OF_DAY, 2);
		tomorrowAt2AM.set(Calendar.MINUTE, 0);
		tomorrowAt2AM.set(Calendar.SECOND, 0);

		return tomorrowAt2AM.getTime();
	}	
}
