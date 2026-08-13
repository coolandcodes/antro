package com.codedev.antro.compiler.frontend.ast.support;

import com.codedev.antro.compiler.frontend.ast.rules.Program;

public class ParseTree {
  private Program root = null;
  
  public ParseTree (Program prog) {
    this.root = prog;
  }
}
