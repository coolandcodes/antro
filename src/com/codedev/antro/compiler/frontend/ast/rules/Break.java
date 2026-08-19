package com.codedev.antro.compiler.frontend.ast.rules;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.complier.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A concrete implementation class for the `break *;` statement.
 */
public class Break extends Stmt {
    private final Token label;

    /**
     * Constructs a new Break statement
     * 
     * @param label the label of the `break` statement (if any)
     */
    public Break(Token label) {
        this.label = label;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitBreak(this);
    }

    public final Token getLabel() {
        return label;
    }
}