package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;
import java.util.Collections;

import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
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
public class MainBlock extends Stmt {
    private List<Token> params = Collections.emptyList();
    private List<Stmt> body = Collections.emptyList();

    /**
     * Constructs a new Main function (entry point)
     * 
     * @param _params the params for the main function.
     * @param _body the body of statements for the main function.
     */
    public MainBlock (List<Token> _params, List<Stmt> _body) {
        this.params = _params;
        this.body = _body;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitMain(this);
    }

    public final List<Token> getParameters () {
        return params;
    }

    public final List<Stmt> getBody () {
        return body;
    }
}
