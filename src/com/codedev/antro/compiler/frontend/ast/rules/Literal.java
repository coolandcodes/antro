package com.codedev.antro.compiler.frontend.ast.rules;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;

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
    private final Object value;

    /**
     * Constructs a new Literal expression.
     * 
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

    public final Object getValue() {
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