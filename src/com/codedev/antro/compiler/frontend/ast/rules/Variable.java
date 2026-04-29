package com.codedev.antro.compiler.frontend.ast..rules;

import com.codedev.antro.comipler.frontend.lexer.Token;
import com.codedev.antro.comipler.frontend.ast.contracts.Expr;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents a variable access expression (e.g., 'x' or 'myVar').
 * It stores the name token which is used to look up the variable's value 
 * in the current scope during evaluation.
 */
public class Variable extends Expr {
    // The token representing the variable's name (holds lexeme and line info)
    private final Token name;

    /**
     * Constructs a new Variable expression.
     * @param name The identifier token for the variable.
     */
    public Variable(Token name) {
        this.name = name;
    }

    /**
     * Implements the Visitor pattern hook.
     * Dispatches the call to the visitor's specific visitVariable method.
     */
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitVariable(this);
    }

    /**
     * Returns the name token of the variable.
     * Use this in the visitor to get the variable's identifier string.
     */
    public final Token getIdentifier() {
        return name.clone();
    }
}