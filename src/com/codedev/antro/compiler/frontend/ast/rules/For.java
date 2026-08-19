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
 * Represents a for loop and all its parts including the statments
 * within its body.
 */
public class For extends Stmt {
    private final Stmt init;
    private final Expr cond;
    private final Expr incrmt;
    private final Stmt body;

    /**
     * 
     */
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