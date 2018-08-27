package gov.gsa.ocfo.aloha.web.enums;

public enum OTMode {
	SUBMIT_OWN("1"),
	ON_BEHALF_OF("2"),
	APPROVER("3");

	private String text;

	OTMode(String text) {
	    this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static OTMode fromString(String text) {
		if (text != null) {
			for (OTMode b : OTMode.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
	  }
	    return null;
	}
}