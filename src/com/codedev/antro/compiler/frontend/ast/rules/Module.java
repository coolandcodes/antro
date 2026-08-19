package com.codedev.antro.compiler.frontend.ast.rules;

import com.codedev.antro.compiler.frontend.lexer.Token;
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
public class Module extends Stmt {
    private final Token path;

    /**
     * Constructs a new Module statement
     * 
     * @param _path the path to the module
     */
    public Module (Token _path) {
        this.path = _path;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitModule(this);
    }

    public final Token getPath() {
        return path;
    }
}