package com.codedev.antro.compiler;

/*import java.util.Queue;*/
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Tokenizer{

     private int lineNumber = 1;

     private byte[] streamBytes;
     
     private char currentChar;

     private long currentCharCount = 0;

     private int tokenCount = 0;
     
     private boolean checkForKeyWord = false;
     
     private static final String[] KEYWORDS_LIST = {"main", "new", "break", "var", "require", "def", "if", "for", "while", "else", "else if", "switch", "begin", "end", "retn", "true", "false", "method", "call", "void", "case"}; // {Tokenizer.KEYWORDS_LIST}

     private static final short MAX_TOKEN_SIZE = 30; // {Tokenizer.MAX_TOKEN_SIZE} defines the maximum length for any given token!

     private ArrayList<Character> LEXEM_BUFFER = new ArrayList<Character>();

     private Deque<Token> tokenQueue = new LinkedList<Token>();

     private Stack<Token> consumedTokens = new Stack<Token>();

     private enum MachineStates{

           S0(true, "whitespace", 0), // 0
           S1(true, "relationaloperator", 1),
           S2(true, "relationaloperator", 2),
           S3(true, "logicalunaryoperator", 3),
           S4(true, "assignmentoperator", 4),
           S5(true, "relationaloperator", 5),
           S6(true, "relationaloperator", 6),
           S7(true, "int", 7),
           S8(false, null, 8),
           S9(false, null, 9),
           SA(false, null, 10),
           SB(true, "variable", 11),
           SC(true, "variable", 12),
           SD(true, "openbracket", 13),
           SE(false, null, 14),
           SF(true, "variable", 15),
           SG(false, null, 16),
           SH(true, "string", 17),
           SJ(false, "EOF", 18),
           SK(true, "cursor", 19),
           SL(true, "closebracket", 20),
           SM(true, "float", 21),
           SN(true, "arithmeticbinaryoperator-add", 22),
           SO(true, "logicalbinaryoperator", 23),
           SP(true, "arithmeticbinaryoperator-mul", 24),
           SQ(true, "bitwise", 25),
           SR(true, "bitwise", 26),
           SS(true, "arithmeticbinaryoperator-sub", 27),
           ST(true, "arithmeticunaryoperator", 28),
           SU(true, "arithmeticbinaryoperator-mul", 29),
           SV(true, "arithmeticbinaryoperator-mod", 30),
           SW(true, "terminator", 31),
           SY(true, "openbrace", 32),
           SZ(true, "closebrace", 33), 
           S_(true, "int", 34),       
           $_(true, "int", 35),
           $S(true, "comma", 36),
           S$(true, "keyword", 37),
           Sx(true, "boolean", 38),
           Sz(true, "overloadoperator", 39);    
 
           private int id;
           private boolean ac;
           private String at;

           private MachineStates(boolean aval, String attr, int id){
               this.ac = aval;
               this.at = attr;
               this.id = id;
           }

           public int getIdentity(){
             return this.id;
           }

           public boolean getAcceptValue(){
             return this.ac;
           }

           public String getAttribute(){
             return this.at;
           }

     }

     private MachineStates currentState;

     private MachineStates[] states;

     private MachineStates[] mStates;

     
     public Tokenizer(File input) throws IOException, FileNotFoundException, InvalidTokenCharException, IllegalArgumentException{

           if(input == null || !Files.getFileExtension(input).equals("txt")){
              throw new IllegalArgumentException("incorrect file type passed to Tokenizer...");
           }
            // read out the file to tokenize...           

           FileInputStream fs = new FileInputStream(input.getName());
           // DataInputStream in = new DataInputStream(new BufferedInputStream(fs));
           this.streamBytes = new byte[fs.available()]; // new byte[in.available()];
           for(int t=0; t < streamBytes.length; t++){
             // populate the bytes array with bytes from the File's input stream!
              fs.read(streamBytes, t, 1);  
              // streamBytes[t] = in.readByte();
           }
           
          init(); // setup buffer and DFA states!
          
         
          tokenize(); // let's go there!
          
     }



     private void init(){ 
        
        /* 
         mStates = new MachineStates[]{
      
              MachineStates.S0,
              MachineStates.S1,
              MachineStates.S2
        }
          Kai!! this one will take too long and too much of space Walahi...
        */ 

         LEXEM_BUFFER.ensureCapacity(MAX_TOKEN_SIZE);

         states = MachineStates.values();
      
         mStates = new MachineStates[states.length];

         for(MachineStates state : states){ 
       
              mStates[state.ordinal()] = state;
          
         }

         currentState = mStates[0]; // set to start state 
     
     } 


     private void tokenize() throws InvalidTokenCharException {
        boolean breakme = false;
        readChar();
        
        while(!EOF()){
          switch(currentState.getIdentity()){
            case 0:
             if(!breakme && floater()){ breakme = true; }
             if(!breakme && integer()){ breakme = true; }
             if(!breakme && cursor()){ breakme = true; }
             if(!breakme && comma()){ breakme = true; }
             if(!breakme && whitespace()){ breakme = true; }
             if(!breakme && variable()){ breakme = true; }
             if(!breakme && relationaloperator()){ breakme = true; }
             if(!breakme && arithmeticoperator()){ breakme = true; }
             if(!breakme && assignmentoperator()){ breakme = true; }
             if(!breakme && logicaloperator()){ breakme = true; }
             if(!breakme && openbracket()){ breakme = true; }
             if(!breakme && openbrace()){ breakme = true; }
             if(!breakme && closebrace()){ breakme = true; }
             if(!breakme && closebracket()){ breakme = true; }
             if(!breakme && string()){ breakme = true; }
             if(!breakme && terminator()){ breakme = true; }
            break;
          }
          // end switch statement

          if(!breakme){
                  String image = ultimateToken().getImage();
                  throw new InvalidTokenCharException("Invalid token character: '"+currentChar+"' found on line :"+lineNumber+" just before: '"+(image.charAt(image.length() - 1))+"'");
          }else{
             
              if(currentState.getAcceptValue() && !currentState.getAttribute().equals("whitespace")){
                  /* switch current state to "keyword" if ... */ 
                  if(currentState.getAttribute().equals("variable")){
                      checkForKeyWord = true;
                  }
                  
                  emitToken();
                     
              }
              breakme = false;
          } // end if / else 
       } // end while
     }


 
    /*!
     * All private token recognization methods 
     */

     private boolean openbracket(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == '('){
             result = true;
             currentState = mStates[13];
             LEXEM_BUFFER.add(currentChar);
             readChar();
          }
        }
        return result;
     }

     private boolean closebracket(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == ')'){
             result = true;
             currentState = mStates[20];
             LEXEM_BUFFER.add(currentChar);
             readChar();
          }
        }
        return result;
     }
     
      private boolean comma(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == ','){
            result = true;
            currentState = mStates[36];
            LEXEM_BUFFER.add(currentChar);
            readChar();
          }
        }
        return result;
     }

     private boolean openbrace(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == '{'){
            result = true;
            currentState = mStates[32];
            LEXEM_BUFFER.add(currentChar);
            readChar();
          }
        }
        return result;
     }

     private boolean closebrace(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == '}'){
             result = true;
             currentState = mStates[33];
             LEXEM_BUFFER.add(currentChar);
             readChar(); 
          }  
        }
        return result;
     }

     private boolean EOF(){ // TODO: remember that this was skipped in checking initial state!!
        boolean result = false;
        if(currentChar == '\u0000'){ // currentCharCount >= (streamBytes.length - 1)
           result = true;
           currentState = mStates[18]; // End-Of-File accept machine state
        }
        return result;
     }


     private boolean cursor(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == ':'){
             result = true;
             currentState = mStates[19];
             LEXEM_BUFFER.add(currentChar);
             readChar();
          }
        }
        return result;
     }

     private boolean terminator(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == ';'){
             result = true;
             currentState = mStates[31];
             LEXEM_BUFFER.add(currentChar);
             readChar();
          }
        }
        return result;
     }

     private boolean whitespace(){
        boolean result = false;
        int x = currentState.getIdentity();
        boolean accept = (x != 8 && x != 9 && x!= 10 && x != 14 && x != 16); 
        if(accept){
          if(Character.isWhitespace(currentChar)){
            result = true;
            if(currentChar == '\n'){
                ++lineNumber;
            }
            if(currentChar == '\b'){

            }
            if(currentChar == '\f'){

            }
            if(currentChar == '\t'){

            }
            if(currentChar == '\r'){
 
            }
            currentState = mStates[0];
            readChar();
          }
        }
        return result;
     }

     private boolean logicalunary(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == '!'){
             result = true;
             currentState = mStates[3];
             LEXEM_BUFFER.add(currentChar);
             readChar();
          }
        } 
        return result;
     }

     private boolean plusOrMinus(){
        boolean result = false;
        int x = currentState.getIdentity();
        boolean accept = (x == 0 || x == 22 || x == 27);
        if(accept){
           if(currentChar == '+' || currentChar == '-'){
              result = true;
              if(x == 22 || x == 27){
                currentState = mStates[28];
              } 
              if(x == 0){
                currentState = (currentChar == '+') ? mStates[22] : mStates[27];
              }
              LEXEM_BUFFER.add(currentChar);
              readChar();
           }
        }
        return result;
     }

     private boolean mulOrDivide(){
         boolean result = false;
         int x = currentState.getIdentity();
         if(x == 0){
           if(currentChar == '*' || currentChar == '/'){
              result = true;
              currentState = (currentChar == '*')? mStates[24] : mStates[29];
              LEXEM_BUFFER.add(currentChar);
              readChar();
           }
           if(currentChar == '%'){
              result = true;
              currentState = mStates[30];
              LEXEM_BUFFER.add(currentChar);
              readChar();
           }
         }
         return result;
     }
   
     private boolean integer(){
        boolean result = false;
 
        int x = currentState.getIdentity(); // save initial state
        boolean accept = (x == 0 || x == 8 || x == 9 || x == 10 || x == 14 || x == 22 || x == 27);
        if(accept){  
            if(plusOrMinus()){
               result = false;
            }
          
            if(Character.isDigit(currentChar)){ 
               do{
                  if(x == 0 || x == 27 || x == 22){
                     currentState = mStates[7];
                     result = true;
                  }
                  if(x == 8){
                     currentState = mStates[21];
                     result = true;
                  }
                  if(x == 9){
                     currentState = mStates[35];
                     result = true;
                  }
                  if(x == 10){
                     currentState = mStates[34];
                     result = true;
                  }
                  if(x == 14){
                     currentState = mStates[16];
                     result = true;
                  }
                  LEXEM_BUFFER.add(currentChar);
                  readChar(); 
              }while(result); 
            }         
        }
        return result;
     }


     private boolean floater(){
        boolean result = false;
        if(integer()){
            result = true;
            if(currentChar == '.' || currentChar == 'e' || currentChar == 'E'){
                result = false;
                if(currentChar == '.') currentState = mStates[8];
                if(currentChar == 'e') currentState = mStates[10];
                if(currentChar == 'E') currentState = mStates[9];
                LEXEM_BUFFER.add(currentChar);
                readChar();
                  if(integer()){
                       result = true;
                       if(currentChar == '.'){
                            result = false;
                            currentState = mStates[8];
                            LEXEM_BUFFER.add(currentChar);
                            readChar();
                            if(integer()){
                                result = true;
                            }
                       }

                   }
            } 
        }
        return result;
     }
     
     private boolean bool(){
      
          return true;
     }

     private boolean relationaloperator(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(assignmentoperator() || comparisonoperator() || logicalunary()){
             result = true;             
             if(assignmentoperator()){
                result = true;
                if(assignmentoperator()){
                   result = true;
                }
             }
          }
        }
        return result;
     }

     private boolean comparisonoperator(){
        boolean result = false;
        int x = currentState.getIdentity();
        if(x == 0){
          if(currentChar == '<' || currentChar == '>'){
            result = true;
            currentState = (currentChar == '<')? mStates[2] : mStates[1];
            LEXEM_BUFFER.add(currentChar);
            readChar();
          }
        }
        return result;
     }

     private boolean arithmeticoperator(){
         boolean result = false; // Java demands we must initialize local variables
         int x = currentState.getIdentity(); // save initial state
         if(x == 0){
           if(plusOrMinus()){
             result = true;
              if(plusOrMinus()){
                result = true;
              }
           }
           if(mulOrDivide()){
               result = true;
           } 
         }
       return result;
     }

   
     private boolean variable(){
        boolean result = false;
        int x = currentState.getIdentity(); // save initial state
        boolean accept = (x == 0 || x == 12 || x == 11);
        if(accept){
          if(currentChar == '$' || Character.isLetter(currentChar)){
            result = true; 
            if(x == 0){
              currentState = (currentChar == '$')?  mStates[12] : mStates[11];
            }
            LEXEM_BUFFER.add(currentChar);
            readChar();
            if(Character.isLetterOrDigit(currentChar)){
              do{
               result = true;
               if(x == 12 || x == 11){
                  currentState = mStates[15];
               }
               LEXEM_BUFFER.add(currentChar);
               readChar();
              }while(Character.isLetterOrDigit(currentChar));
            }
          }
        }
        return result;
     }

     private boolean logicaloperator(){
         boolean result = false;
         if(logicalunary() || bitwise()){
             result = true;
             if(bitwise()){
               result = true;
             }
         }
         return result;
     }

     private boolean bitwise(){
        boolean result = false;
        int x = currentState.getIdentity(); // save initial state
        boolean accept = (x == 0 || x == 25 || x == 26);
        if(accept){
          if(currentChar == '|' || currentChar == '&'){
              result = true;
              if(x == 0){
                 currentState = (currentChar == '|')? mStates[25] : mStates[26];
              }
              if(x == 25 || x == 26){
                 currentState = mStates[23];
              }
              LEXEM_BUFFER.add(currentChar);
              readChar();
          }
        }
        return result; 
     }

     private boolean assignmentoperator(){
        boolean result = false; 
        int x = currentState.getIdentity(); // save initial state
        boolean accept = (x == 0 || x == 4 || x == 5 || x == 1 || x == 3);
        if(accept){
          if(currentChar == '='){
             result = true;
             if(x == 0) currentState = mStates[4];
             if(x == 1) currentState = mStates[5];
             if(x == 4) currentState = mStates[5];
             if(x == 5) currentState = mStates[6];
             if(x == 3) currentState = mStates[5];
             LEXEM_BUFFER.add(currentChar);
             readChar();
          }
        }
        return result;
     }

     private boolean string(){
        boolean result = false;
        int x = currentState.getIdentity();
        boolean accept = (x == 0);
        if(accept){
           if(currentChar == '"'){
                 currentState = mStates[14];
                 LEXEM_BUFFER.add(currentChar);
                 readChar();
                    while(currentChar != '"'){
                        currentState = mStates[16];
                        LEXEM_BUFFER.add(currentChar);
                        readChar();
                    }
                    if(currentChar == '"'){
                           result = true;
                           currentState = mStates[17];
                           LEXEM_BUFFER.add(currentChar);
                           readChar();
                    }
           }
        }
        return result;
     }

     private void readChar(){
        try{
          currentChar = (char) streamBytes[(int) currentCharCount++];
        }catch(ArrayIndexOutOfBoundsException e){
          currentChar = '\u0000';
          if(streamBytes.length - 1 < currentCharCount)
                --currentCharCount;
        }        
     }

     private void emitToken(){
        String image = getTokenImage();
        System.out.println("image: "+image+ " "+currentState.getAttribute());
        if(checkForKeyWord){
            int searchIndex = -1; /* assume the first point in the array {Tokenizer.KEYWORDS_LIST} */
            //searchIndex = Arrays.binarySearch(Tokenizer.KEYWORDS_LIST, image); TODO: dunno why binary search not working here... need to check!
            // TEMPORARY RESOLVE: we use linear search instead of binary search...
            for(int j = 0; j < Tokenizer.KEYWORDS_LIST.length; j++){
               if(image.equals(Tokenizer.KEYWORDS_LIST[j])){
                  searchIndex = j;
               }
            }
            if(searchIndex > -1){
               /* if we get here, it means that we found a keyword... */ 
               if(image.equals("true") || image.equals("false")){
                      currentState = mStates[38];
               }else{
                      currentState = mStates[37];
               }   
              
           }
            checkForKeyWord = false; /* reset the falg so we can be able to check for a keyword later on */ 
        }
        Token t = new Token(currentState.getAttribute(), image, lineNumber); // create a new "Token" from input
        tokenQueue.add(t); // store token at the end of the queue!
        tokenCount = tokenQueue.size(); // record the new size!
        currentState = mStates[0]; // set the state machine (automaton) back to start state!
     }

     private String getTokenImage(){
         if(LEXEM_BUFFER.size() >= MAX_TOKEN_SIZE){
            // exceeded threshold for a the maximum size a token can have...
         }
         char[] lexemchars = new char[LEXEM_BUFFER.size()]; // ready the buffer for read operation 
         String tokenchars = null;
         int i = 0;
         Iterator it = LEXEM_BUFFER.iterator(); // setup an iterator to loop on the buffer 
         while(it.hasNext()){
            lexemchars[i++] = ((Character) it.next()).charValue();// auto-unboxing should occur here, however, proceed with caution!
         }
         if(lexemchars.length > 0){
            tokenchars = new String(lexemchars); // form the string from input stream characters
            LEXEM_BUFFER.clear(); // empty the buffer
         }
         return tokenchars;
     }
     
     public final Token ultimateToken(){
        Token tk = null;
        if(hasMoreTokens()){ 
          tk = tokenQueue.getLast(); /* returns the LAST token but does not remove it from the queue */
        }
        return tk;
     }
     public final boolean hasMoreTokens(){

        return (tokenQueue.size() > 0); /* returns the number of tokens in the queue */
     }
     
     public final Token lookAheadToken(){
        Token tk = null;
        if(hasMoreTokens()){ // fail safe check!
           tk = tokenQueue.getFirst(); /* only retrieves the FIRST token but does not remove it from the queue */ 
        }  
        return tk;
     } 

     public final Token nextToken(){
        Token t = null, copy = null;
        if(hasMoreTokens()){ // fail safe check!
          
              t = (Token) tokenQueue.pollFirst(); // dequeue the first token at the head of thr queue!
           
           /*try{
            copy = (Token) t.clone(); // copy it out!
           }catch(CloneNotSupportedException e){
               copy = new Token(t.getType(), t.getImage(), t.getLineNumber()); // just another copy!
           }*/
        }
        // consumedTokens.push(copy);
        return consumedTokens.push(t);
     }

     public final boolean pushBack(int pushamount){
        boolean notpushed = false;
        Token t;
        try{
          while(pushamount-- > 0){
             t = consumedTokens.pop(); // pop the stack of consumed tokens!
             if(!(t == null)){
               tokenQueue.addFirst(t); // enqueue at the head of the queue!
             }
          }
        }catch(EmptyStackException e){
           notpushed = true;
        }catch(IllegalStateException e){
            notpushed = true;
        }catch(ClassCastException e){
            notpushed = true;
        }catch(NullPointerException e){

        }catch(IllegalArgumentException e){

        }
        return notpushed;
     }

     public int getTokenCount(){

        return tokenQueue.size();
     }    

     private static class Files{
          
             private Files(){
                // empty constructor!
             }

             public static String getFileExtension(File f){
                 String ext = "", name = f.getName();
                 int lastIndexOF = name.lastIndexOf('.');
                 if(lastIndexOF >= 0) ext = name.substring(lastIndexOF+1);
                 return ext;
             }
     }

     public static class InvalidTokenCharException extends Exception{

          static final long serialVersionUID = 475108353563832L;

          public InvalidTokenCharException(String message){
              super(message);
          }

          public InvalidTokenCharException(Exception reason){
              super(reason);
          }
     }
}