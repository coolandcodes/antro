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
 * Represents a `do { * } while(*)` loop.
 */
public class Dowhile extends Stmt {
    private final Expr cond;
    private final Stmt body;

    /**
     * Constructs a new DoWhile block
     * 
     * @param body the body of statements in the `do` block.
     * @param condition the conddition of the `while` portion.
     */
    public Dowhile(Stmt body, Expr condition) {
        this.cond = condition;
        this.body = body;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitDoWhile(this);
    }

    public final Expr getCondition() {
        return cond.clone();
    }

    public final Stmt getBody() {
        return body;
    }
}