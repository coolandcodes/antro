package com.codedev.antro.compiler.frontend.contracts.concerns;

/* @INFO: `RuntimeException` inheriting from `Exception` was a weird mistake! */
/* @INFO: Also, both `RuntimeException` and `Exception` should have been abstract! */

/**
 * Custom unchecked exception to carry any checked exception exposed by the tokenizer
 */
public class LexisException extends RuntimeException {
    public LexisException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}
