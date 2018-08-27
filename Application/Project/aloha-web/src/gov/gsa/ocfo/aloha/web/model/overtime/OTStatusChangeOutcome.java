package gov.gsa.ocfo.aloha.web.model.overtime;

import java.io.Serializable;

public class OTStatusChangeOutcome implements Serializable {
	private static final long serialVersionUID = -4145888083332635005L;

	private final String statusCode;
	private final String confirmMsg;
	
	public OTStatusChangeOutcome(String code, String msg) {
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
