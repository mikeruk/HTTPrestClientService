package demo1.httprestclientservice.exceptions;


/**
 * Thrown for any other 4xx (client) HTTP status.
 */
public class ClientErrorException extends RuntimeException {
    public ClientErrorException() {
        super();
    }
    public ClientErrorException(String message) {
        super(message);
    }
    public ClientErrorException(String message, Throwable cause) {
        super(message, cause);
    }
    public ClientErrorException(Throwable cause) {
        super(cause);
    }
}

