package gov.gsa.ocfo.aloha.exception;

import java.util.ArrayList;
import java.util.List;

public class AlohaValidationException extends Exception {
	private static final long serialVersionUID = -7869395095248289486L;

	private List<String> errorMessages = new ArrayList<String>();
	
	public void addErrorMessage(String msg) {
		this.errorMessages.add(msg);
	}
	public boolean hasErrorMessages() {
		return (this.errorMessages.size() > 0 );
	}
	public List<String> getErrorMessages() {
		return errorMessages;
	}
}
