package com.codedev.antro.compiler.frontend.ast;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.contracts.Expr;

import com.codedev.antro.compiler.frontend.ast.rules.*;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A ancillary artifact for printing the contents of an AST sub tree
 */
public class ExpressionSubTreePrinter implements Expr.Visitor<String> {
    
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinary(Binary expr) {
        Token operator = expr.getOperator();
        return "(" + operator.getImage() + " " + 
               expr.getLeft().accept(this) + " " + 
               expr.getRight().accept(this) + ")";
    }

    @Override
    public String visitUnary(Unary expr) {
        Token operator = expr.getOperator();
        return "\t (" + operator.getImage() + " " + 
               expr.getRight().accept(this) + ")" ;
    }

    @Override
    public String visitLiteral(Literal expr) {
        Object value = expr.getValue();
        return "\t\t (" + value.toString() + ")" ;
    }

    @Override
    public String visitVariable(Literal expr) {
        Token identifier = expr.getIdentifier();
        return "\t\t (" + identifier.getImage() + ")" ;
    }

    /* @TODO: implement other visit methods ... */
}