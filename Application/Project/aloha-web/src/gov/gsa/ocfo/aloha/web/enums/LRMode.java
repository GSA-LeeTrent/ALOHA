package gov.gsa.ocfo.aloha.web.enums;

public enum LRMode {
	SUBMIT_OWN("1"),
	ON_BEHALF_OF("2"),
	APPROVER("3");

	private String text;

	LRMode(String text) {
	    this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static LRMode fromString(String text) {
		if (text != null) {
			for (LRMode b : LRMode.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
	  }
	    return null;
	}
}