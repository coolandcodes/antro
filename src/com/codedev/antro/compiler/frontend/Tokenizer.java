package com.codedev.antro.compiler.frontend;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.lexer.TokenType;
import com.codedev.antro.compiler.frontend.lexer.LexemeQueue;

import com.codedev.antro.compiler.frontend.helpers.NoticeConsoleLogger;
import com.codedev.antro.compiler.frontend.contracts.concerns.LexisException;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * The core logic for turning file/string bytes or contents of buffered reader
 * into a series of tokens.
 */
public class Tokenizer {

    /* ============================
       Input handling
       ============================ */

    private final BufferedReader reader; /* @TODO: Modify this to use `NameBufferedReader` instead in the future */
    private final boolean multiCharScanActive;

    private StringBuilder multiCharScanBuffer = new StringBuilder();
    private String buffer = "";
    private int bufferPos = 0;

    private int line = 1;
    private int column = 0;

    /* ============================
       Output
       ============================ */

    private final LexemeQueue tokenQueue;


    /* ============================
       Keywords
       ============================ */

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
        Map.entry("if", TokenType.IF),
        Map.entry("else", TokenType.ELSE),
        Map.entry("elif", TokenType.ELIF),
        Map.entry("for", TokenType.FOR),
        Map.entry("while", TokenType.WHILE),
        Map.entry("do", TokenType.DO),
        Map.entry("begin", TokenType.BEGIN),
        Map.entry("end", TokenType.END),
        Map.entry("def", TokenType.DEF),
        Map.entry("var", TokenType.VAR),
        Map.entry("retn", TokenType.RETURN),
        Map.entry("continue", TokenType.CONTINUE),
        Map.entry("break", TokenType.BREAK),
        Map.entry("switch", TokenType.SWITCH),
        Map.entry("case", TokenType.CASE),
        Map.entry("default", TokenType.DEFAULT),
        Map.entry("main", TokenType.MAIN),
        Map.entry("void", TokenType.VOID),
        Map.entry("true", TokenType.BOOLEAN),
        Map.entry("false", TokenType.BOOLEAN),
        Map.entry("null", TokenType.NULL),
        Map.entry("panic_on", TokenType.PANIC_ON),
        Map.entry("eject_on", TokenType.EJECT_ON),
        Map.entry("use", TokenType.USE),
        Map.entry("call", TokenType.CALL),
        Map.entry("new", TokenType.NEW),
        Map.entry("export", TokenType.EXPORT),
        Map.entry("require", TokenType.REQUIRE),
        Map.entry("defer", TokenType.DEFER),
        Map.entry("invariants", TokenType.INVARIANTS),
        Map.entry("as", TokenType.ALIASER),
        Map.entry("static", TokenType.STATIC),
        Map.entry("struct", TokenType.STRUCT),
        Map.entry("impl", TokenType.IMPLEMENTATION),
        Map.entry("pause", TokenType.PAUSE),
        Map.entry(("inherits", TokenType.INHERITS),
        Map.entry("trait", TokenType.TRAIT),
        Map.entry("on", TokenType.MODIFIER),
        Map.entry("abstract", TokenType.QUALIFIER),
        Map.entry("package", TokenType.MODULE)
    );


    /* ============================
       Constructors
       ============================ */

    public Tokenizer(String source, LexemeQueue tokenQueue) {
        this.reader = new BufferedReader(new StringReader(source), 2000); /* @FIXME: Modify this to use `NameBufferedReader` instead in the future */
        this.tokenQueue = tokenQueue;
        this.multiCharScanActive = false;
    }

    public Tokenizer(BufferedReader reader, LexemeQueue tokenQueue) {
        this.reader = reader; /* @TODO: Modify this to use `NameBufferedReader` instead in the future */
        this.tokenQueue = tokenQueue;
        this.multiCharScanActive = false;
    }

    /* ============================
       Public API
       ============================ */

