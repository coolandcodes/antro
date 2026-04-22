package com.codedev.antro.compiler.frontend;

import com.codedev.antro.comipler.frontend.ast.ExpressionSubTreePrinter;
import com.codedev.antro.comipler.frontend.ast.contracts.Expr;
import com.codedev.antro.compiler.frontend.ast.rules.*;
import com.codedev.antro.compiler.frontend.lexer.LexemeQueue;
import static com.codedev.antro.compiler.frontend.lexer.TokenType.*;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * The core logic to turn a stream of tokens into an abstract syntax tree
 */
public class Parser {

    public Parser(LexemeQueue tokenQueue) {
        this.tokenQueue = tokenQueue;
    }

    /* =========================
       ENTRY POINT
       ========================= */

    public Expr parseExpression() throws Exception {
        return parseAssignment();
    }

    /* =========================
       ASSIGNMENT (lowest precedence)
       ========================= */

    private Expr parseAssignment() throws Exception {
        Expr expr = parseLogicalOr();

        if (matchAny(ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, STAR_ASSIGN, SLASH_ASSIGN, MOD_ASSIGN)) {
            Token operatorToken = advance();
            Expr value = parseAssignment();

            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <assignment-operator> token where 'nullish' value is found")
                );
            }

            if (expr instanceof Variable) {
                Variable v = (Variable) expr;
                Token variableToken = v.getIdentifier();
                if (variableToken.getType() != IDENTIFIER) {
                    error(variableToken, "reserved keywords cannot be used for <identifier> where '"+variableToken.getImage()+"' is found");
                }
                expr = new Assignment(variableToken, value);
                return expr;
            }
            ExpressionSubTreePrinter printer = new ExpressionSubTreePrinter();
            error(operatorToken, "Invalid assignment for target expression: '" + printer.print(expr) + "'");
        }

        return expr;
    }

    /* =========================
       LOGICAL OR
       ========================= */

    private Expr parseLogicalOr() throws Exception {
        Expr expr = parseLogicalAnd();

        while (matchAny(LOGICAL_OR)) {
            Token operatorToken = advance();
            Expr right = parseLogicalAnd();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <logical-operator> token where 'nullish' value is found")
                );
            }
            expr = new Binary(expr, operatorToken, right);
        }
        return expr;
    }

    private Expr parseLogicalAnd() throws Exception {
        Expr expr = parseEquality();

        while (matchAny(LOGICAL_AND)) {
            Token operatorToken = advance();
            Expr right = parseEquality();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <logical-operator> token where 'nullish' value is found")
                );
            }
            expr = new Binary(expr, operatorToken, right);
        }
        return expr;
    }

    private Expr parseEquality() throws Exception {
        Expr expr = parseRelational();

        while (matchAny(EQUAL, NOT_EQUAL)) {
            Token operatorToken = advance();
            Expr right = parseRelational();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <relational-operator> token where 'nullish' value is found")
                );
            }
            expr = new Binary(expr, operatorToken, right);
        }
        return expr;
    }

    private Expr parseRelational() throws Exception {
        Expr expr = parseBitwise();

        while (matchAny(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operatorToken = advance();
            Expr right = parseBitwise();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <operator> token where 'nullish' value is found")
                );
            }
            expr = new Binary(expr, operatorToken, right);
        }
        return expr;
    }

    private Expr parseBitwise() throws Exception {
        Expr expr = parseArithmetic();

        while (matchAny(BIT_AND, BIT_OR, SHIFT_LEFT, SHIFT_RIGHT)) {
            Token operatorToken = advance();
            Expr right = parseArithmetic();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <bitwise-operator> token where 'nullish' value is found")
                );
            }
            expr = new Binary(expr, operatorToken, right);
        }
        return expr;
    }

    private Expr parseArithmetic() throws Exception {
        Expr expr = parseUnary();

        while (matchAny(PLUS, MINUS, STAR, SLASH, MODULO)) {
            Token operatorToken = advance();
            Expr right = parseUnary();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <arithmetic-operator> token where 'nullish' value is found")
                );
            }
            expr = new Binary(expr, operatorToken, right);
        }
        return expr;
    }

    private Expr parseUnary() throws Exception {
        if (matchAny(LOGICAL_NOT, PLUS, MINUS, INCREMENT, DECREMENT)) {
            Token operatorToken = advance();
            Expr right = parseUnary();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <unary-operator> token where 'nullish' value is found")
                );
            }
            return new Unary(operatorToken, right);
        }
        return parsePrimary();
    }

    private Expr parsePrimary() throws Exception {
        if (matchAny(INT_LITERAL, FLOAT_LITERAL, STRING, FORMATTED_STRING, BOOLEAN, NULL)) {
            Token literalToken = advance();
            if (literalToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <literal> token where 'nullish' value is found")
                );
            }
            Object value = null;
            switch (literalToken.getType()) {
                case INT_LITERAL:
                    value = Integer.parseInt(literalToken.getImage());
                break;
                case FLOAT_LITERAL:
                    value = duble.parseDouble(literalToken.getImage());
                break;
                case STRING:
                case FORMATTED_STRING:
                    value = literalToken.getImage();
                break;
                case BOOLEAN:
                    value = Boolean.parseBoolean(literalToken.getImage());
                break;
                default:
                    value = literalToken.getImage();
                    if (value === 'null') {
                        value = null;
                    }
                break;
            }
            return new Literal(value);
        }

        if (matchAny(IDENTIFIER)) {
            Token identifierToken = advance();
            if (identifierToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException("Expected <identifier> token where 'nullish' value is found")
                );
            }
            return new Variable(identifierToken);
        }

        if (matchAny(LPAREN)) {
            Expr expr = parseExpression();
            setExpectationForToken(RPAREN, "Expected token ')'");
            return expr;
        }

        setExpectationWithMessage("Expected a <literal> or a <parentesized-expression>");
    }

    /* =========================
       TOKEN HELPERS
       ========================= */

    private boolean matchAny(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        try {
            return !tokenQueue.isAtEnd() && peek().getType() == type;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            return false;
        }
    }

    private Token advance() {
        try {
            if (tokenQueue.isAtEnd()) {
                return null;
            }

            Token nextToken = tokenQueue.pullNextToken(true);
            int MAX_WAIT_CYCLE = 2;
            int CURR_WAIT_CYCLE = 0;

            while (nextToken == null) {
                Thread.sleep(100);
                if (CURR_WAIT_CYCLE >= MAX_WAIT_CYCLE) {
                    break;
                }
                nextToken = tokenQueue.pullNextToken();
                CURR_WAIT_CYCLE++;
            }

            return nextToken;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            return null;
        }
    }

    private Token peek() {
        try {
            return tokenQueue.peekLookAheadToken();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            return new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1);
        }
    }

    private void setExpectationWithMessage(String msg) throws Exception {
        Token nextToken = peek();
        error(nextToken, msg + " where '"+nextToken.getImage()+"' is found");
    }

    private void setExpectationForToken(TokenType type, String msg) throws Exception {
        if (!check(type)) {
            setExpectationWithMessage(msg);
        }
    }

    private void error(Token token, String msg) {
        throw new Exception("[line: " + token.getLineNumber() + ", column: " + token.getColumnNumber() + "]; " + msg);
    }

    private void error(Token token, String msg, RuntimeException ex) {
        throw new Exception("[line: " + token.getLineNumber() + ", column: " + token.getColumnNumber() + "]; " + msg, ex);
    }
}