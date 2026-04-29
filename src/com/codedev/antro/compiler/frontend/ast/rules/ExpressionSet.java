package com.codedev.antro.comipler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.comipler.frontend.ast.contracts.Expr;
import com.codedev.antro.comipler.frontend.ast.contracts.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * 
 */
public class ExpressionSet extends Stmt {
    // 1. Define the field to store the state of the expression set
    private final List<Expr> exprsns;

    // 2. Constructor: Initialize the nodes of the tree
    public ExpressionSet(Expr expressions) {
        this.exprsns = expressions;
    }

    // 3. The 'accept' method: This is the core of the Visitor Pattern.
    // It calls the specific visit method on the visitor intended for ExpressionSet nodes.
    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitExpressionSet(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final getExpressions () {
        return exprsns;
    }
}