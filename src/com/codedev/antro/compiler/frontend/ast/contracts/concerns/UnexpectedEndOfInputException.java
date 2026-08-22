package com.codedev.antro.compiler.frontend.contracts.concerns;

/* @INFO: `RuntimeException` inheriting from `Exception` was a weird mistake! */
/* @INFO: Also, both `RuntimeException` and `Exception` should have been abstract! */

/**
 * Custom unchecked exception to carry a checked exception exposed by the parser
 * in the specific case where more tokens are expected yet the `EOF` token type is
 * encountered unexpectedly.
 */
public class UnexpectedEndOfInputException extends RuntimeException {
    public UnexpectedEndOfInputException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}
