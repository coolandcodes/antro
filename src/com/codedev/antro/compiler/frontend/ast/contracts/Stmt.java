package com.codedev.antro.compiler.frontend.ast;

import java.util.List;

import com.codedev.antro.compiler.frontend.ast.contracts.Expr;
import com.codedev.antro.comipler.frontend.ast.rules.Block;
import com.codedev.antro.comipler.frontend.ast.rules.If;
import com.codedev.antro.comipler.frontend.ast.rules.While;
import com.codedev.antro.comipler.frontend.ast.rules.DoWhile;
import com.codedev.antro.comipler.frontend.ast.rules.For;
import com.codedev.antro.comipler.frontend.ast.rules.Switch;
import com.codedev.antro.comipler.frontend.ast.rules.Function;

public abstract class Stmt implements Cloneable {
    public interface Visitor<R> {
        R visitBlock(Block stmt);
        R visitIf(If stmt);
        R visitWhile(While stmt);
        R visitDoWhile(DoWhile stmt);
        R visitFor(For stmt);
        R visitSwitch(Switch stmt);
        R visitFunction(Function stmt);
        R visitExpressionSet(ExpressionSet exprs);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    @Override
    public Stmt clone() {
        try {
            return (Stmt) super.clone();
        } catch (CloneNotSupportedException e) {
            //throw new AssertionError(e);
            return this;
        }
    }
}