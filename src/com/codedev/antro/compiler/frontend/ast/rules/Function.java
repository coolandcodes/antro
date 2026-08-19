package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * Represents a function definition (e.g., `def: doSeomthing() { ... };` ).
 * It stores the name of the. function being ddefined as well as
 * the list of parameters and the set of statements that make up
 * the body of the function definition..
 */
public class Function extends Stmt {
    private final Token name;
    private final List<Token> params;
    private final Stmt body;
    public final boolean globalFlag;

    /**
     * Constructs a new Function block
     * 
     * @param name the name of the function.
     * @param params the list of function paramenters.
     * @param body the body of statements within the function definition.
     * @param globalFlag the boolean flag signaling the lexical scope.
     */
    public Function(Token name, 
                 List<Token> params, 
                 Stmt body,
                 boolean globalFlag) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.globalFlag = globalFlag;
    }

    // @HINT: It calls the specific visit method on the visitor intended for Function nodes.
    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitFunction(this);
    }

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