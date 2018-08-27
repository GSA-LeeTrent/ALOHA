package gov.gsa.ocfo.aloha.exception;

public class AlohaServerException extends Exception {
	private static final long serialVersionUID = -1834779098450501443L;

	public enum ExceptionType {
		GENERIC, OPTIMISTIC_LOCK, EMAIL_FAILURE
	}
	private ExceptionType exceptionType;
	
	public AlohaServerException(String msg) {
		super(msg);
	}
	
	public AlohaServerException(Throwable t) {
		super(t);
		this.exceptionType = ExceptionType.GENERIC;
	}

	public AlohaServerException(Throwable t, ExceptionType exceptionType) {
		super(t);
		this.exceptionType = exceptionType;
	}

	public ExceptionType getExceptionType() {
		return exceptionType;
	}
}
