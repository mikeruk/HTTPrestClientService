package demo1.httprestclientservice.exceptions;

/**
 * Thrown for any 5xx (server) HTTP status.
 */
public class DownstreamServiceException extends RuntimeException {
    public DownstreamServiceException() {
        super();
    }
    public DownstreamServiceException(String message) {
        super(message);
    }
    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public DownstreamServiceException(Throwable cause) {
        super(cause);
    }
}
