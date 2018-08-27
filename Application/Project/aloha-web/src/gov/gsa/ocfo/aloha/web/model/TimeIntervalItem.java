package gov.gsa.ocfo.aloha.web.model;

import java.io.Serializable;

public class TimeIntervalItem implements Serializable {
	private static final long serialVersionUID = 3196495051286376565L;

	private String label;
	private String value;
	
	public TimeIntervalItem(String hour24, String hour12, String minute, String meridiem) {
		this.value = hour24 + minute;
		this.label = hour12 + ":" + minute + " " + meridiem;
	}

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}	
}