package demo1.httprestclientservice.exceptions;


/**
 * Thrown when the RestClient receives a 404 Not Found from the backend.
 *
 * NB! Do not annotate it with: @ResponseStatus(CustomHttpStatus.NOT_FOUND), because if you do,
 * then the custom error message defined in the RestClient.builder() will be overwritten by the annotation;
 * The end client Postman will not get anything about the 404,
 * instead postman client will receive generic 500 "internal error"
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super();
    }
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}