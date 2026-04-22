// Define the checked exception
public class UnexpectedEndOfInputException extends RuntimeException {
    public UnexpectedEndOfInputException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}