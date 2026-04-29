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
public class Switch extends Stmt {
    // 1. Define fields to store state of the switch statement
    private final Expr exprsn;
    private final List<Case> cases;
    private final Stmt defltBrch;

    public class Case {
        private final Expr value;
        private final List<Stmt> body;

        public Case (Expr value, List<Stmt> body) {
            this.value = value;
            this.body = body;
        }

        public Expr getValue() {
            return value.clone();
        }

        public List<Stmt> getBody() {
            return body;
        }
    }

    // 2. Constructor: Initialize the only node of the tree
    public Switch(Expr expression,
                 List<Case> cases, 
                 Stmt defaultBranch) {
        this.exprsn = expression;
        this.cases = cases;
        this.defltBrch = defaultBranch;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitSwitch(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Expr getExpression() {
        return exprsn.clone();
    }

    public final List<Case> getCaseBranches() {
        return cases;
    }

    public final Stmt getDefaultBranch() {
        return defltBrch;
    }
}