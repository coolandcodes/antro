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
public class If extends Stmt {
    // 1. Define fields to store state of the if statement
    private final Expr cond;
    private final Stmt ifBrch;
    private final List<Stmt> elIfStmts;
    private final Stmt elseBrch;

    // 2. Constructor: Initialize the only node of the tree
    public If(Expr condition, 
                 Stmt ifBranch, 
                 List<Stmt> elifBranches, 
                 Stmt elseBranch) {
        this.cond = condition;
        this.ifBrch = ifBranch;
        this.elIfStmts = elifBranches;
        this.elseBrch = elseBranch;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitIf(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Expr getCondition() {
        return cond.clone();
    }

    public final Stmt getIfBranch() {
        return ifBrch;
    }

    public final List<Stmt> getElseIfBranches() {
        return elIfStmts;
    }

    public final Stmt getElseBranch() {
        return elseBrch;
    }
}