package com.codedev.antro.compiler.frontend.ast.support.semantics;

import com.codedev.antro.compiler.frontend.ast.contracts.Expr;
import com.codedev.antro.compiler.frontend.ast.contracts.Stmt;
import com.codedev.antro.compiler.frontend.ast.support.Symbol;

import com.codedev.antro.compiler.frontend.ast.rules.*;

public final class LexicalScopeChainResolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private LexicalScope current = new LexicalScopeChain(null);

    private void beginScope() {
        current = new LexicalScopeChain(current);
    }

    private void endScope() {
        current = current.parent;
    }

    /* =========================
       DECLARATION USAGE
       ========================= */

    @Override
    public Void visitVarDecl(VarDecl stmt) {
        current.define(new Symbol(stmt.name().getImage()));

        if (stmt.initializer() != null) {
            stmt.initializer().accept(this);
        }
        return null;
    }

    /* =========================
       VARIABLE USAGE
       ========================= */

    @Override
    public Void visitVariable(Variable expr) throws Exception {
        Symbol sym = current.resolve(expr.getIdentifier().getImage());

        if (sym == null) {
            throw new Exception("Undefined variable: " + expr.getIdentifier().getImage());
        }
        return null;
    }

    /* =========================
       BLOCK USAGE
       ========================= */

    @Override
    public Void visitBlock(Block stmt) {
        beginScope();
        for (Stmt s : stmt.statements()) {
            s.accept(this);
        }
        endScope();
        return null;
    }

    /* =========================
       FUNCTION USAGE
       ========================= */

    @Override
    public Void visitFunction(Function stmt) {

        current.define(new Symbol(stmt.getName().getImage()));

        beginScope();

        for (Token param : stmt.getParameters()) {
            current.define(new Symbol(param.getImage()));
        }

        stmt.getBody().accept(this);

        endScope();
        return null;
    }

    /* =========================
       EXPRESSIONS USAGE
       ========================= */

    @Override
    public Void visitBinary(Binary e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        return null;
    }

    @Override
    public Void visitUnary(Unary e) {
        e.getRight().accept(this);
        return null;
    }

    @Override
    public Void visitAssignment(Assignment e) {
        e.getRight().accept(this);
        return null;
    }

    @Override
    public Void visitCall(CallExpr e) {
        for (Expr arg : e.args()) arg.accept(this);
        return null;
    }

    @Override
    public Void visitTrial(TrialExpr e) {
        if (e.prefixString() != null) e.prefixString().accept(this);
        e.call().accept(this);
        return null;
    }

    @Override
    public Void visitLiteral(Literal e) { return null; }
}