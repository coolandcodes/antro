package com.codedev.antro.compiler.frontend.ast.rules;

import java.util.List;
import java.util.Collections;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.ast.vocabulary.Stmt;

import com.codedev.antro.compiler.frontend.ast.rule.Require;
import com.codedev.antro.compiler.frontend.ast.rule.Module;
import com.codedev.antro.compiler.frontend.ast.rule.Export;
import com.codedev.antro.compiler.frontend.ast.rule.MainBlock;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * 
 */
public class Program extends Stmt {
    private List<Require> requires = Collections.emptyList();
    private List<Stmt> definitions = Collections.emptyList();

    private final Module module;
    private final Export exportLn;
    private final MainBlock mainBlk;

    /**
     * Constructs a new Program
     * 
     * @param _requires
     * @param _definitions
     * @param _module
     * @param _export
     * @param _main
     */
    public Program (Module _module,
        List<Require> _requires,
        List<Stmt> _definitions,
        MainBlock _main,
        Export _export) {
        this.requires = _requires;
        this.definitions = _definitions;
        this.module = _module;
        this.exportLn = _export;
        this.mainBlk = _main;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> visitor) {
        return visitor.visitProgram(this);
    }

    public final Module getModule () {
        return module;
    }

    public final List<Require> getRequires () {
        return requires;
    }

    public final  List<Stmt> getDefinitions () {
        return definitions;
    }

    public final Export getExport () {
        return exportLn;
    }

    public final MainBlock getMainBlock () {
        return mainBlk;
    }
}