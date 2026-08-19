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
 * A concreate implementation of the `retn *;` statement
 */
public class Return extends Stmt {
    private final Expr value;

    /**
     * Constructs a new Return statement.
     * 
     * @param value the expression value returned.
     */
    public Return(Expr value) {
        this.value = value;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitReturn(this);
    }

    public final Token getValue() {
        return value;
    }
}