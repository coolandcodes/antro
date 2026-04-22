package com.codedev.antro.compiler.frontend.lexer;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

public enum TokenType {

    // Special
    EOF,

    // Identifiers & literals
    IDENTIFIER,
    INT_LITERAL,
    FLOAT_LITERAL,
    STRING,
    FORMATTED_STRING,
    BOOLEAN,
    NULL,

    // Keywords
    IF, ELSE, ELIF, FOR, WHILE, DO, BEGIN, END,
    DEF, VAR, RETURN, MAIN, VOID,
    SWITCH, CASE, DEFAULT,
    BREAK, CONTINUE,
    EXPORT, REQUIRE, DEFER,
    THROW, PANIC_ON, EJECT_ON,
    USE, CALL,
    NEW, INVARIANTS,

    // Types
    TYPE_INT, TYPE_FLT, TYPE_STR, TYPE_ARR, TYPE_BOOL, TYPE_NIL,

    // Operators
    PLUS, MINUS, STAR, SLASH, MODULO,
    INCREMENT, DECREMENT,
    ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, STAR_ASSIGN, SLASH_ASSIGN, MOD_ASSIGN,
    EQUAL, NOT_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT,
    BIT_AND, BIT_OR, SHIFT_LEFT, SHIFT_RIGHT,
    ARROW,

    // Delimiters
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,
    COMMA, DOT, COLON, SEMICOLON,
    AT
}