    /**
     *
     * @throws LexisException
     */
    public final void tokenize() {
        try {
            while (true) {
                char c = peek();
                if (isAtEnd(c)) {
                    if (multiCharScanActive) {
                        NoticeConsoleLogger.logMessage(
                            "TOKENIZER",
                            "token image truncated prematurely"
                        );
                        error("Token image truncated prematurely");
                    }
                    break;
                }
                scanNextByte(advance());
            }
        } catch (Exception e) {
            LexisException lexisEx = new LexisException(
                "lexical scan of source failed", 
                e
            );
            throw lexisEx;
        } finally {
            /* @HINT: Whether or not an error is thrown; emit an `EOF` token */
            emit(
                new Token(TokenType.EOF, String.valueOf('\0'), line, column)
            );
        }
    }

    private char readUnicodeEscape() throws Exception {
        int value = 0;
    
        for (int charCount = 0; charCount < 4; charCount++) {
            char c = advance();
            if (!isHexDigit(c)) {
                error("Invalid Unicode escape sequence");
            }
            value = (value << 4) + Character.digit(c, 16);
        }
    
        return (char) value;
    }


    /* ============================
       Core scanning
       ============================ */

    private void scanNextByte(char c) throws Exception {

        int startColumn = column;

        // Whitespace
        if (isWhitespace(c)) {
            if (multiCharScanActive) {
                multiCharScanActive = false;
            }
            return;
        }

        // Comment
        if (isCommentStart(c)) {
            advance();
            if (multiCharScanActive) {
                multiCharScanActive = false;
            }

            boolean commentTerminated = false;
            
            while (!isAtEnd(peek())) {
                if (isCommentEnd(c, peek())) {
                    advance();
                    commentTerminated = true;
                    break;
                }
                advance();
            }

            if (!commentTerminated && c == '/' && isAtEnd(peek())) {
                NoticeConsoleLogger.logMessage(
                    "TOKENIZER",
                    "unterminated comment found at the end of source on line: " + line
                );
                error("Unterminated comment found");
            }
            return;
        }

        // Strings
        if (c == '"' || c == '\'') {
            readString(c, false, startColumn);
            return;
        }

        // Formatted string
        if (c == 'f' && (peek() == '"' || peek() == '\'')) {
            char quote = advance();
            readString(quote, true, startColumn);
            return;
        }

        // Numbers
        if (isDigit(c) || (c == '-' && isDigit(peek()))) {
            readNumber(c, startColumn);
            return;
        }

        // Identifiers / keywords
        if (isIdentifierStart(c)) {
            readIdentifier(c, startColumn);
            return;
        }

        // Operators & punctuation
        switch (c) {
            case '+': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('+')) {
                    emit(simple(c, TokenType.INCREMENT));
                } else if (match('=')) {
                    emit(simple(c, TokenType.PLUS_ASSIGN));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.PLUS));
                }
                break;
            }
            case '-': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }
                
                if (match('>')) {
                    if (match('>')) {
                        emit(simple(c, TokenType.DOUBLE_ARROW));
                    } else {
                        emit(simple(c, TokenType.ARROW));
                    }
                } else if (match('-')) {
                    emit(simple(c, TokenType.DECREMENT));
                } else if (match('=')) {
                    emit(simple(c, TokenType.MINUS_ASSIGN));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.MINUS));                 
                }
                break;
            }
            case '*': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('=')) {
                    emit(simple(c, TokenType.STAR_ASSIGN));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.STAR));
                }
                break;
            }
            case '/': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('=')) {
                    emit(simple(c, TokenType.SLASH_ASSIGN));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.SLASH));
                }
                break;
            }
            case '%': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('=')) {
                    emit(simple(c, TokenType.MOD_ASSIGN));
                } else if (match('%')) {
                    emit(simple(c, TokenType.ANNOTATION));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.MODULO));
                }
                break;
            }
            case '&': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('&')) {
                    emit(simple(c, TokenType.LOGICAL_AND));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.BIT_AND));
                }
                break;
            }
            case '|': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('|')) {
                    emit(simple(c, TokenType.LOGICAL_OR));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.BIT_OR));
                }
                break;
            }
            case '<': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('<')) {
                    emit(simple(c, TokenType.SHIFT_LEFT));
                } else if (match('=')) {
                    emit(simple(c, TokenType.LESS_EQUAL));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.LESS));
                }
                break;
            }
            case '>': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('>')) {
                    emit(simple(c, TokenType.SHIFT_RIGHT));
                } else if (match('=')) {
                    emit(simple(c, TokenType.GREATER_EQUAL));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.GREATER));
                }
                break;
            }
            case '!': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('=')) {
                    emit(simple(c, TokenType.NOT_EQUAL));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.LOGICAL_NOT));
                }
                break;
            }
            case '=': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                if (match('=')) {
                    emit(simple(c, TokenType.EQUAL));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.ASSIGN));
                }
                break;
            }
            case '(': emit(simple(c, TokenType.LPAREN)); break;
            case ')': emit(simple(c, TokenType.RPAREN)); break;
            case '{': emit(simple(c, TokenType.LBRACE)); break;
            case '}': emit(simple(c, TokenType.RBRACE)); break;
            case '[': emit(simple(c, TokenType.LBRACKET)); break;
            case ']': emit(simple(c, TokenType.RBRACKET)); break;
            case ',': emit(simple(c, TokenType.COMMA)); break;
            case '.': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }

                char nextChar = peek();

                if (Character.isDigit(nextChar) || isSpecialCharacter(nextChar)) {
                    emit(simple(nextChar, TokenType.UNKNOWN));
                } else if (Character.isLetter(nextChar) && Character.isLetter(peek(true))) {
                    // @HINT: About to match a type annotation (e.g. `.int32`, `.byte`, `.bool`)

                    do {
                        multiCharScanBuffer.append(advance());
                    } while (!peekWhitespace());
                    
                    String text = multiCharScanBuffer.toString();
                    multiCharScanBuffer.delete(
                        0,
                        multiCharScanBuffer.length()
                    );
                    
                    if (text.equals(new String(".bool"))) {
                        emit(new Token(TokenType.TYPE_BOOL, text, line, column));
                    } else if (text.equals(new String(".byte"))) {
                        emit(new Token(TokenType.TYPE_BYTE, text, line, column));
                    } else if (text.equals(new String(".uint8"))) {
                        emit(new Token(TokenType.TYPE_INT, text, line, column));
                    } else if (text.equals(new String(".uint16"))) {
                        emit(new Token(TokenType.TYPE_INT, text, line, column));
                    } else if (text.equals(new String(".double"))) {
                        emit(new Token(TokenType.TYPE_DBL, text, line, column));
                    } else if (text.equals(new String(".float"))) {
                        emit(new Token(TokenType.TYPE_FLT, text, line, column));
                    } else if (text.equals(new String(".uint32"))) {
                        emit(new Token(TokenType.TYPE_INT, text, line, column));
                    } else if (text.equals(new String(".uint64"))) {
                        emit(new Token(TokenType.TYPE_INT, text, line, column));
                    } else if (text.equals(new String(".str"))) {
                        emit(new Token(TokenType.TYPE_STR, text, line, column));
                    } else if (text.equals(new String(".char"))) {
                        emit(new Token(TokenType.TYPE_CHAR, text, line, column));
                    } else if (text.equals(new String(".nil"))) {
                        emit(new Token(TokenType.TYPE_NIL, text, line, column));
                    } else if (text.equals(new String(".int"))) {
                        emit(new Token(TokenType.TYPE_INT, text, line, column));
                    } else {
                        emit(new Token(TokenType.TYPE_CUSTOM, text, line, column));
                    }
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.DOT));
                }
                break;
            }
            case ':': {
                if (!peekWhitespace()) {
                    /*
                        @HINT: 
                        
                        Assume multiple character token if there is 
                        no whitespace character until it becomes
                        clear no additional characters can be matched
                    */
                    multiCharScanActive = true;
                    multiCharScanBuffer.append(c);
                }
                
                if (match(':')) {
                    emit(simple(c, TokenType.JOINER));
                } else {
                    multiCharScanActive = false;
                    emit(simple(c, TokenType.COLON));
                }
                break;
            }
            case ';': emit(simple(c, TokenType.SEMICOLON)); break;
            case '@': emit(simple(c, TokenType.AT)); break;

            default: error("Unexpected character found: '" + String.valueOf(c) + "'"); break;
        }
    }

    /* ============================
       Readers
       ============================ */

    private void readIdentifier(char first, int col) throws Exception {
        multiCharScanActive = true;

        if (first !== null) {
            multiCharScanBuffer.append(first);
        } else {
            NullPointerException npEx = new NullPointerException(
                "reading first character (as null) of <identifier> on line: " + line
            );
            error("invalid <identifier> found", npEx);
        }

        while (isIdentifierPart(peek())) multiCharScanBuffer.append(advance());

        String text = multiCharScanBuffer.toString();
        multiCharScanBuffer.delete(
            0,
            multiCharScanBuffer.length()
        );
        
        TokenType type = text.equals(new String("null"))
            ? TokenType.NULL
            : KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        
        emit(new Token(type, text, line, col));
    }

    private void readNumber(char first, int col) throws Exception {
        multiCharScanActive = true;
        multiCharScanBuffer.append(first);

        boolean isHex = false;
        boolean isFloat = false;

        if (first == '0' && peek() == 'x') {
            multiCharScanBuffer.append(advance());
            isHex = true;
            while (isHexDigit(peek())) multiCharScanBuffer.append(advance());

            String text = multiCharScanBuffer.toString();
            multiCharScanBuffer.delete(
                0,
                multiCharScanBuffer.length()
            );

            emit(new Token(TokenType.INT_LITERAL, text, line, col));
            return;
        }

        while (isDigit(peek())) multiCharScanBuffer.append(advance());

        if (peek() == '.') {
            isFloat = true;
            advance();
            while (isDigit(peek())) multiCharScanBuffer.append(advance());

            if (peek() == 'e' || peek() == 'E') {
                multiCharScanBuffer.append(advance());
                if (peek() == '+' || peek() == '-') multiCharScanBuffer.append(advance());
                while (isDigit(peek())) multiCharScanBuffer.append(advance());
            }
        }

        String text = multiCharScanBuffer.toString();
        multiCharScanBuffer.delete(
            0,
            multiCharScanBuffer.length()
        );

        emit(new Token(
            isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL,
            text,
            line,
            col
        ));
    }


    private void readString(char quote, boolean formatted, int col) throws Exception {
        multiCharScanActive = true;
        multiCharScanBuffer.append(formatted ? "f/" + quote : quote);

        char c = peek();
        
        while (c != quote) {
    
            if (isAtEnd(c)) {
              error("Unterminated string literal found");
            }
    
            if (c == '\\') {
                advance(); // discard reverse solidus char
                
                char esc = peek();
                switch (esc) {
    
                    case 'n' -> {
                        advance();
                        multiCharScanBuffer.append('\n');
                    }
                    case 't' -> {
                        advance();
                        multiCharScanBuffer.append('\t');
                    }
                    case 'r' -> {
                        advance();
                        multiCharScanBuffer.append('\r');
                    }
                    case 'b' -> {
                        advance();
                        multiCharScanBuffer.append('\b');
                    }
                    case 'f' -> {
                        advance();
                        multiCharScanBuffer.append('\f');
                    }
                    case '\\' -> {
                        advance();
                        multiCharScanBuffer.append('\\');
                    }
                    case '\'' -> {
                        advance();
                        multiCharScanBuffer.append('\'');
                    }
                    case '"' -> {
                        advance();
                        multiCharScanBuffer.append('\"');
                    }
                    case 'u' -> {
                        multiCharScanBuffer.append(readUnicodeEscape());
                    }
    
                    default -> error("Invalid escape sequence: \\" + esc);
                }
            } else {
                 multiCharScanBuffer.append(advance())
            }
            
            c = peek();
        }
    
        multiCharScanBuffer.append(advance()); // closing quote
        
        String text = multiCharScanBuffer.toString();
        multiCharScanBuffer.delete(
            0,
            multiCharScanBuffer.length()
        );
        
        emit(new Token(
            formatted ? TokenType.FORMATTED_STRING : TokenType.STRING,
            text,
            line,
            col
        ));
    }

    /* ============================
       Helpers
       ============================ */

    /**
     * Consume the next character
     */
    private char advance() throws Exception {
        boolean READER_EOF = false;
        char c = ' ';

        Exception exp = new Exception(
            "could not advance to next character in stream"
        );
        
        try {
            if (bufferPos >= buffer.length()) {
                buffer = reader.readLine();
                
                if (buffer == null) {
                    READER_EOF = true;
                } else {
                    buffer += '\n';
                }
                bufferPos = 0;
            }

            if (READER_EOF) {
                c = '\0';
            } else {
                c = buffer.charAt(bufferPos++);
                column++;
            }
        } catch (IOException ex) {
            exp.initCause(ex);
            throw exp;
        }
        
        if (isAtNewLine(c)) {
            line++;
            column = 0;
        }
        
        return c;
    }

    private char peek() throws Exception {
        char c = advance();
        
        bufferPos--;
        column--;
        
        return c;
    }

    private char peek(boolean setBufferPos_IncrByOne) throws Exception {
        if (!setBufferPos_IncrByOne) {
            return peek();
        }

        char c = advance();
        c = advance();
        
        bufferPos-=2;
        column-=2;
        
        return c;
    }

    private boolean match(char expected) throws Exception {
        if (peek() != expected) return false;
        
        if (multiCharScanActive) {
            multiCharScanBuffer.append(advance())
        } else {
            advance();
        }
        
        return true;
    }

    private boolean isAtEnd(char nextOnAdvance) {
        return nextOnAdvance == '\0';
    }

    private boolean isAtNewLine(char nextOnAdvance) {
        return nextOnAdvance == '\n';
    }

    private void emit(Token token) throws Exception {   
        boolean interrupted = false;
        
        if (multiCharScanActive) {
            multiCharScanActive = false;
        }

        
        try {
            while (true) {
                try {
                    if (token.getType() != TokenType.UNKNOWN) {
                        tokenQueue.pushNextToken(token);
                    } else {
                        error("Unexpected token image: '"+token.getImage()+"'");
                    }
                    interrupted = false;
                    return;
                } catch (InterruptedException ex) {
                    NoticeConsoleLogger.logMessage(
                        "TOKENIZER",
                        "thread running interrupted with message: " + ex.getMessage()
                    );
                    
                    /* 
                        @HINT: 
                        
                        Record that an interruption happened but defer 
                        restoring the interrupted status.
                    */
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                // @HINT: Restore interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }

    private Token simple(char c, TokenType type) {
        String text = multiCharScanActive
            ? multiCharScanBuffer.toString()
            : String.valueOf(c);
        
        multiCharScanBuffer.delete(
            0,
            multiCharScanBuffer.length()
        );
        return new Token(
            type,
            text,
            line,
            column
        );
    }

    private boolean isSpecialCharacter (char c) {
        return c == '%' || c == '&' || c == '|' || c == '?' || c == '$' || c == '@' || c == '!' || c == '=';
    }

    private boolean isWhitespace (char c) {
        // Character.isWhitespace(c);
        return c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f' || c == '\b';
    }

    private boolean peekWhitespace () throws Exception {
        if (isWhitespace(peek())) return true;
        return false;
    }

    private boolean isDigit (char c) {
        // Character.isDigit(c);
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit (char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isCommentStart (char c) throws Exception {
        if (c == '#') {
            return true;
        }

        if (c == '/') {
            char characterAhead = peek();
            return characterAhead == '*';
        }

        return false;
    }

    private boolean isCommentEnd(char start, char curr) throws Exception {
        if (start == '#') {
            return isAtNewLine(curr);
        }
        
        if (start == '/') {
            char characterAhead = peek();
            return curr == "*" && characterAhead == '/';
        }

        NoticeConsoleLogger.logMessage(
            "TOKENIZER",
            "plausible unexpected character: '"+start+"' at the start of comment"
        );
        return false;
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '$' || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || isDigit(c) || c == '_';
    }

    private void error(String msg) throws Exception {
        throw new Exception("[Line " + line + ", Col " + column + "]; " + msg);
    }

    private void error(String msg, RuntimeException ex) throws Exception {
        throw new Exception("[Line " + line + ", Col " + column + "]; " + msg, ex);
    }
}
