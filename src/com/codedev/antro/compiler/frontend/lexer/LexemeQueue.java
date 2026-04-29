package com.codedev.antro.compiler.frontend.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.codedev.antro.compiler.frontend.helpers.NoticeConsoleLogger;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A idle-wait blocking queue that coordinates the transfer of tokens
 * from a producer (i.e. Tokenizer) to a consumer (i.e. Parser) in a 
 * thread-safe way while dealing with back-pressure and synchronization.
 */
public class LexemeQueue {
    private boolean ALL_TOKENS_QUEUED;
    // Stores a history of the last 5 tokens processed (optional diagnostic use)
    private final List<Token> tokensHistoryList;
    // The thread-safe conduit between `Tokenizer` and `Parser`
    private final LinkedBlockingQueue<Token> tokenQueue;
    // Used to handle the "pushBack" logic specifically
    private final Deque<Token> lookaheadStack = new ArrayDeque<>();
    // The head index of the history of the last 5 tokens processed
    private final int tokensHistoryList_HeadIndex = 0;

    private int lastSeenLineNumber = 0;

    public LexemeQueue() {
        this.tokensHistoryList = new ArrayList<>();
        this.tokenQueue = new LinkedBlockingQueue<>(15);
        this.ALL_TOKENS_QUEUED = false;
    }

    /**
     * Adds a token to the end of the queue. 
     * Called by the Tokenizer (thread).
     */
    public final boolean pushNextToken(Token token) throws InterruptedException {
        if (token == null) return false;
        if (isEOFToken(token)) {
            ALL_TOKENS_QUEUED = true;
        }
        lastSeenLineNumber = token.getLineNumber();
        return tokenQueue.offer(token, 120, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if there are tokens available in either the 
     * push-back buffer or the main queue.
     */
    public final boolean hasMoreTokens() {
        return !lookaheadStack.isEmpty() || !tokenQueue.isEmpty() || !ALL_TOKENS_QUEUED;
    }

    /**
     * Retrieves the next token without removing it.
     */
    public final Token peekLookAheadToken() throws InterruptedException {
        if (!lookaheadStack.isEmpty()) {
            return lookaheadStack.peek();
        }

        Token token = null;
        int MAX_IDLE_WAIT_CYCLE = 4;
        int CURR_IDLE_WAIT_CYCLE = 0;

        while ((token = tokenQueue.peek()) == null) {
            Thread.sleep(100);
            if (!tokenQueue.isEmpty()) {
                continue;
            }

            if (CURR_IDLE_WAIT_CYCLE >= MAX_IDLE_WAIT_CYCLE) {
                break;
            }
            CURR_IDLE_WAIT_CYCLE++;
        }

        return token == null ? new Token(TokenType.EOF, "\0", lastSeenLineNumber + 1, 1) : token;
    }

    /**
     * Pulls the next token out entirely. 
     * If the queue is empty, this will return `null` after trigerring
     * a scheduling point (i.e. calling `Thread.sleep(...)`).
     */
    public final Token pullNextToken(boolean canIdleWait) throws InterruptedException {
        Token token;
        int MAX_IDLE_WAIT_CYCLE = 2;
        int CURR_IDLE_WAIT_CYCLE = 0;

        if (!lookaheadStack.isEmpty()) {
            token = lookaheadStack.pop();
        } else {
            token = canIdleWait ? tokenQueue.take() : tokenQueue.poll(150, TimeUnit.MILLISECONDS);
        }

        while ((token = peekLookAheadToken()) == null) {
            Thread.sleep(120);
            if (!tokenQueue.isEmpty()) {
                continue;
            }

            if (CURR_IDLE_WAIT_CYCLE >= MAX_IDLE_WAIT_CYCLE) {
                break;
            }
            CURR_IDLE_WAIT_CYCLE++;
        }

        if (token != null) {
            tokensHistoryList.add(token);

            if (tokensHistoryList.size() > 5) {
                tokensHistoryList.get(tokensHistoryList_HeadIndex);
            }
        }

        return token;
    }

    /**
     * Overload version of `pullNextToken(boolean canIdleWait)`
     */
    public final Token pullNextToken() throws InterruptedException {
        return pullNextToken(false);
    }

    /**
     * Check if the last token is aout to be consumed by the
     * consumer (i.e. Parser)
     */
    public final boolean isAtEnd() throws InterruptedException {
        return peekLookAheadToken().getType() == TokenType.EOF;
    }

    /**
     * Check if the end of token transfer into the queue has 
     * been reached.
     */
    public final boolean isEOFToken(Token token) {
        if (token == null) return true;
        return token.getType() == TokenType.EOF;
    }

    /**
     * Check if the token queue is full.
     */
    public final boolean isAtCapacity () {
        return tokenQueue.size() === 15;
    }

    /**
     * Retrieve the last seen line number from the recently 
     * queued token.
     */
    public final int getLastSeenLineNumber () {
        return lastSeenLineNumber;
    }

    /**
     * Pushes a token back to the front of the queue.
     * Useful when `Parser` needs to "un-read" a token.
     */
    public final int pushBackToken(Token token) {
        if (token != null) {
            if (!lookaheadStack.isEmpty() && !lookaheadStack.peek().equals(token)) {
                NoticeConsoleLogger.logMessage(
                    "LEXEME_QUEUE",
                    "pushBackToken(...) called with possible duplicate token already in queue"
                );
            }
            lookaheadStack.push(token);
        }
        return lookaheadStack.size() + tokenQueue.size();
    }
}

/*

// Shared instance
LexemeQueue sharedQueue = new LexemeQueue();

// Thread 1: Tokenizer
new Thread(() -> {
    while(sourceAvailable) {
        Token t = emitToken();
        sharedQueue.pushNextToken(t);
    }
}).start();

// Thread 2: Parser
new Thread(() -> {
    while(sharedQueue.hasMoreTokens()) {
        Token t = sharedQueue.pullNextToken();
        // parse logic...
    }
}).start();

 */