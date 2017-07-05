# antro

This is a toy parser for a toy programming language called _antor scripting language_ which is still in development.This project is purely educational as the language cannot be used for any industry work in its current form. Therefore, the sole aim of this project is to show and teach the skills required of designing computer languages and implementing them.

> The **Regular Grammar** and **Context-Free Grammar** (written in EBNF format) can be found in the *PARSER_ALGOS_AND_GRAMMAR.md* (markdown) file as well as the details of the algorithm used to implement the recursive descent strategy of the parser.

What is a Regular Grammar ?

- 

What is a Context-Free Grammar ?

- 

## Sample program written in antro

```antro

	require: "file.module";
	require: "regex.module";

	def: MAX 200; 

	begin:
	   main(void)
	     var ty = call: factorUpBy2(MAX);
	     print ty;
	end;

	method: convertToFactor(c, d){
	              retn c * d;
	}

	method: factorUpBy2(x){
	          var y, g = true;
	          if(x > 0){
	              y = (x / 2) * 4;
	          }else{
	              g = false;
	          }
	          y = call: convertToFactor(g, x); 
	        retn y;
	}

```

Though the above program doesn't do anything for now (i.e. the parser as currently written does not produce an Absract Syntax Tree - AST nor does it provide an Immediate Representation - IR), one can still get to understand the basics of what's going on. 

## License 

This is released under the MIT license

## Design Inspiration

Antro language design was inspired by Objective-C and JavaScript combined