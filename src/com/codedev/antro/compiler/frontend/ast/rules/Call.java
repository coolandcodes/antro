package com.coolandcodes.antro.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.comipler.frontend.lexer.Token;
import com.codedev.antro.comipler.frontend.ast.vocabulary.Expr;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents a function or method call expression.
 * It stores the 'callee' (the expression being called), 
 * the arguments passed to it, and the closing parenthesis for location info.
 */
public class Call extends Expr {
    // The identifier for the function/object being called
    private final Token name;
    // The expression that evaluates to the function/object being called
    private final Expr callee;
    // The token for the closing parenthesis (useful for error reporting)
    private final Token paren;
    // The list of expressions passed as arguments
    private final List<Expr> arguments;

    /**
     * Constructs a new Call expression.
     * @param name      The token holding the function name.
     * @param callee    The expression representing the function.
     * @param paren     The closing parenthesis token.
     * @param arguments The list of argument expressions.
     */
    public Call(Token name, Expr callee, Token paren, List<Expr> arguments) {
        this.name = name;
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }

    /**
     * Implements the Visitor pattern hook.
     * Dispatches the call to the visitor's specific visitCall method.
     */
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitCall(this);
    }

    /**
     * Returns the function name
     */
    public final Token getIdentifier() {
        return name;
    }

    /**
     * Returns the expression representing the function being called.
     */
    public final Expr getCallee() {
        return callee.clone();
    }

    /**
     * Returns the closing parenthesis token.
     */
    public final Token getClosingParen() {
        return paren.clone();
    }

    /**
     * Returns the list of argument expressions.
     */
    public final List<Expr> getArguments() {
        return arguments;
    }
}