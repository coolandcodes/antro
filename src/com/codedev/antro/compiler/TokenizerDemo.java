package com.codedev.antro.compiler;



import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;



 /**
  *
  * @author Ifeora Okechukwu
  * @version 0.0.1
  */

 
public class TokenizerDemo {
    
       
         public static void main(String[] args) throws IOException, IllegalArgumentException, FileNotFoundException{
      
                        try{
             
                         Tokenizer tokenizer = new Tokenizer(new File("program.antro"));
             
                         System.out.println("Number of Tokens found: "+tokenizer.getTokenCount());
           
             
                          while(tokenizer.hasMoreTokens()){
                  
                               System.out.println(tok.nextToken());
             
                          }
          
                     }catch(Tokenizer.InvalidTokenCharException e){
             
                               System.err.println(e.getMessage());
          
                     }
          
     
          }
  
}
