/*
 * Antro Compiler
 * https://www.coolcode.io/antro
 * Copyright (c) 2014-2018 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

package com.codedev.antro.compiler.parser;

import com.codedev.antro.compiler.tokenizer.*;

import java.util.Stack;
import java.util.LinkedList;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Parser {

     private static final String INPUT_FILE_NAME; 
     private Tokenizer tokenizer;
     private long  tokensLeftCount;
     private long allTokensCount;
     private Token currentToken;
     private Token lookAheadToken;
     private ParseOutput parsed; 

     private enum StackSymbols {
        START, FOLLOW, END
     } 

     public Parser(String fileName) throws IOException, IllegalArgumentException{  
        
         File f = new File(fileName);
         Parser.INPUT_FILE_NAME = f.getName();
         parsed = new ParseOutput(Parser.INPUT_FILE_NAME);

         try{
            
            this.tokenizer = new Tokenizer(f);
            this.tokensLeftCount = this.allTokensCount = this.tokenizer.getTokenCount(); // save the initial total number of token found from input!
         
         }catch(Tokenizer.InvalidTokenCharException e){
            System.err.println(e.getMessage()); // just development error handling... will change at distribution time
            System.exit(0);
         } 
     }
     
     private void getToken(){
         if(tokenizer.hasMoreTokens()){
            currentToken = tokenizer.nextToken();
         }else{
            currentToken = null;
         }
     }
     
      private boolean expect(String type, boolean absolutely) throws ParseException, UnexpectedEndOfInputException {
            String actualType = tokenizer.lookAheadToken().getType();
            int lineNumber; 
            boolean result = false;
            if(actualType.equals(type)){
                 getToken(); /* retrieve the immediate next token from the tokenizer */
                 result = consumeToken(type); /* {accept} this token if we actually can consumed it! *(ALGORITHM RULE) */   
            }else{
               if(absolutely){
                   lineNumber = tokenizer.lookAheadToken().getLineNumber();
                   throw new ParseException("Syntax Error: expected <"+type+"> but found <"+actualType+"> on line "+lineNumber+"...");
               }else{
                  result = absolutely;
               }
            }
            return result;
     }
      
     private boolean expectKeyword(String keyword, boolean absolutely) throws ParseException, UnexpectedEndOfInputException{
         String actualType = tokenizer.lookAheadToken().getType();
         String actualImage = tokenizer.lookAheadToken().getImage();
         int lineNumber;
         boolean result = false;
         if(!actualType.equals("keyword")){
             throw new ParseException("TypeError: Invalid token '"+actualImage+"' encountered...");
         }else{
             if(actualImage.equals(keyword)){
                result = expect(actualType, absolutely);
             }
         }
         return result;
    }

     private boolean consumeToken(String type) throws UnexpectedEndOfInputException{
        boolean result = false;  
        if(currentToken == null){
            if(allTokensCount == 0){
               System.out.println("zero tokens found for input file: "+Parser.INPUT_FILE_NAME);
            }
            // TODO : check the below again to make sure {conditional} logic is indeed applicable to serve the error!!!
            if(allTokensCount > 0){
               throw new UnexpectedEndOfInputException("expected <"+type+"> but can consume no more tokens from input file "+Parser.INPUT_FILE_NAME);
            }
            
        }else{
           if(currentToken.getType().equals(type)){
                result = true;              
                parsed.appendTokenToTree(currentToken);
           }else{
                result = false;
           }
        }
        
        return result;
     }
     
     private boolean condition() throws ParseException, UnexpectedEndOfInputException{
          boolean result = false;
          
          if(expect("logicalunaryoperator", false)){
              /* TODO: instead of utilizing [pushBack], try making a case for condtional and absolute expectation of a token either at follow points and/or choice points */
              // RESOLVED: don't use [pushBack] as "backtracking" is not involved in this parser.Use "lookahead" instead {tokenizer.pushBack(1);}
          }
          
          if( factor() ){
                result = true;
                while(result == expect("logicalbinaryoperator", true)){
                    if( !factor() ){
                        result = false;
                        break;
                    } 
                }
          }
         
        return result;
     }
     
     private boolean factor() throws ParseException, UnexpectedEndOfInputException{ 
         boolean result = false; 
         if(expect("variable", false)){
            // consume the current token only if it is a "variable" (by conditional expectation)
            result = true;
         }else if(expect("openbracket", false)){
            // consume the current token only if it is an "openbracket" (by conditional expectation)
            if(anyexpression()){
                result = expect("closebracket", true); // expect a "closebracket" at the end
            }else{
               throw new ParseException("Syntax Error: ");
            }    
         }else{
            /* TODO: instead of utilizing [pushBack], try making a case for condtional and absolute expectation of a token either at follow points and/or choice points */
            // RESOLVED: don't use [pushBack] as "backtracking" is not involved in this parser.Use "lookahead" instead {tokenizer.pushBack(1);}
            result = anyexpression();
         }
         return result;  
     }
     
     private boolean numfactor() throws ParseException, UnexpectedEndOfInputException{
         boolean result = false;   
         getToken(); /* retrieve the immediate next token from the tokenizer */
         if(consumeToken("int") || consumeToken("float")){
             // consume the current token only if it is an "int" or a "float"
             result = true;
         }
         return result;
     }
     
     private boolean term() throws ParseException, UnexpectedEndOfInputException{
        boolean result = false; 
        if( factor() ){
            result = true;
            while(result == expect("arithmethicbinaryoperator-mul", true)){
                if(!factor()){
                    result = false;
                    break; // TODO: instead of "breaking", the parser may need to signal an error here by throwing one
                } 
            }
        }else if( numfactor( ) ){
            result = true;
            while(result == expect("arithmethicbinaryoperator-mul", true)){
                if(!numfactor()){
                    result = false;
                    break; // TODO: instead of "breaking", the parser may need to signal an error here by throwing one
                } 
            }
        }
       return result;
     }

     private boolean reqrstatement() throws ParseException, UnexpectedEndOfInputException {
         boolean result = false;
         if(expectKeyword("require", false)){
             getToken();
             if(consumeToken("cursor")){
                 getToken();
                 if(consumeToken("string")){
                     result = expect("terminator", true);
                 }
             }
         }
         return result;
     }
     
     private boolean anytype() throws ParseException, UnexpectedEndOfInputException {
          boolean result = false;
          if(expect("string", false)){
             result = true;
          }else if(factor()){
             result = true;
          }else if(numfactor()){
            result = true;
          }else{
            //throw new ParseException("Syntax Error: ");
          }
          return result;
     }
     
     private boolean callstatement() throws ParseException, UnexpectedEndOfInputException{
         boolean result = false;
          if(expectKeyword("call", false)){
              getToken();
              if(consumeToken("variable")){
                  getToken();
                  if(consumeToken("openbracket")){
                      boolean tick = false;
                      do{  
                        if(anytype()){
                           tick = true;
                        }else{
                           tick = false;
                        } 
                      }while(tick);
                      getToken();
                      if(consumeToken("closebracket")){
                           result = expect("terminator", true);
                      }
                  }
              }
          }
         return result;
     }
     
     private boolean defnstatement() throws ParseException, UnexpectedEndOfInputException {
          boolean result = false;
          if(expectKeyword("def", false)){
              getToken();
              if(consumeToken("varaible")){
                   if(anytype()){
                       result = expect("terminator", true);
                   }else{
                      throw new ParseException("Syntax Error: ");
                   }
              }
          }else if(expectKeyword("method", false)){
              
          }      
          return result;
     }
     
     private boolean retnstatement() throws ParseException, UnexpectedEndOfInputException {
         boolean result = false;
         if(expectKeyword("retn", true)){
             if(expect("variable", false)){
                 ;
             }
             result = expect("terminator", true);
         }
         return result;
     }
     
     private boolean anyexpression(){
      
          return true;
     }
     
     private boolean anystatement() throws ParseException, UnexpectedEndOfInputException {
          boolean result = false;
          if(defnstatement() || retnstatement() || callstatement()){
              result = true;
          }
          return result;
     }  
     
     private boolean statementblocks() throws ParseException, UnexpectedEndOfInputException {
        boolean result = false;
        do{
          if(anystatement()){
             result = true;
          }else{
             break;
          }
        }while(result);
        
        return result;
     }
     
     private void programblock() throws ParseException, UnexpectedEndOfInputException { /* START SYMBOL */
         boolean result = false;
         while(true){
            if(!reqrstatement()){
                break;
            }
         }
         while(true){
             if(!defnstatement()){
                break;
             }
         }
         if(expectKeyword("begin", false)){
             getToken();
             if(consumeToken("cursor")){
                  while(true){
                     if(!anystatement()){
                        break;
                     }
                  }
                  if(expectKeyword("end", true)){
                     result = expect("terminator", true);
                  }
             }else{
                throw new ParseException("Syntax Error: ");
             }    
         }
         
         while(true){
           if(!defnstatement()){
              break;
           }
         }
     }

     public ParseTree parse() throws ParseException, UnexpectedEndOfInputException {
           
          programblock();
          
          return parsed.getAST();
     }
     
     public static class UnexpectedEndOfInputException extends Exception{
     
           
           static final long serialVersionUID = 6429041231749920772L;

        
        
            UnexpectedEndOfInputException(String message) {
           
                   super(message);
       
            }

        

            UnexpectedEndOfInputException(Exception reason) {
            
                   super(reason);
       
            }
         
     }

     public static class ParseException extends Exception{
        
        

            static final long serialVersionUID = 4804542791749920772L;

        
        
            ParseException(String message) {
           
                   super(message);
       
            }

        

            ParseException(Exception reason) {
            
                   super(reason);
       
            }
  
    }
}