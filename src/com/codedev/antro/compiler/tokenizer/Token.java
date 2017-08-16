package com.codedev.antro.compiler.tokenizer;


public class Token implements Cloneable {
    
         private String kind;
        
         private String image;

         private int line_num;

         public Token(String typ, Object img, int line){
              
               this.kind = typ;
               this.image = (String) img;
               this.line_num = line;
         }

         @Override 
         protected Object clone() throws CloneNotSupportedException {

            return super.clone();
         }

         @Override
         public String toString(){

            String string = "Token type: "+this.kind+", Token image: '"+this.image+"' at line: "+this.line_num;
            return string;
         }

         public String getType(){
            
               return this.kind;
         }
       
         public String getImage(){

               return this.image;
         }

         public int getLineNumber(){
 
               return this.line_num;
         }
}