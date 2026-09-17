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
         Program prog = parseProgram();

         return new ParseTree(prog);
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

- joiner := cursor, cursor ;

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

- match := "match" ;

- static := "static" ;

- syncho := "synchronize" ;

- assignmentoperator := [ minus | plus | multiply | divide | modulo ], "=" ;

- int := number ;

- float := [ "0" ] | { digit }, dot, ("0" | digit), { "0" | digit }, [ ( "E" | "e" ) ], [ minus ], [ int ] ;

- pipe := "|" ;

- and := "&" ;

- EOF := "\0" ;

- if := "if" ;

- new := "new" ;

- end := "end" ;

- void := "void" ;

- null := "null" ;

- type := ".int" | ".float" | ".str" | ".arr" | ".bool" | ".nil" | ".char" | ".byte" | ".double" | ".long" | ".ulong" | ".anynumber" | ".anypointer" | ".struct";

- visibility := "priv" | "publ" ;

- defer := "defer" ;

- require := "require" ;

- inherits := "inherits" ;

- pause := "pause" ;

- implementation := "impl" ;

- binder := "on" ;

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

- struct := "struct" ;

- trait := "trait" ;

- export := "export" ;

- aliaser := "as" ;

- retn := "retn" ;

- var := "var" ;

- const := "const" ;

- enum := "enum" ;

- module := "package";

- whitespace := "\f" | "\t" | "\r" | "\n" | "\b" | " " | ?? ;

- annotation := modulo, modulo ;

- incrementarithmeticunaryoperator := plus, plus ;

- decrementarithmeticunaryoperator := minus, minus ;

- arithmeticbinaryoperators := multiply | divide | modulo | plus | minus ;

- bitwiseoperators := pipe | and | lt, lt | gt, gt ;

- logicalunaryoperator := "!" ;

- stringformatprefix := "f" ;

- orlogicalbinaryoperator := pipe, pipe ;

- andlogicalbinaryoperator := and, and ;

- comparisonoperator :=  gt, [ assignmentoperator ] | lt, [ assignmentoperator ] ;

- relationaloperator := ( assignmentoperator | logicalunaryoperator ), assignmentoperator, [ assignmentoperator ] ;

- ace := "@" ;

- boolean := "true" | "false" ;

- link := "->" ;

- signlink := "->>" ;

- directed := "=>" ;

- comma := "," ;

- allchars := whitespace | [ letterordigit | pipe | and | cursor | terminator | hash | pound | dquote | squote | logicalunaryoperator | relationaloperator | retn | ace | multiply | divide | modulo | comma | uscore | plus | minus | assignmentoperator | openbracket | openbrace | closebracket | closebrace | dot ]

- string := dquote, { allchars - dquote }, dquote | squote, { allchars - squote }, squote ;

- formattedstring := stringformatprefix, string ;

- identifier := ( pound | letter | uscore ), {  letterordigit | uscore  } ;

