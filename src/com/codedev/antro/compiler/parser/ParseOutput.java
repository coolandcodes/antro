/*
 * Antro Compiler
 * https://www.coolcode.io/antro
 * Copyright (c) 2014-2018 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

package com.codedev.antro.compiler.parser;

import com.codedev.antro.compiler.tokenizer.*;

import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;

public final class ParseOutput {

	private ParseTree rootNode;

	private String currentProductionRule;
    private ArrayList<Token> tokensGroup;
    private HashMap<String, ArrayList<Token>> parseBlocks;
    // private = new TokenBuffer();

	public ParseOutput(){
		
		 this.currentProductionRule = null;
         this.parseBlocks = new HashMap<String, ArrayList<Token>>();
         this.tokensGroup = new ArrayList<Token>();

         this.rootNode = new ParseTree("program");
	}

	public ParseTree getAST() {

		return rootNode;
	}

	public String getCurrentProduction(){

		return this.currentProductionRule;
	}

	public void setCurrentProduction(String p){

		this.currentProductionRule = p;
	}

	public void appendTokenToTree(Token t){

		this.tokensGroup.add(t);
		
		ParseTree child = rootNode.makeNode();
	}
}