package com.codedev.antro.compiler.frontend.contracts.concerns;

/* @INFO: Deliberately avoiding the use of `java.text.ParseException` */

/* @INFO: `RuntimeException` inheriting from `Exception` was a weird mistake! */
/* @INFO: Also, both `RuntimeException` and `Exception` should have been abstract! */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom unchecked exception to accumulate multiple parsing errors.
 */
public class ParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    // @INFO: Initialized as an empty list to avoid `NullPointerException`s
    private List<Exception> exceptions = Collections.emptyList();

    /**
     * Constructs a ParseException with only a descriptive message.
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * Sets the internal list of exceptions.
     * 
     * @param exceptions The list of collected inner exceptions
     */
    public void setExceptions(List<Exception> exceptions) {
        if (exceptions != null) {
            // @HINT: Defensive copy and make immutable to protect integrity
            exceptions = Collections.unmodifiableList(new ArrayList<>(exceptions));
        } else {
            exceptions = Collections.emptyList();
        }
    }

    /**
     * Returns an unmodifiable list of the inner exceptions.
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }
}
