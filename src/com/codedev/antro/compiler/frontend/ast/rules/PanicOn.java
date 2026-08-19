package com.codedev.antro.compiler.frontend.ast.rules;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A concrete implementation for the `panic_on *;` statement.
 */
public class PanicOn extends Stmt {
    private final Token errorVar;

    /**
     * Constructs a new `panic_on` statement
     * 
     * @param _errorVar the error variable to panic on
     */
    public PanicOn (Token _errorVar) {
        this.errorVar = _errorVar;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitPanic(this);
    }

    public final Token getErrorVariable () {
        return errorVar;
    }
}