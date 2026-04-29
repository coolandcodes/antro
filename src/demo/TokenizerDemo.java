package demo;

import com.codedev.antro.compiler.frontend.*;

import java.io.FileReader;
import java.io.BufferedReader;


 /**
  *
  * @author Ifeora Okechukwu
  * @version 0.0.1
  */

 
public class TokenizerDemo {
       
     public static void main(String[] args) {

          LexemeQueue sharedQueue = new LexemeQueue();

          Thread spawn = new Thread(() -> {
               while (true) {
                    try {
                         if (!sharedQueue.hasMoreTokens()) {
                              break;
                         }
                         Token t = sharedQueue.pullNextToken();
                         System.out.println("Token image: " + t.getImage() + "; Token line number: " + t.getLineNumber());
                    } catch (InterruptedException ex) {
                         Thread.currentThread().interrupt();
                         continue;
                    }
               }
          });
      
          try {

               BufferedReader reader = new BufferedReader(new FileReader("../../basic_program.antro"), 1000);
             
               Tokenizer tokenizer = new Tokenizer(reader, sharedQueue);
             
               tokenizer.tokenize();
          
          } catch (LexisException e) {
             
               System.err.println(e.getMessage());
          
          }

          spawn.start();

          spawn.join(); 
     
     }
  
}
