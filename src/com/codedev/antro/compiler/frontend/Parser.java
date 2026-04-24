package com.codedev.antro.compiler.frontend;

import com.codedev.antro.comipler.frontend.ast.ExpressionSubTreePrinter;
import com.codedev.antro.comipler.frontend.ast.contracts.Expr;
import com.codedev.antro.compiler.frontend.ast.rules.*;
import com.codedev.antro.compiler.frontend.lexer.LexemeQueue;
import static com.codedev.antro.compiler.frontend.lexer.TokenType.*;

import com.codedev.antro.compiler.frontend.contracts.concerns.ParseException;
import com.codedev.antro.compiler.frontend.contracts.concerns.UnexpectedEndOfInputException;

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
        XXXXXX
       ========================= */
    
    private Stmt parseStatement() throws Exception {
        if (matchAny(IF)) {
            advance(); // consume the `IF` token and discard it
            return parseIf();
        }

        if (matchAny(WHILE)) {
            advance(); // consume the `WHILE` token and discard it
            return parseWhile();
        }

        if (matchAny(DO)) {
            advance(); // consume the `DO` token and discard it
            return parseDoWhile();
        }

        if (matchAny(FOR)) {
            advance(); // consume the `FOR` token and discard it
            return parseFor();
        }

        if (matchAny(SWITCH)) {
            advance(); // consume the `SWITCH` token and discard it
            return parseSwitch();
        }

        if (matchAny(DEF)) {
            advance(); // consume the `DEF` token and discard it

            setExpectationForTokenType(COLON, "Expected ':' (after `def`)");
            advance(); // consume the `COLON` token and discard it

            return parseFunction(true);
        }

        if (matchAny(VAR)) {
            advance(); // consume the `VAR` token and discard it
            return parseFunction(false);
        }

        return parseExpression(false);
    }

    private Stmt parseBlock() throws Exception {
        setExpectationForTokenType(LBRACE, "Expected '{'");
        advance(); // consume the `LBRACE` token and discard it

        List<Stmt> statements = new ArrayList<>();

        while (!matchAny(RBRACE)) {
            statements.add(parseStatement());
        }

        setExpectationForTokenType(RBRACE, "Expected '}'");
        advance(); // consume the `RBRACE` token and discard it

        return new Block(statements);
    }
    
    private Stmt parseIf() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' (after `if`)");
        advance(); // consume the `LPAREN` token and discard it

        Expr IfCondition = parseExpression(false);

        setExpectationForTokenType(RPAREN, "Expected ')'");
        advance(); // consume the `RPAREN` token and discard it

        Stmt thenBranch = parseBlock();

        List<Stmt> elifs = new ArrayList<>();

        while (matchAny(ELIF)) {
            advance(); // consume the `ELIF` token and discard it

            setExpectationForTokenType(LPAREN, "Expected '(' after `elif`)");
            advance(); // consume the `LPAREN` token and discard it

            Expr elIfCondition = parseExpression(false);

            setExpectationForTokenType(RPAREN, "Expected ')'");
            advance(); // consume the `RPAREN` token and discard it

            Stmt block = parseBlock();

            elifs.add(new If(elIfCondition, block, List.of(), null));
        }

        Stmt elseBranch = null;

        if (matchAny(ELSE)) {
            advance(); // consume the `ELSE` token and discard it

            elseBranch = parseBlock();
        }

        return new If(ifCondition, thenBranch, elifs, elseBranch);
    }
    
    private Stmt parseWhile() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' after while");
        advance(); // consume the `LPAREN` token and discard it

        Expr condition = parseExpression(false);

        setExpectationForTokenType(RPAREN, "Expected ')'");
        advance(); // consume the `RPAREN` token and discard it

        Stmt body = parseBlock();

        return new While(condition, body);
    }
    
    private Stmt parseDoWhile() throws Exception {
        Stmt body = parseBlock();

        setExpectationForTokenType(WHILE, "Expected 'while' after (`do` block)");
        advance(); // consume the `WHILE` token and discard it

        setExpectationForTokenType(LPAREN, "Expected '('");
        advance(); // consume the `LPAREN` token and discard it

        Expr condition = parseExpression(false);


        setExpectationForTokenType(RPAREN, "Expected ')'");
        advance(); // consume the `RPAREN` token and discard it

        return new DoWhile(body, condition);
    }

    private Stmt parseFor() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' (after `for`)");
        advance(); // consume the `LPAREN` token and discard it

        // @HINT: initializer (can be empty)
        Stmt initializer = null;

        if (!matchAny(SEMICOLON)) {
            initializer = parseExpression(true);
        }

        setExpectationForTokenType(SEMICOLON, "Expected ';'");
        advance(); // consume the `SEMICOLON` token and discard it

        // @HINT: condition (can be empty too)
        Expr condition = null;

        if (!matchAny(SEMICOLON)) {
            condition = parseExpression(true);
        }

        setExpectationForTokenType(SEMICOLON, "Expected ';'");
        advance(); // consume the `SEMICOLON` token and discard it

        // @HINT: increment (also possibly empty)
        Expr increment = null;

        if (!matchAny(RPAREN)) {
            increment = parseExpression(false);
        }

        setExpectationForTokenType(RPAREN, "Expected ')'");
        advance(); // consume the `RPAREN` token and discard it

        Stmt body = parseBlock();

        return new For(initializer, condition, increment, body);
    }

    private Stmt parseSwitch() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' (after `switch`)");
        advance(); // consume the `LPAREN` token and discard it

        Expr expr = parseExpression(false);

        setExpectationForTokenType(RPAREN, "Expected ')'");
        advance(); // consume the `RPAREN` token and discard it

        setExpectationForTokenType(LBRACE, "Expected '{'");
        advance(); // consume the `LBRACE` token and discard it

        List<Case> cases = new ArrayList<>();
        Stmt defaultBranch = null;

        boolean foundCase_Keyword = false;
        boolean foundDefault_Keyword = false;

        while (!matchAny(RBRACE)) {

            /* @HINT: Cannot have a `default` statement before a `case` statement */
            if (!foundDefault_Keyword && matchAny(CASE)) {
                foundCase_Keyword = true;
                advance(); // consume the `CASE` token and discard it
                Expr value = parseExpression(false);

                setExpectationForTokenType(COLON, "Expected ':'");
                advance(); // consume the `COLON` token and discard it

                List<Stmt> body = new ArrayList<>();
                while (!check(CASE) && !check(DEFAULT) && !check(RBRACE)) {
                    body.add(parseStatement());
                }

                cases.add(new Switch.Case(value, body));
            }

            /* @HINT: Cannot have more than one `default` statement */
            else if (!foundDefault_Keyword && matchAny(DEFAULT)) {
                foundDefault_Keyword = true;
                advance(); // consume the `DEFAULT` token and discard it

                setExpectationForTokenType(COLON, "Expected ':'");
                advance(); // consume the `COLON` token and discard it

                List<Stmt> body = new ArrayList<>();

                while (!matchAny(RBRACE)) {
                    body.add(parseStatement());
                }

                defaultBranch = new Block(body);
            }

            /* @HINT: Deal with fall-through cases and a single default */
            if ((foundCase_Keyword && !matchAny(CASE)) || foundDefault_Keyword) {
                setExpectationForTokenType(BREAK, "Expected 'break'");
                advance(); // consume the `BREAK` token and discard it

                if (matchAny(SEMICOLON)) {
                    advance(); // consume the `SEMICOLON` token and discard it
                }
            }

            // Check possible error states 
            if (foundDefault_Keyword && matchAny(CASE)) {
                ;
            }

        }

        setExpectationForTokenType(RBRACE, "Expected '}'");
        advance(); // consume the `RBRACE` token and discard it

        return new Switch(expr, cases, defaultBranch);
    }

    private Stmt parseFunction(boolean isGlobalDefinition) throws Exception {
        setExpectationForTokenType(IDENTIFIER, "Expected function name");
        Token identifier = advance(); // consume the `IDENTIFIER` token

        if (identifier == null) {
            error(
                new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                "unrecoverable parser state encountered",
                new NullPointerException(
                    "Expected <assignment-operator> token where 'nullish' value is found"
                )
            );
        }

        setExpectationForTokenType(LPAREN, "Expected '('");
        advance(); // consume the `LPAREN` token and discard it

        List<Token> params = new ArrayList<>();
        if (!matchAny(RPAREN)) {
            do {
                setExpectationForTokenType(IDENTIFIER, "Expected function parameter");
                params.add(advance()); // consume the `IDENTIFIER` token
            } while (matchAny(COMMA));
        }

        setExpectationForTokenType(RPAREN, "Expected ')' or ','");
        advance(); // consume the `RPAREN` token and discard it

        Stmt body = parseBlock();
        
        if (isGlobalDefinition) {
            setExpectationForTokenType(SEMICOLON, "Expected ';' (after function body)");
            advance(); // consume the `SEMICOLON` token and discard it
        } else {
            if (matchAny(SEMICOLON)) {
                advance(); // consume the `SEMICOLON` token and discard it
            }
        }

        return new Function(identifier, params, body, isGlobalDefinition);
    }


    /* =========================
       RIGHT-RECURSIVE EXTRACTION
       ========================= */

    public Expr parseExpression(boolean isDelimited) throws Exception {
        Expr expr = return parseAssignment();

        if (isDelimited) {
            setExpectationForTokenType(SEMICOLON, "Expected ';'");
            advance(); // consume the `SEMICOLON` token and discard it
        }

        return expr;
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
                    new NullPointerException(
                        "Expected <assignment-operator> token where 'nullish' value is found"
                    )
                );
            }

            if (expr instanceof Variable) {
                Variable v = (Variable) expr;
                Token variableToken = v.getIdentifier();
                if (variableToken.getType() != IDENTIFIER) {
                    error(
                        variableToken,
                        "reserved keywords cannot be used for <identifier> where '"+variableToken.getImage()+"' is found"
                    );
                }
                expr = new Assignment(variableToken, value);
                return expr;
            }
            ExpressionSubTreePrinter printer = new ExpressionSubTreePrinter();
            error(operatorToken, "Invalid assignment for target expression: '" + printer.print(expr) + "'");
        }

        return expr;
    }

    private Expr parseLogicalOr() throws Exception {
        Expr expr = parseLogicalAnd();

        while (matchAny(LOGICAL_OR)) {
            Token operatorToken = advance();
            Expr right = parseLogicalAnd();
            if (operatorToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException(
                        "Expected <logical-operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <logical-operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <relational-operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <bitwise-operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <arithmetic-operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <unary-operator> token where 'nullish' value is found"
                    )
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
                    new NullPointerException(
                        "Expected <literal> token where 'nullish' value is found"
                    )
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
            setExpectationForTokenType(RPAREN, "Expected token ')'");
            advance(); // consume the `RPAREN` token and discard it
            return expr;
        }

        setExpectationForLookAhead("Expected a <literal> or a <parentesized-expression>");
    }

    /* =========================
       PARSER HELPERS
       ========================= */

    /**
     * 
     */
    private boolean matchAny(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     */
    private boolean check(TokenType type) {
        try {
            return !tokenQueue.isAtEnd() && peek().getType() == type;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            return false;
        }
    }

    /**
     * Consume a token from the token queue
     */
    private Token advance() {
        try {
            if (tokenQueue.isAtEnd()) {
                return null;
            }

            Token nextToken = tokenQueue.pullNextToken(true);
            int MAX_WAIT_CYCLE = 2;
            int CURR_WAIT_CYCLE = 0;

            while (nextToken == null || tokenQueue.isAtCapacity()) {
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

    /**
     * Backtrack on a single token.
     * Push the token back into the token queue.
     */
    private void backtrackOnToken (Token token) {
        tokenQueue.pushBackToken(token);
    }

    /**
     * Lookahead and peek the next token still in the token queue.
     */
    private Token peek() {
        try {
            return tokenQueue.peekLookAheadToken();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            return new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1);
        }
    }

    /**
     * Enforce grammar rules where a lookahead token-type match fails.
     */
    private void setExpectationForLookAhead(String msg) throws Exception {
        Token nextToken = peek();
        error(nextToken, msg + " where '"+nextToken.getImage()+"' is found");
    }

    /**
     * Enforce grammar rules where an expected token-type check fails.
     */
    private void setExpectationForTokenType(TokenType type, String msg) throws Exception {
        if (!check(type)) {
            setExpectationForLookAhead(msg);
        }
    }

    private void error(Token token, String msgText) throws Exception {
        String messagePrefix = "[line: " + token.getLineNumber() + ", column: " + token.getColumnNumber() + "]; ";

        if (tokenQueue.hasMoreTokens() && tokenQueue.isEOFToken(token)) {
            UnexpectedEndOfInputException eofEx = throw new UnexpectedEndOfInputException(
                "parser token queue not exhausted yet EOF token encountered"
            );
            throw new Exception(
                messagePrefix + msgText,
                eofEx
            );
        }

        throw new Exception(
            messagePrefix + msgText
        );
    }

    private void error(Token token, String msgText, RuntimeException ex) throws Exception {
        String messagePrefix = "[line: " + token.getLineNumber() + ", column: " + token.getColumnNumber() + "]; ";

        if (tokenQueue.hasMoreTokens() && tokenQueue.isEOFToken(token)) {
            UnexpectedEndOfInputException eofEx = new UnexpectedEndOfInputException(
                "parser token queue not exhausted yet EOF token encountered",
                ex
            );
            throw new Exception(
                messagePrefix + msgText,
                eofEx
            );
        }

        throw new Exception(
            messagePrefix + msgText,
            ex
        );
    }
}
