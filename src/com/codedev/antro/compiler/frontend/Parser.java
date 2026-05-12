package com.codedev.antro.compiler.frontend;

import java.util.ArrayList;
import java.util.List;

import com.codedev.antro.comipler.frontend.ast.ExpressionSubTreePrinter;
import com.codedev.antro.comipler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.comipler.frontend.ast.vocabulary.Stmt;
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
        COMPOUND EXPRESSIONS
        ======================== */
    private Call parseCallExpression() throws Exception {
        setExpectationForTokenType(COLON, "Expected ':' after `call`");
        advance(); // consume the `COLON` token and discard it
        setExpectationForTokenType(IDENTIFIER, "Expected [function name]");
        Token functionNameToken = advance(); // consume the `IDENTIFIER token and keep it

        if (functionNameToken == null) {
            error(
                new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                "unrecoverable parser state encountered",
                new NullPointerException("Expected <function name> token where 'nullish' value is found")
            );
        }

        setExpectationForTokenType(LPAREN, "Expected '(' after [function name]");
        advance(); // consume the `LPAREN` token and discard it

        List<Expr> args = new ArrayList<>();

        if (!matchAny(RPAREN)) {
            do {
                if (args.size() > 0) {
                    advance(); // consume the `COMMA` token and discard it
                }

                args.add(parseExpression(false));
            } while (matchAny(COMMA));
        }

        setExpectationForTokenType(RPAREN, "Expected ')' after last [function argument]");
        Token paren = advance(); // consume the `RPAREN` token and keep it (for error handling purposes)

        return new Call(name, null, paren, args);
    }

    private Expr parseTrialSubExpression(Call prefix, Expr call) throws Exception {

        List<Trial.Chain> chains = new ArrayList<>();

        boolean foundEjectOn_Keyword = false;
        boolean foundUse_Keyword = false;

        /* @FIXME:
        
            Right now there's a bug here that allows continous chaining 
            of `-> eject_on error -> use { ... } -> eject_on -> use { ... }`
        */
        while (matchAny(ARROW)) {
            advance(); // consume the `ARROW` token and discard it

            if (!foundUse_Keyword && (!foundEjectOn_Keyword && matchAny(EJECT_ON))) {
                Token type = advance(); // consume the `EJECT_ON` token and keep it

                Expr value = null;

                if (matchAny(IDENTIFIER)) {
                    value = new Variable(advance());
                }
                
                if (value == null) {
                    if (!check(ARROW)) {
                        /* @FIXME: I'm lost here... 😅🤣 */
                    }

                    setExpectationForLookAhead("Expected [error-identifier] after `eject_on`");
                }

                foundEjectOn_Keyword = true;
                chains.add(new Trial.Chain(type, value));
                continue;
            }

            if (foundEjectOn_Keyword && (!foundUse_Keyword && matchAny(USE))) {
                Token type = advance(); // consume the `USE` token and keep it
                Stmt block = parseBlock('`use`');

                foundUse_Keyword = true;
                chains.add(new Trial.Chain(type, block));
                continue;
            }

            break;
        }

        // @HINT: Check probable error states
        //
        // e.g. a eject_on expression followed by an arrow and then another eject_on
        if (foundEjectOn_Keyword && !foundUse_Keyword && matchAny(EJECT_ON)) {
            setExpectationForLookAhead("Expected 'use' after `->`");
        }

        return new Trial(prefix, call, chains);
    }

    /* =========================
        STATEMENTS
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

            setExpectationForTokenType(COLON, "Expected ':' after `def`");
            advance(); // consume the `COLON` token and discard it

            setExpectationForTokenType(IDENTIFIER, "Expected [function name] after ':'");
            Token identifier = advance(); // consume the `IDENTIFIER` token
            
            if (identifier == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException(
                        "Expected <function-name> token where 'nullish' value is found"
                    )
                );
            }

            return parseFunction(true, identifier);
        }

        if (matchAny(VAR)) {
            advance(); // consume the `VAR` token and discard it

            Token nextToken = advance(); // consume the `IDENTIFIER` token and keep it
            
            if (nextToken == null) {
                error(
                    new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                    "unrecoverable parser state encountered",
                    new NullPointerException(
                        "Expected <identifier> token where 'nullish' value is found"
                    )
                );
            }
        

            if (check(ASSIGN)) {
                backtrackOnToken(nextToken); // after token is pushed back here ...
                return parseVarDeclaration(); // ... it gets consumed here 👈🏼
            }
            
            if (nextToken.getType() != IDENTIFIER) {
                if (nextToken.getType() == ASSIGN) {
                    error(
                        nextToken,
                        "Expected [variable name] after `var` where '"+nextToken.getImage()+"' is found"
                    );
                } else {
                    error(
                        nextToken,
                        "Expected [function name] after `var` where '"+nextToken.getImage()+"' is found"
                    );
                }
            }

            return parseFunction(false, nextToken);
        }

        if (matchAny(RETURN)) {
            advance(); // consume the `RETURN` token and discard it
            return parseReturn();
        }

        if (matchAny(PANIC_ON)) {
            advance(); // consume the `PANIC_ON` token and discard it

            /* @TODO: Implementation goes here */
        }

        ExpressionSet set;
        List<Expr> expressions = new ArrayList<>();

        while (true) {
            Expr expr = parseExpression(false);
            expressions.add(expr);
            
            if (matchAny(COMMA)) {
                advance(); // consume the `COMMA` token and discard it
                continue;
            }

            if (matchAny(SEMICOLON)) {
                advance(); // consume the `SEMICOLON` token and discard it
            }

            break;
        }

        set = new ExpressionSet(expressions);
        return set;
    }

    private Stmt parseReturn() throws Exception {
        Expr value = null;

        if (!matchAny(SEMICOLON)) {
            value = parseExpression(false);
        } else {
            advance(); // consume the `SEMICOLON` token and discard it
        }

        return new Return(value);
    }

    private Stmt parseContinue(boolean checkForLabel) {
        Token label = null;

        if (checkForLabel) {
            if (matchAny(IDENTIFIER)) {
                label = advance(); // consume the `IDENTIFIER` token
            }
        }

        if (matchAny(SEMICOLON)) {
            advance(); // consume the `SEMICOLON` token and discard it
        }

        return new Continue(label);
    }

    private Stmt parseBreak(boolean checkForLabel) {
        Token label = null;

        if (checkForLabel) {
            if (matchAny(IDENTIFIER)) {
                label = advance(); // consume the `IDENTIFIER` token
            }
        }

        if (matchAny(SEMICOLON)) {
            advance(); // consume the `SEMICOLON` token and discard it
        }

        return new Break(label);
    }

    private Stmt parseBlock(String owner) throws Exception {
        setExpectationForTokenType(LBRACE, "Expected '{' after "+owner);
        advance(); // consume the `LBRACE` token and discard it

        List<Stmt> statements = new ArrayList<>();

        while (!matchAny(RBRACE)) {
            if (matchAny(CONTINUE)) {
                advance(); // consume the `CONTINUE` token and discard it
                statements.add(parseContinue(true));
            }

            else if (matchAny(BREAK)) {
                advance(); // consume the `BREAK` token and discard it
                statements.add(parseBreak(true));
            }

            else {
                statements.add(parseStatement());
            }
        }

        String tag = owner.equals("`function`") ? " declaration body" : " statement body";
        setExpectationForTokenType(RBRACE, "Expected '}' after ["+owner+tag+"]");
        advance(); // consume the `RBRACE` token and discard it

        return new Block(statements);
    }

    private Stmt parseVarDeclaration() {

        List<Expr> declarations = new ArrayList<>();

        do {
            if (declarations.size() > 0) {
                advance(); // consume the `COMMA` token and discard it
            }

            setExpectationForTokenType(IDENTIFIER, "Expected [variable name] after `var`");
            Token name = advance(); // consume token

            Expr initializer = null;
            Token operator = null;

            if (matchAny(ASSIGN)) {
                operator = advance(); // consume token and keep it
            }

            if (operator == null) {
                if (matchAny(COMMA)) {
                    declarations.add(new Variable(name));
                    continue;
                }

                setExpectationForLookAhead("Expected [assignment operator] after [variable name]");
            }

            initializer = parseExpression(false);

            declarations.add(new Assignment(name, operator, initializer));

        } while (matchAny(COMMA));

        if (matchAny(SEMICOLON)) {
            advance(); // consume token and discard it
        }

        return new ExpressionSet(declarations);
    }
    
    private Stmt parseIf() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' after `if`");
        advance(); // consume the `LPAREN` token and discard it

        Expr IfCondition = parseExpression(false);

        setExpectationForTokenType(RPAREN, "Expected ')' after [condition]");
        advance(); // consume the `RPAREN` token and discard it

        Stmt thenBranch = parseBlock('`if`');

        List<Stmt> elifs = new ArrayList<>();

        while (matchAny(ELIF)) {
            advance(); // consume the `ELIF` token and discard it

            setExpectationForTokenType(LPAREN, "Expected '(' after `elif`");
            advance(); // consume the `LPAREN` token and discard it

            Expr elIfCondition = parseExpression(false);

            setExpectationForTokenType(RPAREN, "Expected ')' after [condition]");
            advance(); // consume the `RPAREN` token and discard it

            Stmt block = parseBlock('`else if`');

            elifs.add(new If(elIfCondition, block, List.of(), null));
        }

        Stmt elseBranch = null;

        if (matchAny(ELSE)) {
            advance(); // consume the `ELSE` token and discard it

            elseBranch = parseBlock('`else`');
        }

        return new If(ifCondition, thenBranch, elifs, elseBranch);
    }
    
    private Stmt parseWhile() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' after `while`");
        advance(); // consume the `LPAREN` token and discard it

        Expr condition = parseExpression(false);

        setExpectationForTokenType(RPAREN, "Expected ')' after [condition]");
        advance(); // consume the `RPAREN` token and discard it

        Stmt body = parseBlock('`while`');

        return new While(condition, body);
    }
    
    private Stmt parseDoWhile() throws Exception {
        Stmt body = parseBlock('`do while`');

        setExpectationForTokenType(WHILE, "Expected 'while' after [do block]");
        advance(); // consume the `WHILE` token and discard it

        setExpectationForTokenType(LPAREN, "Expected '('");
        advance(); // consume the `LPAREN` token and discard it

        Expr condition = parseExpression(false);


        setExpectationForTokenType(RPAREN, "Expected ')' after [condition]");
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

        Stmt body = parseBlock('`for`');

        return new For(initializer, condition, increment, body);
    }

    private Stmt parseSwitch() throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' after `switch`");
        advance(); // consume the `LPAREN` token and discard it

        Expr expr = parseExpression(false);

        setExpectationForTokenType(RPAREN, "Expected ')' after [switch expression/value]");
        advance(); // consume the `RPAREN` token and discard it

        setExpectationForTokenType(LBRACE, "Expected '{' after '('");
        advance(); // consume the `LBRACE` token and discard it

        List<Case> cases = new ArrayList<>();
        Stmt defaultBranch = null;

        boolean foundCase_Keyword = false;
        boolean foundDefault_Keyword = false;

        while (!matchAny(RBRACE)) {

            /* @HINT: Cannot have a `default` statement before a `case` statement */
            if (!foundDefault_Keyword && matchAny(CASE)) {
                advance(); // consume the `CASE` token and discard it
                Expr value = parseExpression(false);

                setExpectationForTokenType(COLON, "Expected ':' after [case expression/value]");
                advance(); // consume the `COLON` token and discard it

                List<Stmt> body = new ArrayList<>();
                
                while (!check(CASE) && !check(RBRACE)) {
                    if (matchAny(CONTINUE)) {
                        advance(); // consume the `CONTINUE` token and discard it
                        statements.add(parseContinue(false));
                    } else {
                        body.add(parseStatement());
                    }
                }

                foundCase_Keyword = true;
                cases.add(new Switch.Case(value, body));
            }

            /* @HINT: Cannot have more than one `default` statement */
            else if (!foundDefault_Keyword && matchAny(DEFAULT)) {
                advance(); // consume the `DEFAULT` token and discard it

                setExpectationForTokenType(COLON, "Expected ':' after `default`");
                advance(); // consume the `COLON` token and discard it

                List<Stmt> body = new ArrayList<>();

                while (!check(RBRACE)) {
                    if (matchAny(CONTINUE)) {
                        advance(); // consume the `CONTINUE` token and discard it
                        statements.add(parseContinue(false));
                    } else {
                        body.add(parseStatement());
                    }
                }

                foundDefault_Keyword = true;
                defaultBranch = new Block(body);
            }

            // @HINT: Check for probable error states:
            //
            // e.g. having a case after a default block
            if (foundDefault_Keyword && matchAny(CASE)) {
                setExpectationForLookAhead("Expected 'break' after `default`");
            }

            //
            // e.g. having a default block after a default block
            if (foundDefault_Keyword && matchAny(DEFAULT)) {
                setExpectationForLookAhead("Expected 'break' after `default`");
            }

            /* @HINT: Deal with non-fall-through cases and a single default */
            if ((foundCase_Keyword && !matchAny(CASE)) || foundDefault_Keyword) {
                String type = foundDefault_Keyword ? "default" : "case";
                setExpectationForTokenType(BREAK, "Expected 'break' after ["+type+" block body]");
                advance(); // consume the `BREAK` token and discard it

                parseBreak(false); // discard break statement details
            }

            /* @HINT: Deal with fall-through cases */
            continue;
        }

        setExpectationForTokenType(RBRACE, "Expected '}' after [`switch` statement body]");
        advance(); // consume the `RBRACE` token and discard it

        return new Switch(expr, cases, defaultBranch);
    }

    private Stmt parseFunction(boolean isGlobalDefinition, Token functionName) throws Exception {
        setExpectationForTokenType(LPAREN, "Expected '(' after [function name]");
        advance(); // consume the `LPAREN` token and discard it

        List<Token> params = new ArrayList<>();
        if (!matchAny(RPAREN)) {
            boolean startFlag = true;
            do {
                if (params.size() > 0) {
                    advance(); // consume the `COMMA` token and discard it
                }

                String message = startFlag
                ? "Expected [function parameter] after '('"
                : "Expected [function parameter] after ','";

                setExpectationForTokenType(IDENTIFIER, message);
                params.add(advance()); // consume the `IDENTIFIER` token
                startFlag = false;
            } while (matchAny(COMMA));
        }

        setExpectationForTokenType(RPAREN, "Expected ')' or ',' after [function parameter]");
        advance(); // consume the `RPAREN` token and discard it

        Stmt body = parseBlock('`function`');
        
        if (isGlobalDefinition) {
            setExpectationForTokenType(SEMICOLON, "Expected ';' after [`function` declaration body]");
            advance(); // consume the `SEMICOLON` token and discard it
        } else {
            if (matchAny(SEMICOLON)) {
                advance(); // consume the `SEMICOLON` token and discard it
            }
        }

        return new Function(functionName, params, body, isGlobalDefinition);
    }


    /* ===========================
       RIGHT-RECURSIVE EXTRACTION
       ========================== */

    public Expr parseExpression(boolean isDelimited) throws Exception {
        Expr expr = return parseAssignment();

        if (isDelimited) {
            setExpectationForTokenType(SEMICOLON, "Expected ';' after [expression]");
            advance(); // consume the `SEMICOLON` token and discard it
        }

        return expr;
    }

    /* ====================================
       EXPRESSIONS (with lowest precedence)
       ==================================== */

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
                expr = new Assignment(variableToken, operatorToken, value);
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
        if (isTrialStart()) {
            CallExpr call = null;
            Expr prefix = null;

            if (matchAny(STRING, FORMATTED_STRING)) {
                Token literalToken = advance(); // consume token

                if (literalToken == null) {
                    error(
                        new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1),
                        "unrecoverable parser state encountered",
                        new NullPointerException(
                            "Expected <literal> token where 'nullish' value is found"
                        )
                    );
                }
                prefix = new Literal(literalToken.getImage());
                
                if (matchAny(COMMA)) {
                    advance(); // consume token an discard it

                    if (!check(CALL)) {
                        return prefix;
                    }
                } else {
                    ; /* @TODO: Figure out the state of the parser at this point and derive an invariant */
                }
            }

            if (matchAny(CALL)) {
                advance(); // consume token
                call = parseCallExpression();

                if (check(ARROW)) {
                    return parseTrialSubExpression(prefix, call);
                }

                return call;
            }
        }

        if (matchAny(LOGICAL_NOT, PLUS, MINUS, INCREMENT, DECREMENT)) {
            Token operatorToken = advance(); // consume token
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
                    value = Double.parseDouble(literalToken.getImage());
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
            Token nextToken = tokenQueue.pullNextToken(true);
            int MAX_WAIT_CYCLE = 2;
            int CURR_WAIT_CYCLE = 0;

            while (nextToken == null || tokenQueue.isAtCapacity()) {
                Thread.sleep(10);
                if (CURR_WAIT_CYCLE >= MAX_WAIT_CYCLE) {
                    break;
                }
                nextToken = tokenQueue.pullNextToken();
                CURR_WAIT_CYCLE++;
            }

            return nextToken;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        } finally {
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
            if (!tokenQueue.isAtEnd()) {
                return tokenQueue.peekLookAheadToken();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        } finally {
          return new Token(EOF, '\0', tokenQueue.getLastSeenLineNumber() + 1, 1);
        }
    }

    /**
     * Check if a trail compound expression is up ahead in the stream of tokens
     */
    private boolean isTrialStart() {
        return check(STRING) || check(FORMATTED_STRING) || check(CALL);
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
