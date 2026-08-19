package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;
import java.util.Collections;

import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A concrete implementation of the `invariants` block
 */
public class Invariants extends Stmt {
    private List<Expr> exprList = Collections.emptyList();
    private List<Stmt> stmtList = Collections.emptyList();

    /**
     * Constructs a new Invariants block
     * 
     * @param _exprList list if expression for `invariants` block.
     */
    public Invariants (List<Expr> _exprList) {
        this.exprList = _exprList;
    }

    /**
     * Constructs a new Invariants block
     * 
     * @param _stmtList list of statements for `invariants` block.
     */
    public Invariants (List<Stmt> _stmtList) {
        this.stmtList = _stmtList;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitInvariants(this);
    }

    public final List<Expr> getExpressions() {
        return exprList;
    }
}