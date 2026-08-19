package com.codedev.antro.compiler.frontend.ast.rules;

import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents a `while` block
 */
public class While extends Stmt {
    private final Expr cond;
    private final Stmt body;

    /**
     * Constructs a new While block
     * 
     * @param condition the condition for the while loop.
     * @param body the body of statements for the while loop.
     */
    public While(Expr condition, Stmt body) {
        this.cond = condition;
        this.body = body;
    }

    // @HINT: It calls the specific visit method on the visitor intended for While nodes.
    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitWhile(this);
    }

    public final Expr getCondition() {
        return cond.clone();
    }

    public final Stmt getBody() {
        return body;
    }
}