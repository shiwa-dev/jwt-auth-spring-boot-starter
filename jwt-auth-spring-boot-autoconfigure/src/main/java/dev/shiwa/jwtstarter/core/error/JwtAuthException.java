package dev.shiwa.jwtstarter.core.error;

public class JwtAuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final JwtErrorCode errorCode;

    public JwtAuthException(JwtErrorCode errorCode, String message) {
	super(message);
	this.errorCode = errorCode;
    }

    public JwtErrorCode getErrorCode() {
	return errorCode;
    }
}