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
 * 
 */
public class Switch extends Stmt {
    private final List<Case> cases = Collections.emptyList();

    private final Expr exprsn;
    private final Stmt defltBrch;

    // @HINT: Prefer an inner class definition here.
    public class Case {
        private final Expr value;
        private final List<Stmt> body;

        public Case (Expr value, List<Stmt> body) {
            this.value = value;
            this.body = body;
        }

        public final Expr getValue() {
            return value.clone();
        }

        public final List<Stmt> getBody() {
            return body;
        }
    }

    /**
     * Constrcuts a new Switch statement
     * 
     * @param expression the expression to match
     * @param cases all the cases within the `switch` block. 
     * @param defaultBranch the default case within the `switch` block.
     */
    public Switch(Expr expression,
                 List<Case> cases, 
                 Stmt defaultBranch) {
        this.exprsn = expression;
        this.cases = cases;
        this.defltBrch = defaultBranch;
    }

    // @HINT: It calls the specific visit method on the visitor intended for Switch nodes.
    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitSwitch(this);
    }

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