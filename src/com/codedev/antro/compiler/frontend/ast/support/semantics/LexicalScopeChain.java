package com.codedev.antro.compiler.frontend.ast.support.semantics;

import java.util.HashMap;
import java.util.Map;

import com.codedev.antro.compiler.frontend.ast.support.Symbol;

public final class LexicalScopeChain {

    private final LexicalScopeChain parent;
    /* symbol table slice */
    private final Map<String, Symbol> symbols = new HashMap<>();

    public LexicalScopeChain(LexicalScopeChain parent) {
        this.parent = parent;
    }

    public void define(Symbol symbol) {
        symbols.put(symbol.name, symbol);
    }

    public Symbol resolve(String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        if (parent != null) return parent.resolve(name);
        return null;
    }
}