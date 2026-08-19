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
 * Represents an if statement (e.g., `if (...) { ... } else { ... }`)
 * It stores the `if` condition, the then block (i.e. a set of statments)
 * as well as the `else` block and any `else if` conditions and block. 
 */
public class If extends Stmt {
    private final Expr cond;
    private final Stmt ifBrch;
    private final List<Stmt> elIfStmts;
    private final Stmt elseBrch;

    /**
     * Constructs a new If/ElIf/Else block set
     * 
     * @param condition the condtion of the `if` portion of the block set.
     * @param ifBranch the block of statements for the `if` portion of the block set.
     * @param elifranches a set of block for the `elif` portion of the block set.
     * @param elseBranch the block of statements for the `else` portion of the block set.
     */
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