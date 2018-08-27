package gov.gsa.ocfo.aloha.web.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TimeIntervalGroup implements Serializable  {
	private static final long serialVersionUID = -526992680318384677L;
	
	private static final String DELIMETER = ":";
	private static final String SPACE = " ";
	private static final String AM = "AM";
	private static final String PM = "PM";
	
	private static final Map<Integer, String> HOUR_12_MAP = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 3414880089396169558L;
	{
		   put(Integer.valueOf(0), "12");
		   put(Integer.valueOf(1), "1");
		   put(Integer.valueOf(2), "2");
		   put(Integer.valueOf(3), "3");
		   put(Integer.valueOf(4), "4");
		   put(Integer.valueOf(5), "5");
		   put(Integer.valueOf(6), "6");
		   put(Integer.valueOf(7), "7");
		   put(Integer.valueOf(8), "8");
		   put(Integer.valueOf(9), "9");
		   put(Integer.valueOf(10), "10");
		   put(Integer.valueOf(11), "11");
		   put(Integer.valueOf(12), "12");
		   put(Integer.valueOf(13), "1");
		   put(Integer.valueOf(14), "2");
		   put(Integer.valueOf(15), "3");
		   put(Integer.valueOf(16), "4");
		   put(Integer.valueOf(17), "5");
		   put(Integer.valueOf(18), "6");
		   put(Integer.valueOf(19), "7");
		   put(Integer.valueOf(20), "8");
		   put(Integer.valueOf(21), "9");
		   put(Integer.valueOf(22), "10");
		   put(Integer.valueOf(23), "11");
		}};
		private static final Map<Integer, String> HOUR_24_MAP = new HashMap<Integer, String>() {
			private static final long serialVersionUID = -850706063331009769L;
			{
			   put(Integer.valueOf(0), "00");
			   put(Integer.valueOf(1), "01");
			   put(Integer.valueOf(2), "02");
			   put(Integer.valueOf(3), "03");
			   put(Integer.valueOf(4), "04");
			   put(Integer.valueOf(5), "05");
			   put(Integer.valueOf(6), "06");
			   put(Integer.valueOf(7), "07");
			   put(Integer.valueOf(8), "08");
			   put(Integer.valueOf(9), "09");
			   put(Integer.valueOf(10), "10");
			   put(Integer.valueOf(11), "11");
			   put(Integer.valueOf(12), "12");
			   put(Integer.valueOf(13), "13");
			   put(Integer.valueOf(14), "14");
			   put(Integer.valueOf(15), "15");
			   put(Integer.valueOf(16), "16");
			   put(Integer.valueOf(17), "17");
			   put(Integer.valueOf(18), "18");
			   put(Integer.valueOf(19), "19");
			   put(Integer.valueOf(20), "20");
			   put(Integer.valueOf(21), "21");
			   put(Integer.valueOf(22), "22");
			   put(Integer.valueOf(23), "23");
			}};	
		private static final Map<Integer, String> MERIDIEM_MAP = new HashMap<Integer, String>() {
			private static final long serialVersionUID = -850706063331009769L;

		{
			   put(Integer.valueOf(0), AM);
			   put(Integer.valueOf(1), AM);
			   put(Integer.valueOf(2), AM);
			   put(Integer.valueOf(3), AM);
			   put(Integer.valueOf(4), AM);
			   put(Integer.valueOf(5), AM);
			   put(Integer.valueOf(6), AM);
			   put(Integer.valueOf(7), AM);
			   put(Integer.valueOf(8), AM);
			   put(Integer.valueOf(9), AM);
			   put(Integer.valueOf(10), AM);
			   put(Integer.valueOf(11), AM);
			   put(Integer.valueOf(12), PM);
			   put(Integer.valueOf(13), PM);
			   put(Integer.valueOf(14), PM);
			   put(Integer.valueOf(15), PM);
			   put(Integer.valueOf(16), PM);
			   put(Integer.valueOf(17), PM);
			   put(Integer.valueOf(18), PM);
			   put(Integer.valueOf(19), PM);
			   put(Integer.valueOf(20), PM);
			   put(Integer.valueOf(21), PM);
			   put(Integer.valueOf(22), PM);
			   put(Integer.valueOf(23), PM);
			}};

		private static final String MINUTE_INTERVALS[] = {"00","06","12","15","18","24","30","36","42","45","48","54"};
		
		private String label;
		private String description;
		private TimeIntervalItem[] items = new TimeIntervalItem[MINUTE_INTERVALS.length];
		
		public TimeIntervalGroup(int hour) {
			String hour12 = HOUR_12_MAP.get(Integer.valueOf(hour));
			String hour24 = HOUR_24_MAP.get(Integer.valueOf(hour));
			String meridiem = MERIDIEM_MAP.get(Integer.valueOf(hour));
			
			StringBuilder sb = new StringBuilder();
			sb.append(HOUR_12_MAP.get(Integer.valueOf(hour)));
			sb.append(SPACE);
			sb.append(meridiem);
			sb.append(DELIMETER);
			
			this.label = sb.toString();
			this.description = this.label;
			
			for (int ii=0; ii < MINUTE_INTERVALS.length; ii++ ) {
				this.items[ii] = new TimeIntervalItem(hour24, hour12, MINUTE_INTERVALS[ii], meridiem);
			}
		}

		public String getLabel() {
			return label;
		}

		public String getDescription() {
			return description;
		}

		public TimeIntervalItem[] getItems() {
			return items;
		}
}