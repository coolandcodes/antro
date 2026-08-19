package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Attribution;

/**
 * Represents ...
 */
public class Trial extends Expr {
    private final Expr prefix;
    private final Call call;
    private final List<Trial.Chain> chains;

    // @HINT: Prefer inner class defintion here.
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

    /**
     * Constructs a new Trial statement
     * 
     * @param prefixString the prefix ...
     * @param call the call statement portion of the trial statement.
     * @param chains the chain of statements for `eject_on` and `use` portions.
     */
    public Trial(Expr prefixString,
                Call call,
                List<Trial.Chain> chains) {
        this.prefix = prefixString;
        this.call = call;
        this.chains = chains;
    }

    // @HINT: It calls the specific visit method on the visitor intended for a Trial node.
    @Override
    public <R> R accept(Expr.Visitor<R> visitor) {
        return visitor.visitTrial(this);
    }

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