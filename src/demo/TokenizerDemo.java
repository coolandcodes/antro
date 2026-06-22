package demo;

import com.codedev.antro.compiler.frontend.*;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;


 /**
  *
  * @author Ifeora Okechukwu
  * @version 0.0.1
  */

 
public class TokenizerDemo {

     public static void main(String[] args) {

          boolean errorCaught = false;
          LexemeQueue sharedQueue = new LexemeQueue(10);


          try (BufferedReader reader = new BufferedReader(new FileReader("../../basic_program.antro"), 1000)) {

               try {
               
                    Tokenizer tokenizer = new Tokenizer(reader, sharedQueue);
               
                    tokenizer.tokenize();
               
               } catch (LexisException e) {
                    errorCaught = true;
                    System.err.println("Failed to complete lexical analysis; reason: " + e.getMessage());
               }
          } catch (IOException e) {
               errorCaught = true;
               System.err.println("Failed to read the antro source file: " + e.getMessage());
          } 

          if (errorCaught) {
               return;
          }

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
                         System.out.println("Spawned thread for tokenizer was interrupted. Exiting loop cleanly.");
                         break;
                    }
               }
          });

          spawn.start();

          try {
               spawn.join();
          } catch (InterruptedException exp) {
               Thread.currentThread().interrupt();
               System.err.println("Terminating program.... unable to wait for thread to complete.");
               
               // @NOTE: Cooperative cancellation instead of forceful termination
               spawn.interrupt();
          }
     
     }
  
}
