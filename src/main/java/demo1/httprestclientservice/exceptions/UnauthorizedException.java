package demo1.httprestclientservice.exceptions;



/**
 * Thrown when the RestClient receives a 401 Not Found from the backend.
 *
 * NB! Do not annotate it with: @ResponseStatus(CustomHttpStatus.NOT_FOUND), because if you do,
 * then the custom error message defined in the RestClient.builder() will be overwritten by the annotation;
 * The end client Postman will not get anything about the 401,
 * instead postman client will receive generic 500 "internal error"
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super();
    }
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
