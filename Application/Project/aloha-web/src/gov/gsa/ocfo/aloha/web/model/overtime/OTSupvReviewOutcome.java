package gov.gsa.ocfo.aloha.web.model.overtime;

public class OTSupvReviewOutcome {
	private final String statusCode;
	private final String confirmMsg;
	
	public OTSupvReviewOutcome(String code, String msg) {
		this.statusCode = code;
		this.confirmMsg = msg;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getConfirmMsg() {
		return confirmMsg;
	}
}
