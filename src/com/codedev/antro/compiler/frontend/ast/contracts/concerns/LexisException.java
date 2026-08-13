package com.codedev.antro.compiler.frontend.contracts.concerns;

public class LexisException extends Exception {
    public LexisException(String message, Throwable cause) {
        super(message);
        this.initCause(cause); 
    }
}
