package gov.usgs.wma.gcmrc.exception;

public class AquariusRetrievalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AquariusRetrievalException(String message) {
        super(message);
    }
}