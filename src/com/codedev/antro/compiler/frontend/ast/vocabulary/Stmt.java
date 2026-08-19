package com.codedev.antro.compiler.frontend.ast.vocabulary;

import java.util.List;

import com.codedev.antro.compiler.frontend.ast.rules.ExpressionSet;
import com.codedev.antro.compiler.frontend.ast.rules.Block;
import com.codedev.antro.compiler.frontend.ast.rules.If;
import com.codedev.antro.compiler.frontend.ast.rules.While;
import com.codedev.antro.compiler.frontend.ast.rules.DoWhile;
import com.codedev.antro.compiler.frontend.ast.rules.For;
import com.codedev.antro.compiler.frontend.ast.rules.Switch;
import com.codedev.antro.compiler.frontend.ast.rules.Function;
import com.codedev.antro.compiler.frontend.ast.rules.Return;
import com.codedev.antro.compiler.frontend.ast.rules.Break;
import com.codedev.antro.compiler.frontend.ast.rules.Continue;
import com.codedev.antro.compiler.frontend.ast.rules.Require;
import com.codedev.antro.compiler.frontend.ast.rules.Module;
import com.codedev.antro.compiler.frontend.ast.rules.Export;
import com.codedev.antro.compiler.frontend.ast.rules.Invariants;
import com.codedev.antro.compiler.frontend.ast.rules.Defer;
import com.codedev.antro.compiler.frontend.ast.rules.PanicOn;

import com.codedev.antro.compiler.frontend.ast.rules.MainBlock;
import com.codedev.antro.compiler.frontend.ast.rules.Program;

public abstract class Stmt implements Cloneable, Attribution {
    public interface Visitor<R> {
        R visitBlock(Block stmt);
        R visitIf(If stmt);
        R visitWhile(While stmt);
        R visitDoWhile(DoWhile stmt);
        R visitFor(For stmt);
        R visitSwitch(Switch stmt);
        R visitFunction(Function stmt);
        R visitExpressionSet(ExpressionSet exprs);
        R visitBreak(Break brk);
        R visitReturn(Return retn);
        R visitContinue(Continue cont);
        R visitRequire(Require req);
        R visitModule(Module mod);
        R visitExport(Export exp);
        R visitPanic(PanicOn pan);
        R visitDefer(Defer def);
        R visitInvariants(InvariantsBlock invr);
        R visitMain(MainBlock mainBlk);
        R visitProgram(Program prog);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    @Override
    public String getVocabularyTitle() {
        return 'com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt';
    }

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