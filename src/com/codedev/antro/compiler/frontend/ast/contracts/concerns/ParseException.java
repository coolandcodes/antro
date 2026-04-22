/* @NOTE: Delibrately avoiding the use of `java.text.ParseException` */

// Define the checked exception
public class ParseException extends Exception {
    public ParseException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}