package com.codedev.antro.compiler.frontend.ast.rules;

import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents the `defer` block for both post-condition 
 * invariant assertion and a basic block of statement to
 * exxecute when leaving a function scope.
 */
public class Defer extends Stmt {
    private final Stmt exprns;

    /**
     * Constructs a new Defer block
     * 
     * @param _exprns the statements for the `defer` block
     */
    public Defer (Stmt _exprns) {
        this.exprs = _exprns;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitDefer(this);
    }

    public final Stmt getAttachedStatement () {
        return exprns;
    }

    public final boolean hasExpressionSet () {
        return exprns instanceof ExpressionSet;
    }

    public final boolean hasInvariantBlock () {
        return exprns instanceof Invariants;
    }
}