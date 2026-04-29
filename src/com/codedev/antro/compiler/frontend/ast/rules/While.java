package com.codedev.antro.comipler.frontend.ast.rules;

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
public class While extends Stmt {
    // 1. Define fields to store state of the while statement
    private final Expr cond;
    private final Stmt body;

    // 2. Constructor: Initialize the only node of the tree
    public While(Expr condition, Stmt body) {
        this.cond = condition;
        this.body = body;
    }

    // 3. The 'accept' method: This is the core of the Visitor Pattern.
    // It calls the specific visit method on the visitor intended for While nodes.
    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitWhile(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public Expr getCondition() {
        return cond.clone();
    }

    public Stmt getBody() {
        return body;
    }
}