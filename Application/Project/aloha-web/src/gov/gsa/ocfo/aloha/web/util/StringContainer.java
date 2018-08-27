package gov.gsa.ocfo.aloha.web.util;

import java.io.Serializable;

public class StringContainer implements Serializable {
	private static final long serialVersionUID = -3285093976240019500L;
	private String str;

	public StringContainer(String str) {
		this.str = str;
	}
	public String toString() {
		return str;
	}
	public String getClazz() {
		return "String";
	}
}