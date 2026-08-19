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
 * Represents a unary expression, which consists of an operator 
 * and a single operand (e.g., -5 or !true).
 */
public class Unary extends Expr {
    private final Token operator;
    private final Expr right;

    /**
     * Constructs a new Unary statement
     * 
     * @param operator
     * @param right
     */
    public Unary(Token operator, Expr right) {
        this.operator = operator;
        this.right = right;
    }

    /**
     * The 'accept' method implementation for the Visitor pattern.
     * This allows the visitor to identify this node specifically as a Unary type.
     */
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitUnary(this);
    }

    public final Token getOperator() {
        return operator.clone();
    }

    public final Expr getRight() {
        return right.clone();
    }
}