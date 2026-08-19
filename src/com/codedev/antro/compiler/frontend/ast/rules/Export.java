package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;
import java.util.Collections;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A conrete implementation for the `export *, *, *;` statement.
 */
public class Export extends Stmt {
    private List<Token> members = Collections.emptyList();

    /**
     * Constructs a new Export statement
     * 
     * @param _members the list of exported definitions.
     */
    public Export(List<Token> _members) {
        this.members = _members;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitExport(this);
    }

    public final List<Token> getMembers() {
        return members;
    }
}