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

- "setExpectationWithMessage" / "setExpectationForToken" Parser method `parse()` should throw "ParseException"

- "advance" Parser method should throw "UnexpectedEndOfInputException" 

- "expect" Parser method will utilize "lookAheadToken" Tokenizer method
  
- "check" Parser method will return true only when `peek()` returns a Token that matches any expectation

- - Only call "matchAny" on token "follow points" and "choice points" 

- Only call "expect" on when a token is to be matched to validate grammar 

- If a CFG production defintion starts with a call to another CFG non-terminal production defintion, descent recursive through other production rule and terminate at a terminal.


## Entry point for `Parser.java`

```java

   public ParseTree parse () throws ParseException {
      try {
         Block block = parseProgramBlock();

         return new ParseTree(block);
          
      } catch (Exception e) {
         ParseException prsEx = new ParseException("parse syntax issue", e);
         throw prsEx;  
      }
   }
```


getToken(); /* retrieve the immediate next token from the tokenizer */   
consumeToken("identifier"); /* consume the next token retrieved and ensure it is a {identifier} token else raise a syntax error */


### ANTRO LANGUAGE GRAMMAR DETAILS

Regular Grammar Productions (RGP) for ANTRO scripting language (TOKENIZER) -- EBNF
==================================================================================

#### Use [this EBNF meta-syntax defintion](https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_form) to read the productions below

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

- number := [ "0x" ], ("0" | digit), { "0" | digit } ;

- letter := "a" | "b" | "c" | "d" | "e"  | "f" | "g"  | "h" | "i"  | "j" | "k"  | "l" | "m"  | "n" | "o"  | "p" | "q"  | "r" | "s"  | "t" | "u"  | "v" | "w"  | "x" | "y"  | "z" | "A"  | "B" | "C"  | "D" | "E"  | "F" | "G"  | "H" | "I"  | "J" | "K"  | "L" | "M"  | "N" | "O"  | "P" | "Q"  | "R" | "S"  | "T" | "U"  | "V" | "W"  | "X" | "Y"  | "Z" ;

- letterordigit := "0" | digit | letter ;

- plus := "+" ;

- minus := "-" ;

- multiply := "*" ;

- divide := "/" ;

- modulo := "%" ;

- assignmentoperator := [ minus | plus | multiply | divide | modulo ], "=" ;

- int := [ minus ], number ;

- float := [ "0" ] | { digit }, dot, ("0" | digit), { "0" | digit }, [ ( "E" | "e" ), int ] ;

- pipe := "|" ;

- and := "&" ;

- EOF := "\0" ;

- if := "if" ;

- new := "new" ;

- end := "end" ;

- main := "main" ;

- void := "void" ;

- null := "null" ;

- type := ".int" | ".flt" | ".str" | ".arr" | ".bool" | ".nil" ;

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

- oeject := "eject_on" ;

- opanic := "panic_on" ;

- hook := "use" ;

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

- stringformatprefix := "f" ;

- logicalbinaryoperators := pipe,  pipe | and, and ;

- comparisonoperator :=  gt, [ assignmentoperator ] | lt, [ assignmentoperator ] ;

- relationaloperator := comparisonoperator | ( assignmentoperator | logicalunaryoperator ), assignmentoperator, [ assignmentoperator ] ;

- ace := "@" ;

- boolean := "true" | "false" ;

- link := "->" ;

- comma := "," ;

- allchars := whitespace | [ letterordigit | pipe | and | cursor | terminator | hash | pound | dquote | squote | logicalunaryoperator | relationaloperator | retn | ace | multiply | divide | modulo | comma | uscore | plus | minus | assignmentoperator | openbracket | openbrace | closebracket | closebrace | dot ]

- string := dquote, { allchars - dquote }, dquote | squote, { allchars - squote }, squote ;

- formattedstring := stringformatprefix, string ;

- identifier := ( pound | letter ), {  letterordigit | uscore  } ;

- comment := hash, { allchars - hash } ;



Context Free Grammar Productions (CFGP) for ANTRO scripting language (PARSER) -- EBNF
=====================================================================================
(* This is the list of all production rules *)

- numericliteral := int | float ;

- stringliteral := string | formattedstring ;

- flagliteral := boolean ;

- factor :=  identifier | numericliteral ;

- stringedterm := stringliteral, { plus, stringliteral } ;

- flaggedterm := flagliteral, { bitwise, flagliteral } ;

- factoredterm := factor, { ( bitwise | arithmeticbinaryoperator ), factor } ;

