package com.codedev.antro.comipler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.comipler.frontend.lexer.Token;
import com.codedev.antro.comipler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.comipler.frontend.ast.vocabulary.Attribution;

/**
 * 
 */
public class Trial extends Expr {
    // 1. Define fields to store state of the switch statement
    private final Expr prefix;
    private final Call call;
    private final List<Trial.Chain> chains;

    public class Chain {
        private final Token type;
        private final Attribution value;

        public Chain (Token type, Attribution value) {
            this.value = value;
            this.type = type;
        }

        public final Expr getValue() {
            return value;
        }

        public final Token getType() {
            return type.clone();
        }
    }

    // 2. Constructor: Initialize the only node of the tree
    public Trial(Expr prefixString,
                Call call,
                List<Trial.Chain> chains) {
        this.prefix = prefixString;
        this.call = call;
        this.chains = chains;
    }

    // 3. The 'accept' method: This is the core of the Visitor Pattern.
    // It calls the specific visit method on the visitor intended for a Trial node.
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitTrial(this);
    }

    // 4. Accessors (Getters) so the Visitor can inspect the data
    public final Expr getPrefix() {
        return prefix;
    }

    public final List<Trial.Chain> getChains() {
        return chains;
    }

    public final Call getCallExpression() {
        return call;
    }
}