- comment := hash, { allchars - hash } | ( divide, multiply ), { allchars - ( multiply, divide ), (multiply, divide) ;


Context Free Grammar Productions (CFGP) for ANTRO scripting language (PARSER) -- EBNF
=====================================================================================
(* This is the list of all production rules *)

- numericliteral   := int | float ;

- stringliteral    := string | formattedstring ;

- enumitemtuple    := openbracket, ( numericliteral | stringliteral ), closebracket ;

- structblocklist  := identifier, type, { comma, identifier, type } ;

- enumitemstruct   := openbracket, ( openbrace, structblocklist, closebrace, | identifier, [ ".struct" ] ), closebracket ;

- enumitem         :=  ( visibility )?, identifier, ( enumitemtuple | enumitemstruct )?, [ assignmentoperator, expression ] ;

- structure        := struct, identifier, openbrace, structblocklist, closebrace ;

- enumeration      := enum, identifier, openbrace, enumitem, { comma, enumitem }, closebrace ;

- boundfnlist      := identifier, openbracket, declexpressionlist, closebracket, [ type, ( signlink, ".Err" )? ] ;

- traitblock       := [ inherits, identifier, openbrace, ( multiply | identifier, { comma, identifier } ), closebrace, terminator ], structblocklist, { comma, boundfnlist } ;

- traitform        := trait, identifier, openbrace, traitblock, closebrace ;

- array            := ace, openbrace, [ expressionsgroup | array ], { comma, ( expressionsgroup | array ) }, closebrace ;

- rhs_slot         := boolean | callexpression | trialexpression ;

- postfix           :=  identifier, ( incrementarithmeticunaryoperator | decrementarithmeticunaryoperator ) ;

- increment_prefix  := incrementarithmeticunaryoperator, identifier ;

- decrement_prefix  := decrementarithmeticunaryoperator, identifier ;

- calcexpression   :=  numericliteral | expressionsgroup ;

- term             := rhs_slot | identifier | array | null ;

- unary             :=  ( logicalunaryoperator )?, ( decrement_prefix | increment_prefix | rhs_slot ) | ( minus | plus )?, ( calcexpression | postfix ) | identifier ;

- arithmetic        := stringliteral { plus, stringliteral } | unary { arithmeticbinaryoperators, unary } ;

- bitwise           := arithmetic { bitwiseoperators, arithmetic } ;

- relational        := bitwise { comparisonoperator, bitwise } ;

- equality          := relational { relationaloperator, relational } ;

- logical_and       := equality { andlogicalbinaryoperator, equality } ;

- logical_or        := logical_and { orlogicalbinaryoperator, logical_and } ;

- assignment        := logical_or | identifier, assignmentoperator, assignment ;

- expression        := assignment { comma, assignment } | identifier, cursor, identifier ;

- expressionsgroup  := openbracket, expression, closebracket ;

- expressionset     := expression, { comma, expression } ;

- logicexpression   :=  void | null | expressionset | expressiongroup ;

- logicexpressionlist := logicexpression, { comma, logicexpression } ;

- callexpression := call, cursor, identifier ( joiner, identifier | new )?, openbracket, logicexpressionlist, closebracket ;

- trialexpression := [ stringedterm, comma ], callexpression, [ link, oeject, identifier ], [ link, hook, limitedscopeblock ] ;

- limitedtrialexpression := [ stringedterm, comma ], callexpression, [ link, oeject, identifier ] ;

- declsolution := identifier, [ type ];

- declexpression :=  declsolution, { assignmentoperator, ( logicexpression | array ) } ;

- declexpressionlist := declexpression, { comma, declexpression } ;

- simpledeclunit := [ static ], { ( var | const ), declexpressionlist }, terminator ;

- declstatement := simpledeclunit | ( structure | enumeration | traitform ), terminator;

- reqrstatement := require, cursor, string, { aliaser, identifier }, terminator ;

- retnstatement := retn, [ ( logicexpression | array ) ], [ terminator ] ; (* if we put `logicexpressionlist` instead of `logicexpression` here, we risk making antro a multi-value return language *)

- callstatement := trialexpression, terminator ;

- fdefnbody := openbracket, declexpressionlist, closebracket, [ type, ( signlink, ".Err" )? ], scopeblock ;

- globalliteraldefnstatement := def, cursor, identifier, literal, terminator ;

- globalfunctiondefnstatement := def, cursor, identifier, fdefnbody, terminator ;

- localfunctiondefnstatement := var, identifier, fdefnbody, [ terminator ] ;

- globaldefnstatement := globalliteraldefnstatement | globalfunctiondefnstatement ;

- forstatement := for, openbracket, simpledeclunit, [ expression ], terminator, ( increment_prefix | decrement_prefix | postfix ), closebracket, scopeblock ;

- dowhilestatement := do, scopeblock, while, openbracket, logicexpression, closebracket ;

- whilestatement := while, openbracket, logicexpression, closebracket, scopeblock ;

- ifstatement := if, openbracket, logicexpression, closebracket, scopeblock ;

- elseifstatement := elif, openbracket, logicexpression, closebracket, scopeblock ;

- elsestatement := else, scopeblock ;

- switchblock := openbrace { { case, logicexpression, cursor }, { blockstatement }, [ breakstatement ] }, [ default, cursor, { blockstatement }, breakstatement ], closebrace ;

- switchstatement := switch openbrackect, term, closebracket switchblock ;

- breakstatement := break, terminator ;

- continuestatement := continue, terminator ;

- matchitem := (numericliteral | stringliteral | identifier), directed, expressionset ;

- matchstatement := match, openbracket, identifier, [ ".Err" ], closebracket, openbrace, matchitem, { comma, matchitem }, closebrace ;

- pausestatement := pause, openbracket, identifier, [ ".Err" ], closebracket, openbrace, { declstatement | controlstatement }, closebrace, terminator ;

- invariantstatement := [ defer ], link, invariants, openbrace, { declstatement | controlstatement | flowstatement | callstatement }, closebrace, [ terminator ] ;

- deferstatement := defer, openbrace, { declexpressionlist | invariantstatement | pausestatement }, closebrace, terminator ;

- branchstatement := ifstatement, { elseifstatement }, { elsestatement } | switchstatement ;

- controlstatement := branchstatement | forstatement | whilestatement | dowhilestatement ;

- flowstatement :=  breakstatement | continuestatement ;

- modulestatement := module, cursor, string, terminator ;

- exportstatement := export, cursor, ( multiply | identifier, { comma, identifier } ), terminator ;

- blockstatment := declstatement | localfunctiondefnstatement | controlstatement | invariantstatement | deferstatement | retnstatement | callstatement | matchstatement ;

- limitedscopeblock :=  openbrace, { declstatement | controlstatement | deferstatement | retnstatement | ( limitedtrialexpression, terminator ) }, closebrace ;

- scopeblock := openbrace, { blockstatement | flowstatement }, closebrace ;

- mainblock := begin, cursor, openbracket, declexpressionlist, closebracket, { blockstatement }, end, [ terminator ] ;

- programblock := [ modulestatement ], { reqrstatement }, { globaldefnstatement }, [ mainblock ], { globaldefnstatement }, [ exportstatement ], EOF ;





