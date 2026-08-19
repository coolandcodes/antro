package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents a set of expressions that are located within a block.
 */
public class ExpressionSet extends Stmt {
    private final List<Expr> exprsns;

    public ExpressionSet(Expr expressions) {
        this.exprsns = expressions;
    }

    // It calls the specific visit method on the visitor intended for ExpressionSet nodes.
    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitExpressionSet(this);
    }

    public final List<Expr> getExpressions () {
        return exprsns;
    }
}