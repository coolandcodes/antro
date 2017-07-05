# TOKENIZER DEVELOPMENT STANDARD

- Token families available (identifier, keyword, symbol)

- All tokens must be resolve to either exactly one terminal or sequence of terminals (on the right hand side) and never sequence of non-terminals or terminals and non-terminals

- (variable) tokens will belong to identifier family, (if), (while), (for), (switch), (void), (case), (ret) tokens will belong to keyword family and other tokens will belong to symbol family


# PARSER DEVELOPMENT STANDARD

>This parser is an LL parser (top-down) with a lookahead of 1 (even though the tokenizer supports backtracking). The CFG tries as much as possible to avoid direct left-recursion and an ambiguous leftmost-derivation/parse-tree for the production of any given non-terminal.

- All choice points in any CFG non-terminal production will have conditional expectation for failure (i.e. throwing a Parse Exception)

- Tokenizer will have a "lookAheadToken" method - (this will return the yet to be consumed token which is the exact next token in line to be retrieved by the parser)

- "expect" Parser method should throw "ParseException" - (conditionally using "canFail" flag)

- "consumeToken" Parser method should throw "UnexpectedEndOfInputException" 

- "expect" Parser method will utilize "lookAheadToken" Tokenizer method

- Only call "expect" on token "follow points" and "choice points" never on non-token "follow points" 

- If a CFG production defintion starts with a call to another CFG non-terminal production defintion, do not call "getToken" immediately else if it starts with a call to a terminal definition, then place a call to "getToken" immediately first before the call to "consumeToken"

- Never ask for a token ( calling "getToken" ) if we are not ready to consume it immediately ( by calling "consumeToken" immediately after). This applies very much to using CFG production definitions / the Parser method "expect". In the event that we cannot consume ...


### PARSER METHOD USE AND CASE

1. Parser will have "getToken" method defined as the Java psuedocode below:
      
```java
      /* pre-condition: assume "currentToken" global variable and "allTokensCount" global variable */

      // procedure:

      void getToken"(void){
         if(tokenizer.hasMoreTokens()){
            currentToken = tokenizer.nextToken();
            --allTokensCount;
         }else{
            currentToken = null;
         }
      }

      // usage: getToken();
```

2. Parser will have an "expect" method defined as the Java psuedocode below:
   
```java

      // procedure:
     
     boolean expect(String type, boolean canFail) throws ParseException, UnexpectedEndOfInputException {
            String actualType = tokenizer.lookAheadToken().getType();
            String actualImage = tokenizer.lookAheadToken().getImage();
            int lineNumber = 0; 
            boolean isKeyword = actualType.equals("keyword");
            boolean result = false;
            
            if(actualType.equals(type)){
                 result = true;   
            }else{
              
              if(isKeyword && actualImage.equals(type)){
                  result = true;
              }
                  
              if(!result && canFail){
                  lineNumber = tokenizer.lookAheadToken().getLineNumber();
                  if(isKeyword){
                     throw new ParseException("Invalid token <"+actualImage+"> encountered on line "+lineNumber+".");
                  }else{
                     throw new ParseException("Unexpected token <"+type+">, found <"+actualType+"> on line "+lineNumber+".");
                  }   
              }
            }  
            
           return result;
     }

      // usage: expect("operator", false);
      //        expect("if", true);
      //        expect("begin", false);
```

3. Parser will have "consumeToken" method defined as the Java psuedocode below:

```java

     /* pre-condition: assume "currentNode" = (current node while traversing the Abstract Syntax Tree - AST being built) and "allTokensCount" =  (global variable) */
     
     // procedure:

     boolean consumeToken() throws UnexpectedEndOfInputException{
        boolean result = false;  
        if(currentToken.equals(null)){
            if(allTokensCount === 0){
               throw UnexpectedEndOfInputException("unexpected end of input from source file "+Parser.INPUT_FILE_NAME);
            }
        }else{
           if(!currentToken.getType().equals("")){
                result = true;              
                tokensGroup.add(currentToken);
                //more tree building code for AST depending on "currentToken" and "currentNode"
           }else{
                // handle as error compiler error itself (not complacent of source file)
           }
        }
        
        return result;
     }
 
     // usage: consumeToken();
```

4. At the begining of each production call (except start symbol production), the "expects" method will be called

```java

    // no pseudo code available

```

5. Parser will have a "parse" method defined as the Java pseudocode below:

