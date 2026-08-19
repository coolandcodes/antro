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
 * A concrete implementation of a Binary expression (e.g., 1 + 2).
 * This class extends the Expr abstract class and implements the Visitor hook.
 */
public class Binary extends Expr {
    private final Expr left;
    private final Token operator;
    private final Expr right;

    /**
     * Constructs a new Binary expression
     * 
     * @param left
     * @param operator
     * @param right
     */
    public Binary(Expr left, Token operator, Expr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    // @HINT: It calls the specific visit method on the visitor intended for Binary nodes.
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitBinary(this);
    }

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