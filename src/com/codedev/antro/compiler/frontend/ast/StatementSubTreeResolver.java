package com.codedev.antro.compiler.frontend.ast;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Expr;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

import com.codedev.antro.compiler.frontend.ast.rules.Require;
import com.codedev.antro.compiler.frontend.ast.rules.Return;

import com.codedev.antro.compiler.frontend.ast.rules.MainBlock;
import com.codedev.antro.compiler.frontend.ast.rules.Program;


/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A resolver utility for dispatch ...
 */
public class StatementSubTreeResolver implements Stmt.Visitor<Void> {

    @Override
    public Void visitRequire(Require stmt) {
        // @TODO: semantic processing of require
        return null;
    }

    @Override
    public Void visitReturn(Return stmt) {
        // @TODO: semantic processing of return
        return null;
    }

    // ...

    @Override
    public Void visitProgram(Program stmt) {
        Stmt mod = stmt.getModule();

        if (mod != null) {
            mod.accept(this);
        }

        for (Stmt s : stmt.getRequires()) s.accept(this);
        for (Stmt s : stmt.getDefinitions()) s.accept(this);

        Stmt mainBlk = stmt.getMainBlock();

        if (mainBlk != null) {
            mainBlk.accept(this);
        }

         Stmt expt = stmt.getExport();

        if (expt != null) {
            expt.accept(this);
        }

        return null;
    }
}