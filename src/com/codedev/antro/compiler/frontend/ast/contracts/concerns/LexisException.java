// Define the checked exception
public class LexisException extends Exception {
    public LexisException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}