```java

    Node parse(void) throws ParseException, UnexpectedEndOfInputException{
        /* parse method will always call the start symbol production as entry point */
        ASTNode ast = programblock();
        return ast;
    }
```


### CFG PRODUCTION DEFINITIONS (PARSER) - Java pseudocode

```java

    boolean literal(boolean fail){
       boolean res = false;
       res = expect("void", res) || expect("string", res) || expect("int", res) || expect("float", res) || expect("boolean", res);
       if(res){
          getToken(); /* retrieve the immediate next token from the tokenizer */
          consumeToken();
       }
       return res;
    }

    boolean factor (boolean fail){
      boolean res = false;
      res = expect("variable", res);

      if(res){
         getToken(); /* retrieve the immediate next token from the tokenizer */
         consumeToken();
      }else{
          res = literal( fail );
      }
      
      return res;
    }

    boolean arithmeticexpression (boolean fail){

    }

    boolean term (boolean fail){
         boolean res = false;
         if(expects("openbracket", res)){
              getToken();
              consumeToken();
              res = arithmeticexpression( fail ) && expects("closebracket", res);
              if(res){
                  getToken(); /* retrieve the immediate next token from the tokenizer */
                  consumeToken();
              }
         }else{ 
            res = arithmeticexpression( fail );
            while(res && (expect("arithmethicbinaryoperator-mul", res) || expect("relationaloperator", res))){
                    getToken();  /* retrieve the immediate next token from the tokenizer */
                    consumeToken();
                    res = arithmeticexpression( res ); 
            }
        }
        return res; 
    }

    boolean condition(boolean fail){
         boolean res = false;
         if(expect("logicalunaryoperator", res)){
           getToken();  /* retrieve the immediate next token from the tokenizer */
           consumeToken();
         }

         res = term( res );
      
         while(res && expect("logicalbinaryoperator")){
            getToken();  /* retrieve the immediate next token from the tokenizer */
            consumeToken("logicalbinaryoperator");
            res = term( fail );
         }

         return res;
    }

    boolean evaluation(boolean fail){
        boolean res = false;
        if(expect("openbracket", fail)){
            getToken(); /* re */
            consumeToken();
            res = term ( fail );
            while(res && expect("comma", res)){
                getToken();
                consumeToken();
                res = term ( fail ); 
            }
            res = expect("closebracket", fail);
            if(res){
                getToken();
                consumeToken();
            }
        }
        return res;
    }

    boolean composition(boolean fail){

    }

    boolean declstatement (boolean fail){
          boolean res = false;
          if(expect("var", res)){
              getToken();
              consumeToken();
              res = composition( fail );
          }

          while(res && expect("comma", res)){
                getToken();
                consumeToken();
                res = composition( fail ); 
          }

          res = expect("terminator", res);
          if(res){  
              getToken();
              consumeToken();
          }

          return res;
    }

```


getToken(); /* retrieve the immediate next token from the tokenizer */   
consumeToken("closebracket"); // ")"


### ANTRO LANGUAGE GRAMMAR DETAILS

Regular Grammar Productions (RGP) for ANTRO scripting language (TOKENIZER) -- EBNF
==================================================================================

- pound := "$" ; (* used in variable token definition *)

- hash := "#" ;

- assignmentoperator := "=" ;

- uscore := "_" ;

- lt := "<";

- gt := ">";

- dquote := "\""; 

- squote := "'";

- exponentchar := "E" | "e"

- cursor := ":"

- terminator := ";"

- openbracket := "("

- closebracket := ")"

- openbrace := "{"

- closebrace := "}"

- dot := "."

- digit := "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" 

- letter := "a" | "b" | "c" | "d" | "e"  | "f" | "g"  | "h" | "i"  | "j" | "k"  | "l" | "m"  | "n" | "o"  | "p" | "q"  | "r" | "s"  | "t" | "u"  | "v" | "w"  | "x" | "y"  | "z" | "A"  | "B" | "C"  | "D" | "E"  | "F" | "G"  | "H" | "I"  | "J" | "K"  | "L" | "M"  | "N" | "O"  | "P" | "Q"  | "R" | "S"  | "T" | "U"  | "V" | "W"  | "X" | "Y"  | "Z" ;

- letterordigit := digit | letter ;

- plus := "+" ;

- minus := "-" ;

- multiply := "*" ;

- divide := "/"

