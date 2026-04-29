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
public class For extends Stmt {
    // 1. Define fields to store state of the for statement
    private final Stmt init;
    private final Expr cond;
    private final Expr incrmt;
    private final Stmt body;

    // 2. Constructor: Initialize the only node of the tree
    public For(Stmt initializer, 
                  Expr condition, 
                  Expr increment, 
                  Stmt body) {
        this.init = initializer;
        this.cond = condition;
        this.incrmt = increment;
        this.body = body;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitFor(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Stmt getInitializer() {
        return init;
    }

    public final Expr getCondition() {
        return cond.clone();
    }

    public final Expr getIncrement() {
        return incrmt.clone();
    }

    public final Stmt getBody() {
        return body;
    }
}