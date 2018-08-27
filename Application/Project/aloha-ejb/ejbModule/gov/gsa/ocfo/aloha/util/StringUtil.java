package gov.gsa.ocfo.aloha.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
	public static boolean isNullOrEmpty(String val) {
		return ( (val == null) || (val.trim().length() == 0) );
	}
	public static String toProperCase(String val) {
		if ( val != null && val.trim().length() > 1 ) {
			return val.substring(0, 1) + (val.substring(1)).toLowerCase();
		} else {
			return val;
		}
	}
	public static String buildFullName(String firstName, String middleName, String lastName) {
		StringBuilder strBldr = new StringBuilder();
		if ( !StringUtil.isNullOrEmpty(firstName)) {
			strBldr.append(firstName);
		}
		if ( !StringUtil.isNullOrEmpty(middleName)) {
			strBldr.append(Punctuation.SPACE);
			strBldr.append(middleName.substring(0, 1));
		}
		if ( !StringUtil.isNullOrEmpty(lastName)) {
			strBldr.append(Punctuation.SPACE);			
			strBldr.append(lastName);
		}
		return strBldr.toString();
	}
	public static String buildDateRange(Date startDate, Date endDate, String dateFormat) {
		StringBuilder strBldr = new StringBuilder();		
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		strBldr.append(sdf.format(startDate));
		strBldr.append(" - ");
		strBldr.append(sdf.format(endDate));
		return strBldr.toString();
	}	
}
