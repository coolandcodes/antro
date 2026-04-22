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
 * A Literal expression represents a constant value in the source code,
 * such as a number, a string, or a boolean (e.g., 42, "hello", or true).
 */
public class Literal extends Expr {
    // 1. The raw value of the literal (stored as a generic Object)
    private final Object value;

    /**
     * Constructor for a Literal expression.
     * @param value The actual runtime value of the literal.
     */
    public Literal(Object value) {
        this.value = value;
    }

    /**
     * The 'accept' method implementation for the Visitor pattern.
     * Dispatches the call to the visitor's 'visitLiteral' method.
     */
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitLiteral(this);
    }

    // 2. Accessor (Getter) for the visitor to retrieve the stored value
    public Object getValue() {
        try {
            return value.clone();
        } catch (CloneNotSupportedException e) {
            return value;
        }
    }
}

/*

Expr left = new Literal(123.0);
Expr right = new Literal("abc");
Token operator = new Token(TokenType.PLUS, "+", null, 1);

Expr result = new Binary(left, operator, right);

 */