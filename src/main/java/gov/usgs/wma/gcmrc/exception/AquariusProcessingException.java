package gov.usgs.wma.gcmrc.exception;

public class AquariusProcessingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AquariusProcessingException(String message) {
		super(message);
	}
}