- modulo := "%"

- int := digit, { digit } ;

- float := int, dot, int [  exponentchars, int ] ;

- pipe := "|" ;

- and := "&" ;

- EOF := "\0" ;

- if := "if" ;

- new := "new" ;

- end := "end" ;

- main := "main" ;

- void := "void" ;

- require := "require" ;

- def := "def" ;

- switch := "switch" ;

- else := "else" ;

- elseif := "elseif" ;

- for := "for" ;

- begin := "begin" ;

- while := "while" ;

- break := "break" ;

- case := "case" ;

- default := "default" ;

- method := "method" ;

- call := "call" ;

- continue := "continue" ;

- print := "print" ;

- ret := "retn" ;

- var := "var" ;

- whitespace := "\f" | "\t" | "\r" | "\n" | "\b" | " " | ?? ;

- arithmeticunaryoperators := plus, [ plus ] | minus, [ minus ] ;

- arithmeticbinaryoperators := multiply | divide | modulo ;

- bitwise := pipe | and ;

- logicalunaryoperator := "!" ;

- logicalbinaryoperators := pipe, [ pipe ] | and, [ and ] ;

- comparisonoperator :=  gt, [ assignmentoperator ] | lt, [ assignmentoperator ] ;

- relationaloperator := comparisonoperator | ( assignmentoperator | logicalunaryoperator ), assignmentoperator ;

- ace := "@" ;

- boolean := "true" | "false" ;

- comma := "," ;

- allchars := [ whitespace | letterordigit | pipe | and | cursor | terminator | hash | pound | dquote | squote | logicalunaryoperator | ace | multiply | divide | modulo | comma | uscore | plus | minus | assignmentoperator | openbracket | openbrace | closebracket | closebrace | dot ]

- string := dquote, { allchars - dquote },  dquote | squote, { allchars - squote }, squote

- variable := pound | letter, {  letterordigit | uscore  } ;

- comment := hash, { allchars - hash } ;



Context Free Grammar Productions (CFGP) for ANTRO scripting language (PARSER) -- EBNF
=====================================================================================

- literal := void | string | int | float | boolean ;

- factor :=  variable | literal ;

- arithmeticexpression := factor, { bitwise | arithmeticbinaryoperator-add, factor } | arithmeticunaryoperator, variable ;

- term := arithmeticexpression, { arithmeticbinaryoperator-mul | relationaloperator ) arithmeticexpression } | openbracket, arithmeticexpression, closebracket ; 

- array := ace, openbrace, [ term | array ], { comma, term | array }, closebrace ;
                    
- condition := [ logicalunaryoperator ] term,  { logicalbinaryoperator, term } ;

- evaluation := openbracket, [ term ], { comma, term }, closebracket  ;

- callexpression := call, cursor, variable, evaluation ;

- composition := variable, [ assignmentoperator,  callexpression | term ];

- declstatement := [ var, composition ],  { comma, composition }, terminator ;

- reqrstatement := require, cursor, string, terminator ;

- retnstatement := ret, [ callexpression | term ], terminator ;

- breakstatement := break, terminator ;

- continuestatement := continue, terminator ;

- defnstatement :=  def, variable, literal, terminator ;

- printstatement := print, ( callexpression | term ), terminator ;

- forstatement := for, openbracket, declstatement, [ condition ], terminator, arithmeticexpression, closebracket, openbrace, { scopedstatement }, closebrace ;

- whilestatement := while, openbracket, condition, closebracket openbrace [ scopedstatement ] closebrace ;

- ifstatement := if, openbrackect, condition, closebracket, openbrace [ scopedstatement ], closebrace, { elseif, openbrackect, condition, closebracket | else , openbrace, { scopedstatement }, closebrace } ;

- switchstatement := switch openbrackect, term, closebracket openbrace { case, literal, cursor, { scopedstatement - breakstatement },  breakstatement }, [ default, cursor, { scopedstatement - breakstatement },  breakstatement ], closebrace ;

- scopedstatement := ifstatement | printstatement | forstatement | whilestatement | declstatement | retnstatement | switchstatement | breakstatement | continuestatement ;

- routineexpression := method, cursor, variable, evaluation, openbrace, { scopedstatement }, closebrace ;

- programblock := { reqrstatement }, { defnstatement }, [ begin, cursor, main, evaluation  [ scopedstatement ], end, terminator ], { routineexpression }



