package gov.gsa.ocfo.aloha.util;

import java.util.HashMap;
import java.util.Map;

public class DayUtil {

	private static final Map<String, String> abbreviationMap = initAbbreviationsMap();
	
	private static Map<String, String>initAbbreviationsMap() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("SUNDAY", "SUN");        
		map.put("MONDAY","MON");        
		map.put("TUESDAY","TUE");       
		map.put("WEDNESDAY","WED");
		map.put("THURSDAY","THU");      
		map.put("FRIDAY","FRI");        
		map.put("SATURDAY","SAT");      
		return map;
	}
	public static String getAbbreviation(String dayOfWeek) {
		return abbreviationMap.get(dayOfWeek);
	}
}
