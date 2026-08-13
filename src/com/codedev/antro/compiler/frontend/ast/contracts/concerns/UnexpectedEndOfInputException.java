package com.codedev.antro.compiler.frontend.contracts.concerns;

public class UnexpectedEndOfInputException extends RuntimeException {
    public UnexpectedEndOfInputException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}
