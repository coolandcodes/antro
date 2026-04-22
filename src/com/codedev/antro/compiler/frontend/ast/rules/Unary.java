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
 * Represents a unary expression, which consists of an operator 
 * and a single operand (e.g., -5 or !true).
 */
public class Unary extends Expr {
    // 1. Define fields for the operator and the expression it acts upon
    private final Token operator;
    private final Expr right;

    // 2. Constructor to initialize the unary expression
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

    // 3. Accessors (Getters) for the visitor to retrieve the node data
    public Token getOperator() {
        return operator.clone();
    }

    public Expr getRight() {
        return right.clone();
    }
}