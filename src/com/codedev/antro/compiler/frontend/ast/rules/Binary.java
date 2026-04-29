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
 * A concrete implementation of a Binary expression (e.g., 1 + 2).
 * This class extends the Expr abstract class and implements the Visitor hook.
 */
public class Binary extends Expr {
    // 1. Define fields to store the state of the expression
    private final Expr left;
    private final Token operator;
    private final Expr right;

    // 2. Constructor: Initialize the nodes of the tree
    public Binary(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    // 3. The 'accept' method: This is the core of the Visitor Pattern.
    // It calls the specific visit method on the visitor intended for Binary nodes.
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitBinary(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Expr getLeft() {
        return left.clone();
    }

    public final Token getOperator() {
        return operator.clone();
    }

    public final Expr getRight() {
        return right.clone();
    }
}