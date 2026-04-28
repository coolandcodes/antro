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
public class Dowhile extends Stmt {
    // 1. Define fields to store state of the do-while statement
    private final Expr cond;
    private final Stmt body;

    // 2. Constructor: Initialize the only node of the tree
    public Dowhile(Stmt body, Expr condition) {
        this.cond = condition;
        this.body = body;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitDoWhile(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public Expr getCondition() {
        return cond.clone();
    }

    public Stmt getBody() {
        return body;
    }
}