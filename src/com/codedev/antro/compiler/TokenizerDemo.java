package com.codedev.antro.compiler;



import java.io.File;

import java.io.IOException;

import java.io.FileNotFoundException;



 /**
 
  *

  * @author SCOFIELD
 
  */

 
public class TokenizerDemo {
    
       
         public static void main(String[] args) throws IOException, IllegalArgumentException, FileNotFoundException{
      
                        try{
             
                         Tokenizer tok = new Tokenizer(new File("antro-lang.txt"));
             
                         System.out.println("Number of Tokens found: "+tok.getTokenCount());
           
             
                          while(tok.hasMoreTokens()){
                  
                               System.out.println(tok.nextToken());
             
                          }
          
                     }catch(Tokenizer.InvalidTokenCharException e){
             
                               System.err.println(e.getMessage());
          
                     }
          
     
          }
  
}
