package gov.gsa.ocfo.aloha.web.util;

import java.math.BigDecimal;

public class NumberUtil {

	public static boolean isBigDecimal(String val) {
		try {
			new BigDecimal(val);
			return true;
		} catch(NumberFormatException nfe) {
			return false;
		}
	}
}
