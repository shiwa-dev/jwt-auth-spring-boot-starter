package dev.shiwa.jwtstarter.core.error;

public class JwtAuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final JwtErrorCode code;

    public JwtAuthException(JwtErrorCode code, String message) {
	super(message);
	this.code = code;
    }

    public JwtErrorCode getCode() {
	return code;
    }
}