package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;
import java.util.Collections;

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
public class Block extends Stmt {
    private List<Stmt> stmts = Collections.emptyList();

    /**
     * Constructs a new Block of statements
     * 
     * @param statements the list of statements for a block.
     */
    public Block(List<Stmt> statements) {
        this.stmts = statements;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitBlock(this);
    }

    public final List<Stmt> getStatements() {
        return stmts;
    }
}