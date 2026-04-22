package com.codedev.antro.compiler.frontend.lexer;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

public class Token implements Cloneable {

    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return this.type;
    }

    public String getImage() {
        return this.lexeme;
    }

    public int getLineNumber() {
        return this.line;
    }

    public int getColumnNumber() {
        return this.column;
    }

    @Override
    public Token clone() {
        try {
            return (Token) super.clone();
        } catch (CloneNotSupportedException e) {
            //throw new AssertionError(e);
            return this;
        }
    }

    @Override
    public String toString() {
        return "Token kind: " + this.type +
               ", Token image: '" + this.lexeme +
               "'-> at line: " + this.line +
               "-> on column: " + this.column;
    }
}
