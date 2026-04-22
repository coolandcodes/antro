package com.codedev.antro.comipler.frontend.ast.rules;

import com.codedev.antro.comipler.frontend.lexer.Token;
import com.codedev.antro.comipler.frontend.ast.contracts.Expr;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents an assignment expression (e.g., x = 5).
 * It stores the name of the variable being assigned to and the
 * expression that produces the value to be stored.
 */
public class Assignment extends Expr {
    // The name of the variable receiving the value
    private final Token name;
    // The expression whose result will be assigned to the variable
    private final Expr value;

    /**
     * Constructs a new Assignment expression.
     * @param name  The identifier token of the variable.
     * @param value The expression on the right-hand side of the equals sign.
     */
    public Assignment(Token name, Expr value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Implements the Visitor pattern hook.
     * Dispatches the call to the visitor's specific visitAssignment method.
     */
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignment(this);
    }

    /**
     * Returns the name token of the variable being assigned.
     */
    public Token getLeft() {
        return name;
    }

    /**
     * Returns the expression representing the value to be assigned.
     */
    public Expr getRight() {
        return value;
    }
}