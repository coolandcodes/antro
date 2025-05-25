# TOKENIZER DEVELOPMENT STANDARD

- Token families available (identifier, keyword)

- All tokens that have type as "keyword" will always have the name of the token equal to its' image.

- All tokens must be resolve to either exactly one terminal or sequence of terminals (on the right hand side) and never sequence of non-terminals or terminals and non-terminals

- (variable) tokens will belong to "identifier" family, (if), (while), (for), (switch), (do), (void), (null), (export), (module), (case), (retn) tokens will belong to "keyword" family.


# PARSER DEVELOPMENT STANDARD

>This parser is an LL parser (top-down) with a lookahead of 1 (even though the tokenizer supports backtracking). The CFG tries as much as possible to avoid direct left-recursion and an ambiguous leftmost derivation of the parse-tree for the production rule of any given LHS non-terminal.

- All choice points in any CFG non-terminal production will have conditional expectation for failure (i.e. throwing a Parse Error / Exception when the token consumed doesn't match the production expansion from the grammar at that point)

- All CFG non-terminal productions with no choice points in their definition will fail unconditionally when they are in conflict with the order of token from the tokenizer.

- The tokenizer will have a "lookAheadToken" method - (this will return the next yet-to-be-consumed token which is the exact token in line to be retrieved by the parser)

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

      void getToken(void){
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
     
     boolean expect(String type, boolean canFail) throws ParseException {
            
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
                       throw new ParseException("Context Error: Invalid keyword token \"<"+actualImage+">\", found on line "+lineNumber+".");
                    }else{
                       throw new ParseException("Syntax Error: Unexpected token \"<"+actualImage+">\" of type <"+actualType+">, found on line "+lineNumber+"  \r\n\r\n compiler expected <"+type+">");
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

     boolean consumeToken(String currentProductionRule) throws UnexpectedEndOfInputException{
        boolean result = false;  
        boolean non_terminal_switch = false;
        if(currentToken.equals(null)){
            if(allTokensCount === 0){
               throw new UnexpectedEndOfInputException("unexpected end of input from source file "+Parser.INPUT_FILE_NAME);
            }
        }else{
           if(!currentToken.getType().equals("")){
                result = true;
                
                if(parsed.getCurrentProduction() === null){
                    // if we are at the start symbol, then set to create the root node of the AST
                    parsed.setCurrentProduction(this.rootProductionRule); 
                }
                
                if(!parsed.getCurrentProduction().equals(currentProductionRule)){
                    // any time the below is called create new AST Node
                    parsed.setCurrentProduction(currentProductionRule); 
                    non_terminal_switch = true;
                }   

                parsed.appendTokenToTree(currentToken, non_terminal_switch);
                
           }else{
                // handle as error compiler error itself (not complicit of source file)
           }
        }
        
        return result;
     }
 
     // usage: consumeToken("composition");
```

4. At the begining of each production call (except start symbol production), the "expects" method will be called

```java

    // no pseudo code available

```

5. Parser will have a "parse" method defined as the Java pseudocode below:

```java

    ParseTree parse(void) throws ParseException, UnexpectedEndOfInputException{

        /* parse method will always call the start symbol production as entry point */
        
        if(programblock() === true){
          
            return parsed.getIntermediateRepresentation("AST");
        }
    }
```


### CFG PRODUCTION DEFINITIONS (PARSER) - Java pseudocode

```java

    boolean literal(boolean fail){

       boolean res = false;
       res =  (expect("string", res) 
                      || expect("int", res) 
                          || expect("float", res) 
                              || expect("boolean", fail));

       if(res){
          getToken(); /* retrieve the immediate next token from the tokenizer */
          consumeToken("literal");
       }

       return res;
    }

    boolean factor (boolean fail){

      boolean res = false;
      res = expect("variable", res);

      if(res){
         getToken(); /* retrieve the immediate next token from the tokenizer */
         consumeToken("factor");
      }else{
          res = literal( fail );
      }
      
      return res;
    }

    boolean term (boolean fail){

        boolean res = false;

         if(expect("arithmeticunaryoperator", res)){

              getToken();
              consumeToken("term");

              res = expect("variable", fail);

              if(res){
                  getToken(); /* retrieve the immediate next token from the tokenizer */
                  consumeToken("term");
              }
         }else{ 
            res = factor( fail );
            
            while(res && (expect("bitwise", false) || expect("arithmeticbinaryoperator-add", false))){
                    getToken();  /* retrieve the immediate next token from the tokenizer */
                    consumeToken("term");
                    res = factor( fail ); 
            }
        }

        return res;
    }

    boolean arithmeticexpression (boolean fail){

         boolean res = false;

         if(expect("openbracket", res)){
              getToken();
              consumeToken("airthmeticexpression");

              res = expression( fail ) && expect("closebracket", res);

              if(res){
                  getToken(); /* retrieve the immediate next token from the tokenizer */
                  consumeToken("expression");
              }
         }else{ 

            res = term( fail );

            while(res && (expect("arithmethicbinaryoperator-mul", false) || expect("relationaloperator", false))){
                    getToken();  /* retrieve the immediate next token from the tokenizer */
                    consumeToken("expression");
                    res = term( fail ); 
            }
        }

        return res; 
    }

    boolean array(boolean fail){ 
          
          boolean res = false;

          if(expect("ace", fail)){
              getToken();
              consumeToken("array");

              if(expect("openbrace", fail)){
                 do{

                    getToken();
                    consumeToken("array");

                    res = expression( res ) || array( fail );

                 }while(res && expect("comma", false));

                 res = expect("closebrace", fail);

                 if(res){
                    getToken();
                    consumeToken("array");
                 }
                  
              }
          }

          return res;
    }

    boolean condition(boolean fail){
         boolean res = false;
         
         if(expect("logicalunaryoperator", res)){
             do{
                
                getToken();  /* retrieve the immediate next token from the tokenizer */
                consumeToken("condition");
                res = expression( fail );

             }while(res && expect("logicalbinaryoperator", false));
         }

         return res;
    }

    boolean evaluation(boolean fail){
        
        boolean res = false;
        
        if(expect("openbracket", fail)){

            do{

                  getToken(); /* retrieve the immediate next token from the tokenizer */
                  consumeToken("evaluation");
                  res = expression ( fail );

            }while(res && expect("comma", false));

            res = expect("closebracket", fail);

            if(res){
                getToken();
                consumeToken("evaluation");
            }
        }

        return res;
    }

    boolean callexpression(boolean fail){ 
          
          boolean res = false;

          if(expect("call", fail)){
                getToken();
                consumeToken("callexpression");

                if(expect("cursor", fail)){
                    getToken();
                    consumeToken("callexpression");

                    if(expect("variable", fail)){
                        getToken();
                        consumeToken("callexpression");

                        res = evaluation( fail );

                    }
                }
          }

          return res;
    }

    boolean composition(boolean fail){ 

          boolean res = false;

          if(expect("variable", fail)){

              getToken();
              consumeToken("composition");

              res = true;

              if(expect("assignmentoperator", res)){
                  getToken();
                  consumeToken("composition");

                  res = callexpression ( res ) || expression ( fail );

              }
          }

          return res;
    }

    boolean declstatement (boolean fail){
          
          boolean res = false;
          
          if(expect("var", res)){
              do{

                  getToken();
                  consumeToken("declstatement");
                  res = composition( fail );

              }while(res && expect("comma", false));
          }

          res = expect("terminator", fail);
          
          if(res){  
              getToken();
              consumeToken("declstatement");
          }

          return res;
    }

    boolean reqrstatement(boolean fail){ 
          
          boolean res = false;

          if(expect("require", fail)){

                getToken();
                consumeToken("reqrstatement");

                if(expect("cursor", fail)){

                    getToken();
                    consumeToken("reqrstatement");

                    if(expect("string", fail)){

                          getToken();
                          consumeToken("reqrstatement");

                          if(expect("terminator", fail)){

                              getToken();
                              consumeToken("reqrstatement");
                          }
                    }
                }
          }

          return res;
    }

    boolean retnstatement(boolean fail){ 

          boolean res = false;

          if(expect("retn", fail)){
              
              getToken();
              consumeToken("retnstatement");

              res = callexpression( res ) || expression ( fail );


          }

          res = expect("termnator", fail);

          if(res){
              getToken();
              consumeToken("retnstatement");
          }

          return res;
    }

    boolean breakstatement(boolean fail){
          
          boolean res = false;

          if(expect("break", fail)){
              getToken();
              consumeToken("breakstatement");

              if(expect("terminator", fail)){
                  getToken();
                  consumeToken("breakstatement");

                  res = true;
              }
          }

          return res;
    }

    boolean continuestatement(boolean fail){

          boolean res = false;

          if(expect("continue", fail)){
              getToken();
              consumeToken("continuestatement");

              if(expect("terminator", fail)){
                  getToken();
                  consumeToken("continuestatement");

                  res = true;
              }
          }

          return res;
    }

    boolean defnstatement(boolean fail){

    }

```


getToken(); /* retrieve the immediate next token from the tokenizer */   
consumeToken("identifier"); /* consume the next token retrieved and ensure it is a {identifier} token else raise a syntax error */


### ANTRO LANGUAGE GRAMMAR DETAILS

Regular Grammar Productions (RGP) for ANTRO scripting language (TOKENIZER) -- EBNF
==================================================================================

#### Use [this EBNF metasyntax defintion](https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form) to read the productions below

(* The list of all valid tokens for Antro Language - A purely functional language without much OOP *)

- pound := "$" ; (* used in variable token definition *)

- hash := "#" ;

- opencue := "[" ;

- closecue := "]" ;

- uscore := "_" ;

- lt := "<" ;

- gt := ">" ;

- dquote := "\"" ; 

- squote := "'" ;

- cursor := ":" ;

- terminator := ";" ;

- openbracket := "(" ;

- closebracket := ")" ;

- openbrace := "{" ;

- closebrace := "}" ;

- dot := "." ;

- digit := "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;

- number := digit, { "0" | digit } ;

- letter := "a" | "b" | "c" | "d" | "e"  | "f" | "g"  | "h" | "i"  | "j" | "k"  | "l" | "m"  | "n" | "o"  | "p" | "q"  | "r" | "s"  | "t" | "u"  | "v" | "w"  | "x" | "y"  | "z" | "A"  | "B" | "C"  | "D" | "E"  | "F" | "G"  | "H" | "I"  | "J" | "K"  | "L" | "M"  | "N" | "O"  | "P" | "Q"  | "R" | "S"  | "T" | "U"  | "V" | "W"  | "X" | "Y"  | "Z" ;

- letterordigit := digit | letter ;

- plus := "+" ;

- minus := "-" ;

- multiply := "*" ;

- divide := "/"

- modulo := "%"

- assignmentoperator := [ minus | plus | multiply | divide | modulo ], "=" ;

- naturalnumber 

- int := "0" | [ minus ], number ;

- float := int, dot, number, [ ( "E" | "e" ), int ] ;

- pipe := "|" ;

- and := "&" ;

- EOF := "\0" ;

- if := "if" ;

- new := "new" ;

- end := "end" ;

- main := "main" ;

- void := "void" ;

- null := "null" ;

- type := ".int" | ".float" | ".string" | ".array" | ".boolean" | ".nil" ;

- defer := "defer" ;

- require := "require" ;

- def := "def" ;

- invariants := "invariants" ;

- switch := "switch" ;

- else := "else" ;

- elif := "elif" ;

- for := "for" ;

- begin := "begin" ;

- do := "do" ;

- while := "while" ;

- break := "break" ;

- case := "case" ;

- default := "default" ;

- throw := "throw" ;

- othrow := "or_throw" ;

- opanic := "or_panic" ;

- hook := "hook" ;

- call := "call" ;

- continue := "continue" ;

- export := "export" ;

- retn := "retn" ;

- var := "var" ;

- whitespace := "\f" | "\t" | "\r" | "\n" | "\b" | " " | ?? ;

- arithmeticunaryoperators := plus, plus | minus, minus ;

- arithmeticbinaryoperators := multiply | divide | modulo | plus | minus ;

- bitwise := pipe | and ;

- logicalunaryoperator := "!" ;

- logicalbinaryoperators := pipe,  pipe | and, and ;

- comparisonoperator :=  gt, [ assignmentoperator ] | lt, [ assignmentoperator ] ;

- relationaloperator := comparisonoperator | ( assignmentoperator | logicalunaryoperator ), assignmentoperator ;

- ace := "@" ;

- boolean := "true" | "false" ;

- link := "|:" ;

- comma := "," ;

- allchars := [ whitespace | letterordigit | pipe | and | cursor | terminator | hash | pound | dquote | squote | logicalunaryoperator | relationaloperator | retn | ace | multiply | divide | modulo | comma | uscore | plus | minus | assignmentoperator | openbracket | openbrace | closebracket | closebrace | dot ]

- string := dquote, { allchars - dquote },  dquote | squote, { allchars - squote }, squote

- identifier := ( pound | letter ), {  letterordigit | uscore  } ;

- comment := hash, { allchars - hash } ;



Context Free Grammar Productions (CFGP) for ANTRO scripting language (PARSER) -- EBNF
=====================================================================================
(* This is the list of all production rules *)

- literal :=  string | int | float | boolean | void | null;

- factor :=  identifier | literal ;

- term := factor, { ( bitwise | arithmeticbinaryoperator ), factor } | arithmeticunaryoperator, identifier | identifier, arithmeticunaryoperator ;

- airthmeticexpression := term, { ( arithmeticbinaryoperator | relationaloperator ), term } | openbracket, airthmeticexpression, closebracket ;

- array := ace, openbrace, [ airthmeticexpression | array ], { comma, airthmeticexpression | array }, closebrace ;

- logicoperationexpression := airthmeticexpression, { logicalbinaryoperator, airthmeticexpression } ;

- trialexpression := [ string ], callexpression, ( { link, (othrow | opanic), (identifier | "$") }, { link, hook, scopeblock } ) ;

- callexpression := call, cursor, identifier, openbracket, logicexpressionlist, closebracket ;
                    
- logicexpression :=  term | [ logicalunaryoperator ], ( callexpression | trialexpression | logicoperationexpression ) ;

- logicexpressionlist := logicexpression, { comma, logicexpression } ;

- declsolution := [ type ], identifier ;

- declexpression :=  declsolution, { assignmentoperator, logicexpression } ;

- declexpressionlist := declexpression, { comma, declexpression }

- declstatement := { var, declexpressionlist }, terminator ;

- reqrstatement := require, cursor, string, terminator ;

- retnstatement := retn, [ logicexpression ], terminator ;

- callstatement := callexpression, terminator ;

- parameterlist := declsolution, { comma, declsolution } ;

- functiondefstatement := openbracket, parameterlist, closebracket, scopeblock;

- defnstatement :=  def, cursor, identifier, ( term | functiondefstatement ), terminator ;

- forstatement := for, openbracket, declstatement, [ airthmeticexpression ], terminator, [ airthmeticexpression ], closebracket, scopeblock ;

- dowhilestatement := do, scopeblock, while, openbracket, logicexpression, closebracket ;

- whilestatement := while, openbracket, logicexpression, closebracket, scopeblock ;

- ifstatement := if, openbracket, logicexpression, closebracket, scopeblock ;

- elseifstatement := elif, openbracket, logicexpression, closebracket, scopeblock ;

- elsestatement := else, scopeblock ;

- switchstatement := switch openbrackect, term, closebracket openbrace { { case, literal, cursor }, { blockstatement }, flowstatement }, [ default, cursor, { blockstatement }, flowstatement ], closebrace ;

- breakstatement := break, terminator ;

- continuestatement := continue, terminator ;

- deferstatement := @TODO... later using the defer keyword;

- branchstatement := ifstatement, { elseifstatement }, { elsestatement } | switchstatement ;

- controlstatement := branchstatement | forstatement | whilestatement | dowhilestatement | retnstatement | breakstatement | continuestatement;

- flowstatement :=  ( break | continue ), terminator ;

- modulestatement := module, cursor, string, terminator ;

- exportstatement := export, cursor, identifier, { comma, identifier }, terminator ;

- blockstatment := declstatement | controlstatement ;

- scopeblock := openbrace, { blockstatement | flowstatement }, closebrace ;

- routineblock := method, cursor, identifier, openbracket, declexpressionlist, closebracket, scopeblock, [ terminator ] ;

- mainblock := begin, cursor, main, openbracket, declexpressionlist, closebracket, { blockstatement }, [ exportstatement ], end, [ terminator ] ;

- programblock := { modulestatement }, { reqrstatement }, { defnstatement }, [ mainblock ], { routineblock }, EOF ;



