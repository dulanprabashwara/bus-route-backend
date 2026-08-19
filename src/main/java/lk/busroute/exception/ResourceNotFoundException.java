package lk.busroute.exception;

/**
 * Exception for when a requested resource (stop, route, journey, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String errorCode;

    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
