package com.codedev.antro.comipler.frontend.ast.rules;

import java.util.List;

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
public class Block extends Stmt {
    // 1. Define field to store the list of statements in a scoped block
    private final List<Stmt> stmts;

    // 2. Constructor: Initialize the only node of the tree
    public Block(List<Stmt> statements) {
        this.stmts = statements;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitBlock(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final List<Stmt> getStatements() {
        return stmts;
    }
}