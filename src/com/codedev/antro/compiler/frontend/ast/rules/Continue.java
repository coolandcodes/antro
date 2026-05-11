package com.codedev.antro.comipler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.comipler.frontend.lexer.Token;
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
public class Continue extends Stmt {
    // 1. Define field to store the list of statements in a scoped block
    private final Token label;

    // 2. Constructor: Initialize the only node of the tree
    public Continue(Token label) {
        this.label = label;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitContinue(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Token getLabel() {
        return label;
    }
}