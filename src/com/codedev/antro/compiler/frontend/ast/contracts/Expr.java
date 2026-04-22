package com.codedev.antro.compiler.frontend.ast.contracts;

public abstract class Expr {
    public interface Visitor<R> {
        R visitBinary(Binary e);
        R visitUnary(Unary e);
        R visitLiteral(Literal e);
        R visitVariable(Variable e);
        R visitAssignment(Assignment e);
        R visitCall(Call e);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    @Override
    public Expr clone() {
        try {
            return (Expr) super.clone();
        } catch (CloneNotSupportedException e) {
            //throw new AssertionError(e);
            return this;
        }
    }
}
