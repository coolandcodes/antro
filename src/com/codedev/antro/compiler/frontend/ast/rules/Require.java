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
 * Represents the module loading facilities for source files.
 */
public class Require extends Stmt {
    private final Token path;
    private final Token namespace;

    /**
     * Constructs a new Require statement.
     * 
     * @param _path the path of the module being required.
     * @param _namespace the dynamic namespace for the moddule eing required.
     */
    public Require (Token _path, Token _namespace) {
        this.path = _path;
        this.namespace = _namespace;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitRequire(this);
    }

    public final Token getPath() {
        return path;
    }

    public final Token getNamespace() {
        return namespace;
    }
}