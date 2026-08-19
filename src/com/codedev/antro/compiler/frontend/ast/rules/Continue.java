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
 * A concrete implementation class for the `continue *;` statement.
 */
public class Continue extends Stmt {
    private final Token label;

    /**
     * Constructs a new Continue statement
     * 
     * @param label the label of the `continue` statement (if any)
     */
    public Continue(Token label) {
        this.label = label;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitContinue(this);
    }

    public final Token getLabel() {
        return label;
    }
}