- operandterm := flaggedterm | factoredterm | stringedterm | null ;

- symbolterm := (operandterm - stringedterm) | arithmeticunaryoperator, identifier | identifier, arithmeticunaryoperator ;

- arithmeticexpression := symbolterm, { arithmeticbinaryoperator, symbolterm } | identifier, assignmentoperator, arithmeticexpression ; (* This production rule is recursive *)

- relationalexpression := operandterm, { relationaloperator, operandterm } | identifier, assignmentoperator, relationalexpression ; (* This production rule is recursive *)

- airthmeticexpressionsgroup :=  openbracket, airthmeticexpression, arithmeticbinaryoperator, { airthmeticexpression | airthmeticexpressiongroup }, closebracket ; (* This production rule is recursive *)

- relationalexpressiongroup := openbracket, relationalexpression, relationaloperator, { relationalexpression | relationalexpressiongroup }, closebracket ; (* This production rule is recursive *)

- expressionsgroup := airthmeticexpressionsgroup | relationalexpressiongroup ;

- array := ace, openbrace, [ expressionsgroup | array ], { comma, expressionsgroup | array }, closebrace ;

- logicoperationexpression := expressionsgroup, { logicalbinaryoperator, expressionsgroup } ;

- callexpression := call, cursor, identifier, openbracket, logicexpressionlist, closebracket ;

- trialexpression := [ stringedterm, comma ], callexpression, ( { link, (oeject | opanic), (identifier | pound, pound) }, { link, hook, scopeblock } ) ;
                    
- logicexpression :=  operandterm | [ logicalunaryoperator ], ( callexpression | logicoperationexpression ) | trialexpression ;

- logicexpressionlist := logicexpression, { comma, logicexpression } ;

- declsolution := [ type ], identifier ;

- declexpression :=  declsolution, { assignmentoperator, logicexpression } ;

- declexpressionlist := declexpression, { comma, declexpression } ;

- declstatement := { var, declexpressionlist }, terminator ;

- reqrstatement := require, cursor, string, terminator ;

- retnstatement := retn, [ logicexpression ], [ terminator ] ; (* if we put `logicexpressionlist` instead of `logicexpresion`, we risk making antro an multi-value return language *)

- callstatement := trialexpression, terminator ;

- fdefnbody := openbracket, (void | declexpressionlist), closebracket, scopeblock ;

- globalliteraldefnstatement := def, cursor, identifier, literal, terminator ;

- globalfunctiondefnstatement := def, cursor, identifier, fdefnbody, terminator ;

- localfunctiondefnstatement := var, identifier, fdefnbody, [ terminator ] ;

- defnstatement := globalliteraldefnstatement | globalfunctiondefnstatement ;

- forstatement := for, openbracket, declstatement, [ airthmeticexpression ], terminator, [ airthmeticexpression ], closebracket, scopeblock ;

- dowhilestatement := do, scopeblock, while, openbracket, logicexpression, closebracket ;

- whilestatement := while, openbracket, logicexpression, closebracket, scopeblock ;

- ifstatement := if, openbracket, logicexpression, closebracket, scopeblock ;

- elseifstatement := elif, openbracket, logicexpression, closebracket, scopeblock ;

- elsestatement := else, scopeblock ;

- switchstatement := switch openbrackect, term, closebracket openbrace { { case, literal, cursor }, { blockstatement }, flowstatement }, [ default, cursor, { blockstatement }, flowstatement ], closebrace ;

- breakstatement := break, terminator ;

- continuestatement := continue, terminator ;

- invariantsblock := link, invariants, openbrace, { trialexpression }, closebrace, [ terminator ] ;

- deferstatement := defer, declexpressionlist | invariantsblock ;

- branchstatement := ifstatement, { elseifstatement }, { elsestatement } | switchstatement ;

- controlstatement := branchstatement | forstatement | whilestatement | dowhilestatement | retnstatement ;

- flowstatement :=  breakstatement | continuestatement ;

- modulestatement := module, cursor, string, terminator ;

- exportstatement := export, cursor, identifier, { comma, identifier }, terminator ;

- blockstatment := declstatement | localfunctiondefnstatement | controlstatement ;

- scopeblock := openbrace, { blockstatement | flowstatement }, closebrace ;

- mainblock := begin, cursor, openbracket, (void | declexpressionlist), closebracket, { blockstatement }, end, [ terminator ] ;

- programblock := { modulestatement }, { reqrstatement }, { defnstatement }, [ mainblock ], { defnstatement }, [ exportstatement ], EOF ;





