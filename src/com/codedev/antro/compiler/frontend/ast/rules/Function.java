package com.codedev.antro.comipler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.comipler.frontend.lexer.Token;
import com.codedev.antro.comipler.frontend.ast.contracts.Expr;
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
public class Function extends Stmt {
    // 1. Define fields to store the state of the function
    private final Token name;
    private final List<Token> params;
    private final Stmt body;
    public final boolean globalFlag;

    // 2. Constructor: Initialize the only node of the tree
    public Function(Token name, 
                 List<Token> params, 
                 Stmt body,
                 boolean globalFlag) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.globalFlag = globalFlag;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitFunction(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Token getName () {
        return name.clone();
    }

    public final List<Token> getParameters () {
        return params;
    }

    public final Stmt getBody () {
        return body;
    }
}