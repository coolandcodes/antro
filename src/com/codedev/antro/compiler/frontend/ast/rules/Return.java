package com.codedev.antro.comipler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.comipler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.comipler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * 
 */
public class Return extends Stmt {
    // 1. Define field to store the list of statements in a scoped block
    private final Expr value;

    // 2. Constructor: Initialize the only node of the tree
    public Return(Expr value) {
        this.value = value;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitReturn(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Token getValue() {
        return value;
    }
}